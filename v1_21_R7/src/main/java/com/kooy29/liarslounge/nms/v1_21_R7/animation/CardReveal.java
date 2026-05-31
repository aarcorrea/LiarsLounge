package com.kooy29.liarslounge.nms.v1_21_R7.animation;

import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.nms.v1_21_R7.VersionWrapper;
import com.kooy29.liarslounge.storage.yaml.SoundsPath;
import com.kooy29.liarslounge.utils.SoundUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R7.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class CardReveal implements com.kooy29.liarslounge.api.animation.ICardReveal {

    private final IArena arena;
    private final int riseTicks = 15;
    private final int displayTicks = 60;
    private final int dropTicks = 10;
    private final double startYOffset = -0.4;
    private final double finalYOffset = 0.3;
    private final double totalDrop = 0.7;
    private final Location tableCenter;
    private int tick = 0;
    private JavaPlugin instance;

    public CardReveal(JavaPlugin instance, IArena arena) {
        this.arena = arena;
        this.instance = instance;
        this.tableCenter = arena.getTableLocation().clone().add(0, 0.04, 0);
        ;
    }

    @Override
    public void revealTo(Player viewer, ItemStack[] cards, Player accuserPly, boolean isTruth) {
        if (viewer == null || cards == null || cards.length == 0) return;

        arena.sendDebugMsg("CardReveal - revealTo() for " + viewer.getName());

        double[] baseOffsets = (cards.length == 1) ? new double[]{0} :
                (cards.length == 2) ? new double[]{-0.25, 0.25} :
                new double[]{-0.5, 0.0, 0.5};

        List<EntityArmorStand> stands = new ArrayList<>();
        List<Location> baseLocations = new ArrayList<>();

        // Spawn armor stands at precomputed locations
        for (int i = 0; i < cards.length; i++) {
            Location target = computeTargetForViewer(viewer, tableCenter, baseOffsets[i], cards.length);
            baseLocations.add(target);

            EntityArmorStand stand = new ArmorStandBuilder(tableCenter.getWorld())
                    .setInvisible(true)
                    .setSmall(true)
                    .setGravity(false)
                    .setMarker(true)
                    .setLocation(target.getX(), tableCenter.getY() + startYOffset, target.getZ(), 0f, 0f)
                    .setHelmet(cards[i])
                    .spawn(viewer)
                    .getArmorStand();

            stands.add(stand);
        }

        tick = 0;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!viewer.isOnline()) {
                    cancel();
                    destroyAll(viewer, stands);
                    return;
                }

                double y;
                if (tick <= riseTicks) {
                    double progress = (double) tick / riseTicks;
                    y = tableCenter.getY() + startYOffset + (finalYOffset - startYOffset) * progress;
                    teleportAll(viewer, stands, baseLocations, y);
                    if (tick == riseTicks) {
                        SoundUtil.playSound(viewer, SoundsPath.CardReveal.RISE);
                        if (isTruth && accuserPly == viewer)
                            SoundUtil.playSound(accuserPly, SoundsPath.CardReveal.ACCUSER_RIGHT);
                        else if (accuserPly == viewer)
                            SoundUtil.playSound(accuserPly, SoundsPath.CardReveal.ACCUSER_WRONG);
                    }
                } else if (tick <= riseTicks + displayTicks) {
                    y = tableCenter.getY() + finalYOffset;
                    teleportAll(viewer, stands, baseLocations, y);
                } else if (tick <= riseTicks + displayTicks + dropTicks) {
                    double progress = (double) (tick - riseTicks - displayTicks) / dropTicks;
                    y = tableCenter.getY() + finalYOffset - totalDrop * progress;
                    teleportAll(viewer, stands, baseLocations, y);
                } else {
                    cancel();
                    destroyAll(viewer, stands);
                }

                tick++;
            }
        }.runTaskTimer(instance, 0L, 1L);
    }

    @Override
    public void revealToSpectators(Collection<Player> spectators, ItemStack[] cards, Player accuserPly, boolean isTruth) {
        if (spectators == null || spectators.isEmpty() || cards == null || cards.length == 0) return;

        arena.sendDebugMsg("CardReveal - revealToSpectators() for " + spectators.size() + " viewers");

        // use accuser's perspective
        double[] baseOffsets = (cards.length == 1) ? new double[]{0} :
                (cards.length == 2) ? new double[]{-0.25, 0.25} :
                new double[]{-0.5, 0.0, 0.5};

        List<Location> baseLocations = new ArrayList<>();
        List<EntityArmorStand> stands = new ArrayList<>();

        for (int i = 0; i < cards.length; i++) {
            Location target = computeTargetForViewer(accuserPly, tableCenter, baseOffsets[i], cards.length);
            baseLocations.add(target);

            EntityArmorStand stand = new ArmorStandBuilder(tableCenter.getWorld())
                    .setInvisible(true)
                    .setSmall(true)
                    .setGravity(false)
                    .setMarker(true)
                    .setLocation(target.getX(), tableCenter.getY() + startYOffset, target.getZ(), 0f, 0f)
                    .getArmorStand();

            for (Player spec : spectators) {
                if (!spec.isOnline()) continue;
                VersionWrapper.sendPackets(spec,
                        ArmorStandBuilder.packetPlayOutSpawnEntity(stand),
                        new PacketPlayOutEntityMetadata(stand.aA(), stand.aD().b()),
                        new PacketPlayOutEntityEquipment(stand.aA(), List.of(Pair.of(EnumItemSlot.f, CraftItemStack.asNMSCopy(cards[i])))));
            }
            stands.add(stand);
        }

        tick = 0;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (spectators.stream().noneMatch(Player::isOnline)) {
                    cancel();
                    return;
                }

                double y;
                if (tick <= riseTicks) {
                    double progress = (double) tick / riseTicks;
                    y = tableCenter.getY() + startYOffset + (finalYOffset - startYOffset) * progress;
                } else if (tick <= riseTicks + displayTicks) {
                    y = tableCenter.getY() + finalYOffset;
                } else if (tick <= riseTicks + displayTicks + dropTicks) {
                    double progress = (double) (tick - riseTicks - displayTicks) / dropTicks;
                    y = tableCenter.getY() + finalYOffset - totalDrop * progress;
                } else {
                    cancel();
                    int[] ids = stands.stream().mapToInt(EntityArmorStand::aA).toArray();
                    for (Player spec : spectators) {
                        if (!spec.isOnline()) continue;
                        VersionWrapper.sendPacket(spec, new PacketPlayOutEntityDestroy(ids));
                    }
                    return;
                }

                // Apply same Y to all spectators
                for (int i = 0; i < stands.size(); i++) {
                    Location base = baseLocations.get(i);
                    EntityArmorStand stand = stands.get(i);
                    stand.a(base.getX(), y, base.getZ(), 0f, 0f);
                    var pmr = new PositionMoveRotation(
                            stand.dI(),
                            Vec3D.c,
                            0f,
                            0f
                    );
                    PacketPlayOutEntityTeleport packet =
                            new PacketPlayOutEntityTeleport(
                                    stand.aA(),
                                    pmr,
                                    Set.of(), // absolute teleport
                                    false
                            );
                    for (Player spec : spectators) {
                        if (spec.isOnline()) VersionWrapper.sendPacket(spec, packet);
                    }
                }

                tick++;
            }
        }.runTaskTimer(instance, 0L, 1L);
    }

    private void teleportAll(Player viewer, List<EntityArmorStand> stands, List<Location> baseLocations, double y) {
        for (int i = 0; i < stands.size(); i++) {
            Location base = baseLocations.get(i);
            EntityArmorStand stand = stands.get(i);
            stand.a(base.getX(), y, base.getZ(), 0f, 0f);
            var pmr = new PositionMoveRotation(
                    stand.dI(),
                    Vec3D.c,
                    0f,
                    0f
            );
            PacketPlayOutEntityTeleport packet =
                    new PacketPlayOutEntityTeleport(
                            stand.aA(),
                            pmr,
                            Set.of(), // absolute teleport
                            false
                    );
            VersionWrapper.sendPacket(viewer, packet);
        }
    }

    private void destroyAll(Player viewer, List<EntityArmorStand> stands) {
        int[] ids = stands.stream().mapToInt(EntityArmorStand::aA).toArray();
        VersionWrapper.sendPacket(viewer, new PacketPlayOutEntityDestroy(ids));
        stands.clear();
    }

    @Override
    public void forceStop() {
        tick = riseTicks + displayTicks + dropTicks + 100;
    }

    private Location computeTargetForViewer(Player viewer, Location tableCenter, double baseOffsetValue, int amount) {
        double x = tableCenter.getX();
        double z = tableCenter.getZ();

        if (amount == 1) {
            return new Location(tableCenter.getWorld(), x, tableCenter.getY() + finalYOffset, z);
        }

        double dx = viewer.getLocation().getX() - tableCenter.getX();
        double dz = viewer.getLocation().getZ() - tableCenter.getZ();
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

        return new Location(tableCenter.getWorld(), x + ox, tableCenter.getY() + finalYOffset, z + oz);
    }
}
