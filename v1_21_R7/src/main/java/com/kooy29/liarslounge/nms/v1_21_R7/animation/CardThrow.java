package com.kooy29.liarslounge.nms.v1_21_R7.animation;

import com.kooy29.liarslounge.api.animation.ICardThrow;
import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.api.arena.IArenaManager;
import com.kooy29.liarslounge.nms.v1_21_R7.VersionWrapper;
import com.kooy29.liarslounge.storage.yaml.SoundsPath;
import com.kooy29.liarslounge.utils.SoundUtil;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public class CardThrow implements ICardThrow {
    private final long delayBetweenThrows = 12L;
    private final int travelTicks = 24;
    private final long stayTicks = 0L;
    private final int closeTicks = 10;
    private final double startYOffset = 0.2;
    private final double tableLandingYOffset = 0.3;
    private final double dropDistance = 0.7;

    private final Map<UUID, List<EntityArmorStand>> viewerEntityIds = new HashMap<>();
    private final Map<Integer, Location> entityPositions = new HashMap<>();
    private final List<Integer> scheduledTaskIds = new ArrayList<>();
    private final Map<Integer, Location> throwerCardTargets = new HashMap<>();
    private BukkitTask closeTask = null;
    private IArena arena;
    private JavaPlugin instance;

    public CardThrow(JavaPlugin instance, IArena arena) {
        this.instance = instance;
        this.arena = arena;
    }

    public static Color modernDustColor(int r, int g, int b) {
        return Color.fromRGB(r, g, b);
    }

    @Override
    public void clearOldData() {
        forceStop();
        viewerEntityIds.clear();
        entityPositions.clear();
        scheduledTaskIds.clear();
        throwerCardTargets.clear();
    }

    @Override
    public void throwCards(Collection<Player> players, Collection<Player> spectators, Player thrower, int amount, Runnable endTask) {
        forceStop(); // clear any previous animation

        if (players == null || players.isEmpty() || spectators == null || thrower == null) return;
        arena.sendDebugMsg("CardThrow.java - Called throw cards. Thrower = " + thrower.getName() + ", Amount = " + amount);
        amount = Math.clamp(amount, 1, 3);

        Location tableLocation = arena.getTableLocation().clone().add(0, 0.04, 0);

        double[] baseOffsets = (amount == 1) ? new double[]{0} :
                (amount == 2) ? new double[]{-0.25, 0.25} :
                new double[]{-0.5, 0.0, 0.5};

        for (int i = 0; i < amount; i++) {
            final int idx = i;
            int finalAmount = amount;
            int taskId = Bukkit.getScheduler().runTaskLater(instance, () -> {

                // Compute thrower perspective once per card index
                Location throwerTarget = computeTargetForViewer(thrower, tableLocation, baseOffsets[idx], finalAmount);
                throwerCardTargets.put(idx, throwerTarget);

                // Players
                for (Player viewer : players) {
                    if (!viewer.isOnline()) continue;
                    Location target = viewer.equals(thrower)
                            ? throwerTarget
                            : computeTargetForViewer(viewer, tableLocation, baseOffsets[idx], finalAmount);
                    spawnCardForViewer(viewer, target, thrower);
                }

                // Spectators (reuse thrower perspective)
                for (Player spectator : spectators) {
                    if (!spectator.isOnline()) continue;
                    spawnCardForViewer(spectator, throwerTarget, thrower);
                }

                // Immediately clear the stored thrower target for this card index
                throwerCardTargets.remove(idx);

                // Schedule close animation for all viewers
                if (idx == finalAmount - 1) {
                    long totalDelay = delayBetweenThrows * (finalAmount - 1) + travelTicks + stayTicks;
                    if (finalAmount == 1) totalDelay += 20;
                    int closeId = Bukkit.getScheduler().runTaskLater(instance, () -> {
                        startCloseForViewers(endTask);
                    }, totalDelay).getTaskId();
                    scheduledTaskIds.add(closeId);
                }
            }, delayBetweenThrows * i).getTaskId();

            scheduledTaskIds.add(taskId);
        }
    }

    private void spawnCardForViewer(Player viewer, Location target, Player thrower) {
        arena.sendDebugMsg("CardThrow.java - Spawning card for viewer" + viewer.getName() + " Thrower = " + thrower.getName());
        Location start = thrower.getLocation().clone().add(0, startYOffset, 0);

        EntityArmorStand stand = new ArmorStandBuilder(target.getWorld())
                .setInvisible(true)
                .setSmall(true)
                .setGravity(false)
                .setMarker(true)
                .setLocation(start.getX(), start.getY(), start.getZ(), 0f, 0f)
                .setHelmet(IArenaManager.getGameItem("hidden"))
                .spawn(viewer)
                .getArmorStand();

        SoundUtil.playSound(viewer, SoundsPath.Card.THROW);

        int entityId = stand.aA();
        viewerEntityIds.computeIfAbsent(viewer.getUniqueId(), k -> new ArrayList<>()).add(stand);
        entityPositions.put(entityId, start.clone());

        Vector delta = target.toVector().subtract(start.toVector()).multiply(1.0 / travelTicks);
        int trailLength = 5;

        for (int t = 0; t < travelTicks; t++) {
            int moveTaskId = Bukkit.getScheduler().runTaskLater(instance, () -> {
                Location pos = entityPositions.get(entityId);
                if (pos == null) return;

                pos.add(delta);
                for (int i = 1; i <= trailLength; i++) {
                    float trailProgress = (float) i / trailLength;

                    int r = 255;
                    int g = (int) (170 + (1 - trailProgress) * 50);
                    int b = 255;

                    Location particlePos = pos.clone()
                            .subtract(delta.clone().multiply(i))
                            .add(0, 1, 0);

                    Particle.DustOptions dustOptions =
                            new Particle.DustOptions(
                                    modernDustColor(r, g, b),
                                    1.25f // slightly larger
                            );

                    particlePos.getWorld().spawnParticle(
                            Particle.DUST,
                            particlePos,
                            1,
                            0, 0, 0,
                            0,
                            dustOptions
                    );
                }

                sendTeleport(viewer, stand, pos);

            }, t).getTaskId();
            scheduledTaskIds.add(moveTaskId);
        }


    }

    private Location computeTargetForViewer(Player viewer, Location tableCenter, double baseOffsetValue, int amount) {
        arena.sendDebugMsg("CardThrow.java - Computing location for viewer" + viewer.getName());
        Location center = tableCenter.clone();
        double x = center.getX();
        double z = center.getZ();
        double y = center.getY() + tableLandingYOffset;

        if (amount == 1) {
            return new Location(center.getWorld(), x, y, z);
        }

        double dx = viewer.getLocation().getX() - center.getX();
        double dz = viewer.getLocation().getZ() - center.getZ();
        double angle = Math.toDegrees(Math.atan2(-dx, dz));
        angle = (angle + 360.0) % 360.0;

        double ox = 0.0, oz = 0.0;
        if (angle >= 45 && angle < 135) { // EAST
            oz = baseOffsetValue;
        } else if (angle >= 135 && angle < 225) { // SOUTH
            ox = -baseOffsetValue;
        } else if (angle >= 225 && angle < 315) { // WEST
            oz = -baseOffsetValue;
        } else { // NORTH
            ox = baseOffsetValue;
        }

        return new Location(center.getWorld(), x + ox, y, z + oz);
    }

    private void startCloseForViewers(Runnable endTask) {
        if (closeTask != null) {
            closeTask.cancel();
            closeTask = null;
        }

        closeTask = new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                double perTick = dropDistance / closeTicks;
                Iterator<Map.Entry<UUID, List<EntityArmorStand>>> it = viewerEntityIds.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<UUID, List<EntityArmorStand>> entry = it.next();
                    Player viewer = Bukkit.getPlayer(entry.getKey());
                    if (viewer == null || !viewer.isOnline()) {
                        it.remove();
                        continue;
                    }
                    for (EntityArmorStand stand : entry.getValue()) {
                        Location pos = entityPositions.get(stand.aA());
                        if (pos == null) continue;
                        pos.subtract(0, perTick, 0);
                        sendTeleport(viewer, stand, pos);
                    }
                }

                tick++;
                if (tick > closeTicks) {
                    cancel();
                    for (Map.Entry<UUID, List<EntityArmorStand>> entry : viewerEntityIds.entrySet()) {
                        Player viewer = Bukkit.getPlayer(entry.getKey());
                        if (viewer == null || !viewer.isOnline()) continue;
                        if (!entry.getValue().isEmpty()) {
                            int[] arr = entry.getValue().stream().mapToInt(Entity::aA).toArray();
                            PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(arr);
                            VersionWrapper.sendPacket(viewer, destroy);
                            arena.sendDebugMsg("CardThrow.java - Destroying for viewer " + viewer.getName() + " Cause = AnimationEnd");
                        }
                    }
                    viewerEntityIds.clear();
                    entityPositions.clear();
                    if (endTask != null) {
                        try {
                            endTask.run();
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }.runTaskTimer(instance, 0L, 1L);
    }

    @Override
    public void forceStop() {
        for (int id : scheduledTaskIds) {
            Bukkit.getScheduler().cancelTask(id);
        }
        scheduledTaskIds.clear();

        if (closeTask != null) {
            closeTask.cancel();
            closeTask = null;
        }

        for (Map.Entry<UUID, List<EntityArmorStand>> e : viewerEntityIds.entrySet()) {
            Player viewer = Bukkit.getPlayer(e.getKey());
            if (viewer == null || !viewer.isOnline()) continue;
            if (!e.getValue().isEmpty()) {
                int[] arr = e.getValue().stream().mapToInt(Entity::aA).toArray();
                PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(arr);
                VersionWrapper.sendPacket(viewer, destroy);
                arena.sendDebugMsg("CardThrow.java - Destroying for viewer " + viewer.getName() + " Cause = ForceStop");
            }
        }

        viewerEntityIds.clear();
        entityPositions.clear();
        throwerCardTargets.clear();
    }

    private void sendTeleport(Player viewer, EntityArmorStand entity, Location loc) {
        arena.sendDebugMsg("CardThrow.java - Teleporting for viewer " + viewer.getName());

        entity.a(loc.getX(),
                loc.getY(),
                loc.getZ(), 0f, 0f);
        var pmr = new PositionMoveRotation(
                entity.dJ(),
                new Vec3D(0, 0, 0),
                0f,
                0f
        );
        PacketPlayOutEntityTeleport packet =
                new PacketPlayOutEntityTeleport(
                        entity.aA(),
                        pmr,
                        Set.of(),
                        false
                );

        VersionWrapper.sendPacket(viewer, packet);
    }
}
