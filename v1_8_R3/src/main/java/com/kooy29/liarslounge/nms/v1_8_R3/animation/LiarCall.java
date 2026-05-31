package com.kooy29.liarslounge.nms.v1_8_R3.animation;

import com.kooy29.liarslounge.api.animation.ILiarCall;
import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.api.nms.IWrapperMethods;
import com.kooy29.liarslounge.nms.v1_8_R3.VersionWrapper;
import com.kooy29.liarslounge.storage.yaml.SoundsPath;
import com.kooy29.liarslounge.utils.SoundUtil;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;

public class LiarCall implements ILiarCall {

    IArena arena;
    HashMap<Player, EntityArmorStand> stands = new HashMap<>();
    Location nonStrikeLoc = null;
    private JavaPlugin instance;

    public LiarCall(JavaPlugin instance, IArena arena) {
        this.instance = instance;
        this.arena = arena;
    }

    public void clearOldData() {
        for (Player player : new ArrayList<>(stands.keySet())) {
            EntityArmorStand stand = stands.get(player);
            if (stand != null) {
                PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(stand.getId());
                for (Player p : arena.getWorld().getPlayers()) {
                    VersionWrapper.sendPacket(p, destroyPacket);
                }
                stands.remove(player);
                IWrapperMethods.armorStands.remove(stand.getId());
            }
        }
    }

    public void setupAxe(Player player, Location actionItemLoc) {
        arena.sendDebugMsg("LiarCall.java - setting up axe for player - " + player.getName());
        Vector dir = actionItemLoc.getDirection();
        dir.setY(0).normalize().multiply(1);
        Location newLoc = actionItemLoc.clone().add(dir);

        double yaw = Math.toDegrees(Math.atan2(
                actionItemLoc.getZ() - newLoc.getZ(),
                actionItemLoc.getX() - newLoc.getX()
        )) - 90;

        if (yaw < -180) yaw += 360;
        if (yaw > 180) yaw -= 360;

        newLoc.setYaw((float) yaw);
        newLoc.setPitch(0);

        double rad = Math.toRadians(yaw);
        double backX = -Math.cos(rad) * 0.25;
        double backZ = -Math.sin(rad) * 0.25;
        newLoc.add(backX, 0, backZ);


        EntityArmorStand stand;
        if (stands.containsKey(player)) {
            stand = stands.get(player);
        } else {
            stand = new ArmorStandBuilder(newLoc.getWorld())
                    .setGravity(false)
                    .setInvisible(true)
                    .setLocation(newLoc.getX(), newLoc.getY() - 0.4, newLoc.getZ(), newLoc.getYaw() - 85, 0f)
                    .getArmorStand();
        }


        ItemStack axe = new ItemStack(Material.GOLD_AXE);

        DataWatcher watcher = stand.getDataWatcher();

        watcher.watch(14, new Vector3f(0f, 0f, 90f));
        stands.put(player, stand);
        IWrapperMethods.armorStands.add(stand.getId());
        for (Player p : actionItemLoc.getWorld().getPlayers()) {
            VersionWrapper.sendPackets(p,
                    new PacketPlayOutSpawnEntityLiving(stand),
                    new PacketPlayOutEntityEquipment(stand.getId(), 0, CraftItemStack.asNMSCopy(axe)),
                    new PacketPlayOutEntityMetadata(stand.getId(), watcher, true));
        }
    }

    public void moveToPlayer(Player player, Runnable task) {
        EntityArmorStand stand = stands.get(player);
        if (stand == null) return;
        nonStrikeLoc = new Location(null, stand.locX, stand.lastY, stand.locZ, stand.yaw, stand.pitch);

        Location startLoc = stand.getBukkitEntity().getLocation();
        Location playerLoc = player.getLocation();

        // === SAME yaw logic as test() ===
        Vector dir = playerLoc.getDirection();
        dir.setY(0).normalize().multiply(1.3);
        Location newLoc = playerLoc.clone().add(dir);

        double yaw = Math.toDegrees(Math.atan2(
                playerLoc.getZ() - newLoc.getZ(),
                playerLoc.getX() - newLoc.getX()
        )) - 90;
        if (yaw < -180) yaw += 360;
        if (yaw > 180) yaw -= 360;

        float yawF = (float) yaw - 85;

        // Move vector towards newLoc
        Vector moveVec = newLoc.toVector().subtract(startLoc.toVector());
        int steps = 10; // ticks to move
        Vector stepVec = moveVec.multiply(1.0 / steps);

        byte yawByte = (byte) (yawF * 256 / 360);
        PacketPlayOutEntity.PacketPlayOutEntityLook packetHeadRot =
                new PacketPlayOutEntity.PacketPlayOutEntityLook(
                        stand.getId(),
                        yawByte,
                        (byte) 0,
                        true
                );

        // Send initial head rotation
        for (Player p : playerLoc.getWorld().getPlayers()) {
            VersionWrapper.sendPacket(p, packetHeadRot);
        }

        new BukkitRunnable() {
            int tick = 0;
            Location currentLoc = startLoc.clone();

            @Override
            public void run() {
                if (tick >= steps) {
                    cancel();
                    if (task != null) Bukkit.getScheduler().runTask(instance, task);
                    return;
                }

                currentLoc.add(stepVec);

                // Always set yaw so it stays correct
                stand.setLocation(currentLoc.getX(), currentLoc.getY(), currentLoc.getZ(), yawF, 0f);

                // Send teleport with correct yaw
                PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(stand);
                for (Player p : new ArrayList<>(playerLoc.getWorld().getPlayers())) {
                    arena.sendDebugMsg("LiarCall.java - sending teleport axe of player - " + player.getName() + " to viewer " + p.getName());
                    VersionWrapper.sendPacket(p, teleportPacket);
                }

                tick++;
            }
        }.runTaskTimerAsynchronously(instance, 10L, 1L);
    }

    public void moveBackToLoc(Player player) {
        EntityArmorStand stand = stands.get(player);
        if (stand == null) return;
        stand.setLocation(nonStrikeLoc.getX(), nonStrikeLoc.getY(), nonStrikeLoc.getZ(),
                nonStrikeLoc.getYaw(), nonStrikeLoc.getPitch());
        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(stand);
        byte yawByte = (byte) (nonStrikeLoc.getYaw() * 256 / 360);
        PacketPlayOutEntity.PacketPlayOutEntityLook packetHeadRot =
                new PacketPlayOutEntity.PacketPlayOutEntityLook(
                        stand.getId(),
                        yawByte,
                        (byte) 0,
                        true
                );
        for (Player p : new ArrayList<>(player.getLocation().getWorld().getPlayers())) {
            arena.sendDebugMsg("LiarCall.java - sending moveBackToLoc of player - " + player.getName() + " to viewer " + p.getName());
            VersionWrapper.sendPackets(p, teleportPacket, packetHeadRot);
        }
    }


    public void playAxeSwing(Player player, boolean fullSwing, Runnable task) {
        arena.sendDebugMsg("Called playAxeSwing!! - " + player.getName());
        EntityArmorStand stand = stands.get(player);
        new BukkitRunnable() {
            boolean reloading = true;
            float bodyYawDegrees = stand.yaw;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    destroyAxe(player, stand.getId());
                    Bukkit.getScheduler().runTaskLater(instance, task, 10L);
                    return;
                }
                if (reloading) {
                    bodyYawDegrees -= 1f;
                    byte yawByte = (byte) (bodyYawDegrees * 256 / 360);
                    PacketPlayOutEntity.PacketPlayOutEntityLook lookPacket =
                            new PacketPlayOutEntity.PacketPlayOutEntityLook(stand.getId(), yawByte, (byte) 0, true);
                    for (Player p : arena.getWorld().getPlayers()) {
                        arena.sendDebugMsg("LiarCall.java - sending playAxeSwing of player - " + player.getName() + " to viewer " + p.getName());
                        VersionWrapper.sendPacket(p, lookPacket);
                        if (stand.yaw - 60 == bodyYawDegrees)
                            SoundUtil.playSound(p, SoundsPath.CallLiar.AXE_RELOAD);
                    }
                    if (stand.yaw - 80 >= bodyYawDegrees) {
                        reloading = false;
                    }
                } else {
                    cancel();
                    if (fullSwing)
                        bodyYawDegrees += 180f; // swing the axe kill
                    else
                        bodyYawDegrees += 85f; // swing the axe just till neck
                    byte yawByte = (byte) (bodyYawDegrees * 256 / 360);
                    PacketPlayOutEntity.PacketPlayOutEntityLook lookPacket =
                            new PacketPlayOutEntity.PacketPlayOutEntityLook(stand.getId(), yawByte, (byte) 0, true);
                    for (Player p : arena.getWorld().getPlayers()) {
                        arena.sendDebugMsg("LiarCall.java - sending playAxeSwing of player - " + player.getName() + " to viewer " + p.getName() + ", fullSwing: - " + fullSwing);
                        VersionWrapper.sendPacket(p, lookPacket);
                        if (fullSwing) {
                            SoundUtil.playSound(p, SoundsPath.CallLiar.AXE_FULL_SWING);
                        } else {
                            Bukkit.getScheduler().runTaskLater(instance, () -> {
                                Bukkit.getScheduler().runTaskLater(instance, () -> p.getWorld().spigot().playEffect(player.getEyeLocation(), Effect.SMOKE, 0, 64, 0, 0, 0, 0, 1, 64), 1L);
                                SoundUtil.playSound(p, SoundsPath.CallLiar.AXE_HALF_SWING);
                            }, 1L);
                        }
                    }
                    Bukkit.getScheduler().runTaskLater(instance, task, 10L);
                    if (fullSwing) {
                        Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
                            destroyAxe(player, stand.getId());
                        }, 20L);
                    }
                }
            }
        }.runTaskTimerAsynchronously(instance, 0L, 1L);
    }

    public void destroyAxe(Player player, int entityId) {
        if (entityId == -1) {
            if (!stands.containsKey(player)) return;
            entityId = stands.get(player).getId();
        }
        PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(entityId);
        IWrapperMethods.armorStands.remove(entityId);
        stands.remove(player);
        for (Player p : arena.getWorld().getPlayers()) {
            arena.sendDebugMsg("LiarCall.java - sending destroyAxe of player - " + player.getName() + " to viewer " + p.getName());
            p.removePotionEffect(PotionEffectType.BLINDNESS);
            p.removePotionEffect(PotionEffectType.SLOW);
            VersionWrapper.sendPacket(p, destroyPacket);
        }
    }
}