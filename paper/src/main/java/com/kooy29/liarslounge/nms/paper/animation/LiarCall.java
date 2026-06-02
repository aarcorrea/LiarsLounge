package com.kooy29.liarslounge.nms.paper.animation;

import com.kooy29.liarslounge.api.animation.ILiarCall;
import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.api.nms.IWrapperMethods;
import com.kooy29.liarslounge.nms.paper.VersionWrapper;
import com.kooy29.liarslounge.storage.yaml.SoundsPath;
import com.kooy29.liarslounge.utils.SoundUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Rotations;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
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
    HashMap<Player, ArmorStand> stands = new HashMap<>();
    Location nonStrikeLoc = null;
    private final JavaPlugin instance;

    // TODO: if player instantly joins & leaves liarcall animation may not complete, all ver

    public LiarCall(JavaPlugin instance, IArena arena) {
        this.instance = instance;
        this.arena = arena;
    }

    @Override
    public void clearOldData() {
        for (Player player : new ArrayList<>(stands.keySet())) {
            ArmorStand stand = stands.get(player);
            if (stand != null) {
                ClientboundRemoveEntitiesPacket destroyPacket = new ClientboundRemoveEntitiesPacket(stand.getId());
                for (Player p : arena.getWorld().getPlayers()) {
                    VersionWrapper.sendPacket(p, destroyPacket);
                }
                stands.remove(player);
                IWrapperMethods.armorStands.remove(stand.getId());
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


        ArmorStand stand;
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
        stand.setRightArmPose(new Rotations(0f, 0f, 90f));
        IWrapperMethods.armorStands.add(stand.getId());
        for (Player p : actionItemLoc.getWorld().getPlayers()) {
            VersionWrapper.sendPackets(p,
                    ArmorStandBuilder.packetPlayOutSpawnEntity(stand),
                    new ClientboundSetEquipmentPacket(stand.getId(), List.of(Pair.of(EquipmentSlot.MAINHAND, CraftItemStack.asNMSCopy(axe)))),
                    new ClientboundSetEntityDataPacket(stand.getId(), stand.getEntityData().getNonDefaultValues()));
        }
    }

    @Override
    public void moveToPlayer(Player player, Runnable task) {
        ArmorStand stand = stands.get(player);
        if (stand == null) return;
        nonStrikeLoc = new Location(null, stand.getX(), stand.getY(), stand.getZ(), stand.getYRot(), stand.getXRot());

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
        ClientboundMoveEntityPacket.Rot packetHeadRot =
                new ClientboundMoveEntityPacket.Rot(
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
            final Location currentLoc = startLoc.clone();

            @Override
            public void run() {
                if (tick >= steps) {
                    cancel();
                    if (task != null) Bukkit.getScheduler().runTask(instance, task);
                    return;
                }

                currentLoc.add(stepVec);

                // Always set yaw so it stays correct
                stand.absSnapTo(currentLoc.getX(), currentLoc.getY(), currentLoc.getZ(), yawF, 0f);

                // Send teleport with correct yaw
//                PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(stand);
                var pmr = new PositionMoveRotation(
                        stand.position(),
                        Vec3.ZERO,
                        yawF,
                        0f
                );
                ClientboundTeleportEntityPacket packet =
                        new ClientboundTeleportEntityPacket(
                                stand.getId(),
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
        ArmorStand stand = stands.get(player);
        if (stand == null) return;
        stand.absSnapTo(nonStrikeLoc.getX(), nonStrikeLoc.getY(), nonStrikeLoc.getZ(),
                nonStrikeLoc.getYaw(), nonStrikeLoc.getPitch());
//        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(stand);
        //                PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(stand);
        var pmr = new PositionMoveRotation(
                stand.position(),
                Vec3.ZERO,
                nonStrikeLoc.getYaw(),
                nonStrikeLoc.getPitch()
        );
        ClientboundTeleportEntityPacket packetTeleport =
                new ClientboundTeleportEntityPacket(
                        stand.getId(),
                        pmr,
                        Set.of(), // absolute teleport
                        false
                );
        byte yawByte = (byte) (nonStrikeLoc.getYaw() * 256 / 360);
        ClientboundMoveEntityPacket.Rot packetHeadRot =
                new ClientboundMoveEntityPacket.Rot(
                        stand.getId(),
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
        ArmorStand stand = stands.get(player);
        new BukkitRunnable() {
            boolean reloading = true;
            float bodyYawDegrees = stand.getYRot();

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
                    ClientboundMoveEntityPacket.Rot lookPacket =
                            new ClientboundMoveEntityPacket.Rot(stand.getId(), yawByte, (byte) 0, true);
                    for (Player p : arena.getWorld().getPlayers()) {
                        arena.sendDebugMsg("LiarCall.java - sending playAxeSwing of player - " + player.getName() + " to viewer " + p.getName());
                        VersionWrapper.sendPacket(p, lookPacket);
                        if (stand.getYRot() - 60 == bodyYawDegrees)
                            SoundUtil.playSound(p, SoundsPath.CallLiar.AXE_RELOAD);
                    }
                    if (stand.getYRot() - 80 >= bodyYawDegrees) {
                        reloading = false;
                    }
                } else {
                    cancel();
                    if (fullSwing)
                        bodyYawDegrees += 180f; // swing the axe kill
                    else
                        bodyYawDegrees += 85f; // swing the axe just till neck
                    byte yawByte = (byte) (bodyYawDegrees * 256 / 360);
                    ClientboundMoveEntityPacket.Rot lookPacket =
                            new ClientboundMoveEntityPacket.Rot(stand.getId(), yawByte, (byte) 0, true);
                    for (Player p : arena.getWorld().getPlayers()) {
                        arena.sendDebugMsg("LiarCall.java - sending playAxeSwing of player - " + player.getName() + " to viewer " + p.getName() + ", fullSwing: - " + fullSwing);
                        VersionWrapper.sendPacket(p, lookPacket);
                        if (fullSwing) {
                            Bukkit.getScheduler().runTaskLater(instance, () -> SoundUtil.playSound(p, SoundsPath.CallLiar.AXE_FULL_SWING), 1L);
                        } else {
                            Bukkit.getScheduler().runTaskLater(instance, () -> {
                                Bukkit.getScheduler().runTaskLater(instance, () -> p.getWorld().spawnParticle(Particle.SMOKE, player.getEyeLocation(), 2), 1L);
                                SoundUtil.playSound(p, SoundsPath.CallLiar.AXE_HALF_SWING);
                            }, 1L);
                        }
                    }
                    if (fullSwing) {
                        Bukkit.getScheduler().runTaskLater(instance, task, 10L);
                        Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
                            destroyAxe(player, stand.getId());
                        }, 40L);
                    } else {
                        Bukkit.getScheduler().runTaskLater(instance, task, 40L);
                    }
                }
            }
        }.runTaskTimerAsynchronously(instance, 0L, 1L);
    }

    @Override
    public void destroyAxe(Player player, int entityId) {
        if (entityId == -1) {
            if (!stands.containsKey(player)) return;
            entityId = stands.get(player).getId();
        }
        ClientboundRemoveEntitiesPacket destroyPacket = new ClientboundRemoveEntitiesPacket(entityId);
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