package com.kooy29.liarslounge.nms.v1_8_R3.animation;

import com.kooy29.liarslounge.api.animation.ITableCard;
import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.api.arena.IArenaManager;
import com.kooy29.liarslounge.nms.v1_8_R3.VersionWrapper;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.storage.yaml.SoundsPath;
import com.kooy29.liarslounge.utils.MsgUtil;
import com.kooy29.liarslounge.utils.SoundUtil;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TableCard implements ITableCard {
    private final float yawSpeed = 2.0f;
    private ArmorStandBuilder armorStand;
    private JavaPlugin instance;
    private float currentYaw = 0f;
    private int tickCount = 1;
    private IArena arena;

    public TableCard(JavaPlugin instance, IArena arena) {
        this.instance = instance;
        this.arena = arena;
    }

    public void forceStop() {
        tickCount = 95;
    }

    public void run(Runnable task) {
        tickCount = 1;
        Location loc = arena.getTableLocation();
        armorStand = new ArmorStandBuilder(loc.getWorld())
                .setGravity(false)
                .setInvisible(true)
                .setSmall(true)
                .setLocation(loc.getX(), loc.getY() - 0.5, loc.getZ(), 0F, 0F)
                .setHelmet(VersionWrapper.addCustomDataNMS(IArenaManager.getGameItem(arena.getTableCardType().name).clone(), ""))
                .setName(MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.TABLE_CARD_ANIMATION).replace("%table_card%", MsgUtil.getConfigMessage(MsgPath.Game.Cards.CARD.replace("%card%", arena.getTableCardType().name)))))
                .spawn();
        armorStand.setMotY(0);
        for (Player p : arena.getWorld().getPlayers()) SoundUtil.playSound(p, SoundsPath.TableCard.RISE);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (arena.isEnding()) {
                    cancel();
                    for (Player p : arena.getWorld().getPlayers()) {
                        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(armorStand.getId());
                        arena.sendDebugMsg("TableCard.java - sending destroy tick to viewer " + p.getName() + " Cause = ArenaEnding");
                        VersionWrapper.sendPacket(p, destroy);
                    }
                    tickCount = 1;
                    return;
                }
                if (tickCount >= 95) {
                    cancel();
                    for (Player p : arena.getWorld().getPlayers()) {
                        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(armorStand.getId());
                        arena.sendDebugMsg("TableCard.java - sending destroy tick to viewer " + p.getName());
                        VersionWrapper.sendPacket(p, destroy);
                    }
                    tickCount = 1;
                    task.run();
                    return;
                }

                if (tickCount <= 20) {
                    armorStand.setMotY((0.2 * tickCount));
                } else if (tickCount <= 65) {
//                    for (Player p : arena.getWorld().getPlayers()) {
//                        // play sound?
//                    }
                    armorStand.setMotY(0);
                } else if (tickCount <= 75) {
                    armorStand.setMotY(-(0.2 * (tickCount - 65)));
                }
                // Continuous rotation
                currentYaw = (currentYaw + yawSpeed) % 360;
                // Apply them
                armorStand.setYaw(currentYaw);

                PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook moveLookPacket =
                        new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(
                                armorStand.getId(),
                                (byte) 0,
                                (byte) armorStand.getMotY(),
                                (byte) 0,
                                (byte) armorStand.getYaw(),
                                (byte) 0,
                                false
                        );

                for (Player p : arena.getWorld().getPlayers()) {
                    arena.sendDebugMsg("TableCard.java - sending tick to viewer " + p.getName());
                    VersionWrapper.sendPacket(p, moveLookPacket);
                }
                tickCount++;
            }
        }.runTaskTimer(instance, 0L, 1L);
    }
}