package com.kooy29.liarslounge.nms.v1_21_R7.animation;

import com.kooy29.liarslounge.api.animation.ILiarCall;
import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.api.nms.IWrapperMethods;
import com.kooy29.liarslounge.nms.v1_21_R7.VersionWrapper;
import com.kooy29.liarslounge.storage.yaml.SoundsPath;
import com.kooy29.liarslounge.utils.SoundUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Vector3f;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_21_R7.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class LiarCall implements ILiarCall {

    IArena arena;
    HashMap<Player, EntityArmorStand> stands = new HashMap<>();
    Location nonStrikeLoc = null;
    private JavaPlugin instance;

    // TODO: if player instantly joins & leaves liarcall animation may not complete, all ver

    public LiarCall(JavaPlugin instance, IArena arena) {
        this.instance = instance;
        this.arena = arena;
    }

    @Override
    public void clearOldData() {
        for (Player player : new ArrayList<>(stands.keySet())) {
            EntityArmorStand stand = stands.get(player);
            if (stand != null) {
                PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(stand.aA());
                for (Player p : arena.getWorld().getPlayers()) {
                    VersionWrapper.sendPacket(p, destroyPacket);
                }
                stands.remove(player);
                IWrapperMethods.armorStands.remove(stand.aA());
            }
        }
    }

    @Override
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
                    .setMarker(true)
                    .setLocation(newLoc.getX(), newLoc.getY() - 0.4, newLoc.getZ(), newLoc.getYaw() - 85, 0f)
                    .getArmorStand();
        }


        ItemStack axe = new ItemStack(Material.GOLDEN_AXE);
        stands.put(player, stand);
        stand.d(new Vector3f(0f, 0f, 90f));
        IWrapperMethods.armorStands.add(stand.aA());
        for (Player p : actionItemLoc.getWorld().getPlayers()) {
            VersionWrapper.sendPackets(p,
                    ArmorStandBuilder.packetPlayOutSpawnEntity(stand),
                    new PacketPlayOutEntityEquipment(stand.aA(), List.of(Pair.of(EnumItemSlot.a, CraftItemStack.asNMSCopy(axe)))),
                    new PacketPlayOutEntityMetadata(stand.aA(), stand.aD().c()));
        }
    }

    @Override
    public void moveToPlayer(Player player, Runnable task) {
        EntityArmorStand stand = stands.get(player);
        if (stand == null) return;
        nonStrikeLoc = new Location(null, stand.dP(), stand.dR(), stand.dV(), stand.ec(), stand.ee());

        Location startLoc = stand.getBukkitEntity().getLocation();
        Location playerLoc = player.getLocation();

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
                        stand.aA(),
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
                stand.a(currentLoc.getX(), currentLoc.getY(), currentLoc.getZ(), yawF, 0f);

                // Send teleport with correct yaw
//                PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(stand);
                var pmr = new PositionMoveRotation(
                        stand.dI(),
                        Vec3D.c,
                        yawF,
                        0f
                );
                PacketPlayOutEntityTeleport packet =
                        new PacketPlayOutEntityTeleport(
                                stand.aA(),
                                pmr,
                                Set.of(), // absolute teleport
                                false
                        );
                for (Player p : new ArrayList<>(playerLoc.getWorld().getPlayers())) {
                    arena.sendDebugMsg("LiarCall.java - sending teleport axe of player - " + player.getName() + " to viewer " + p.getName());
                    VersionWrapper.sendPacket(p, packet);
                }

                tick++;
            }
        }.runTaskTimerAsynchronously(instance, 10L, 1L);
    }

    @Override
    public void moveBackToLoc(Player player) {
        EntityArmorStand stand = stands.get(player);
        if (stand == null) return;
        stand.a(nonStrikeLoc.getX(), nonStrikeLoc.getY(), nonStrikeLoc.getZ(),
                nonStrikeLoc.getYaw(), nonStrikeLoc.getPitch());
//        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(stand);
        //                PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(stand);
        var pmr = new PositionMoveRotation(
                stand.dI(),
                Vec3D.c,
                nonStrikeLoc.getYaw(),
                nonStrikeLoc.getPitch()
        );
        PacketPlayOutEntityTeleport packetTeleport =
                new PacketPlayOutEntityTeleport(
                        stand.aA(),
                        pmr,
                        Set.of(), // absolute teleport
                        false
                );
        byte yawByte = (byte) (nonStrikeLoc.getYaw() * 256 / 360);
        PacketPlayOutEntity.PacketPlayOutEntityLook packetHeadRot =
                new PacketPlayOutEntity.PacketPlayOutEntityLook(
                        stand.aA(),
                        yawByte,
                        (byte) 0,
                        true
                );
        for (Player p : new ArrayList<>(player.getLocation().getWorld().getPlayers())) {
            arena.sendDebugMsg("LiarCall.java - sending moveBackToLoc of player - " + player.getName() + " to viewer " + p.getName());
            VersionWrapper.sendPackets(p, packetTeleport, packetHeadRot);
        }
    }


    @Override
    public void playAxeSwing(Player player, boolean fullSwing, Runnable task) {
        arena.sendDebugMsg("Called playAxeSwing!! - " + player.getName());
        EntityArmorStand stand = stands.get(player);
        new BukkitRunnable() {
            boolean reloading = true;
            float bodyYawDegrees = stand.ec();

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    destroyAxe(player, stand.aA());
                    Bukkit.getScheduler().runTaskLater(instance, task, 10L);
                    return;
                }
                if (reloading) {
                    bodyYawDegrees -= 1f;
                    byte yawByte = (byte) (bodyYawDegrees * 256 / 360);
                    PacketPlayOutEntity.PacketPlayOutEntityLook lookPacket =
                            new PacketPlayOutEntity.PacketPlayOutEntityLook(stand.aA(), yawByte, (byte) 0, true);
                    for (Player p : arena.getWorld().getPlayers()) {
                        arena.sendDebugMsg("LiarCall.java - sending playAxeSwing of player - " + player.getName() + " to viewer " + p.getName());
                        VersionWrapper.sendPacket(p, lookPacket);
                        if (stand.ec() - 60 == bodyYawDegrees)
                            SoundUtil.playSound(p, SoundsPath.CallLiar.AXE_RELOAD);
                    }
                    if (stand.ec() - 80 >= bodyYawDegrees) {
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
                            new PacketPlayOutEntity.PacketPlayOutEntityLook(stand.aA(), yawByte, (byte) 0, true);
                    for (Player p : arena.getWorld().getPlayers()) {
                        arena.sendDebugMsg("LiarCall.java - sending playAxeSwing of player - " + player.getName() + " to viewer " + p.getName() + ", fullSwing: - " + fullSwing);
                        VersionWrapper.sendPacket(p, lookPacket);
                        if (fullSwing) {
                            Bukkit.getScheduler().runTaskLater(instance, () -> SoundUtil.playSound(p, SoundsPath.CallLiar.AXE_FULL_SWING), 1L);
                        } else {
                            Bukkit.getScheduler().runTaskLater(instance, () -> {
                                Bukkit.getScheduler().runTaskLater(instance, () -> p.getWorld().spawnParticle(Particle.SMOKE, player.getEyeLocation(), 1), 1L);
                                SoundUtil.playSound(p, SoundsPath.CallLiar.AXE_HALF_SWING);
                            }, 1L);
                        }
                    }
                    Bukkit.getScheduler().runTaskLater(instance, task, 10L);
                    if (fullSwing) {
                        Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
                            destroyAxe(player, stand.aA());
                        }, 20L);
                    }
                }
            }
        }.runTaskTimerAsynchronously(instance, 0L, 1L);
    }

    @Override
    public void destroyAxe(Player player, int entityId) {
        if (entityId == -1) {
            if (!stands.containsKey(player)) return;
            entityId = stands.get(player).aA();
        }
        PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(entityId);
        IWrapperMethods.armorStands.remove(entityId);
        stands.remove(player);
        for (Player p : arena.getWorld().getPlayers()) {
            arena.sendDebugMsg("LiarCall.java - sending destroyAxe of player - " + player.getName() + " to viewer " + p.getName());
            Bukkit.getScheduler().runTask(instance, () -> {
                if (p.isOnline()) {
                    p.removePotionEffect(PotionEffectType.BLINDNESS);
                    p.removePotionEffect(PotionEffectType.SLOWNESS);
                }
            });
            VersionWrapper.sendPacket(p, destroyPacket);
        }
    }
}