package com.kooy29.liarslounge.nms.v1_21_R7.animation;

import com.kooy29.liarslounge.api.animation.ITableCard;
import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.api.arena.IArenaManager;
import com.kooy29.liarslounge.nms.v1_21_R7.VersionWrapper;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.storage.yaml.SoundsPath;
import com.kooy29.liarslounge.utils.MsgUtil;
import com.kooy29.liarslounge.utils.SoundUtil;
import net.minecraft.network.protocol.game.PacketPlayOutEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TableCard implements ITableCard {
    private final float yawSpeed = 2.0f;
    private ArmorStandBuilder armorStand;
    private JavaPlugin instance;
    private float currentYaw = 0f;
    private double currentY = 0;
    private int tickCount = 1;
    private IArena arena;

    public TableCard(JavaPlugin instance, IArena arena) {
        this.instance = instance;
        this.arena = arena;
    }

    @Override
    public void forceStop() {
        tickCount = 95;
    }

    @Override
    public void run(Runnable task) {
        tickCount = 1;
        currentY = 0;
        Location loc = arena.getTableLocation();
        armorStand = new ArmorStandBuilder(loc.getWorld())
                .setGravity(false)
                .setInvisible(true)
                .setSmall(true)
                .setLocation(loc.getX(), loc.getY() - 0.5, loc.getZ(), loc.getYaw(), loc.getPitch())
                .setName(MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.TABLE_CARD_ANIMATION).replace("%table_card%", MsgUtil.getConfigMessage(MsgPath.Game.Cards.CARD.replace("%card%", arena.getTableCardType().name)))))
                .setHelmet(VersionWrapper.addCustomDataNMS(IArenaManager.getGameItem(arena.getTableCardType().name).clone(), ""))
                .spawn();
        armorStand.setYaw(0);
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
                    currentY = 0;
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
                    currentY = 0;
                    task.run();
                    return;
                }
                double previousY = currentY;
                if (tickCount <= 10) {
                    // rise: 0 to 1.0 blocks in 10 ticks
                    currentY = tickCount / 10.0;
                } else if (tickCount <= 65) {
                    // Hover and rotate at 1.0 block
                    currentY = 1.0;
                } else if (tickCount <= 85) {
                    // slow descent: 1.0 to 0 blocks over 20 ticks
                    double progress = (tickCount - 65) / 20.0;
                    currentY = 1.0 - progress;
                } else {
                    currentY = 0;
                }
                // Continuous rotation
                double deltaY = currentY - previousY;
                currentYaw = (currentYaw + yawSpeed) % 360;
                armorStand.setYaw(currentYaw);
                short protocolDeltaY = (short) Math.clamp(deltaY * 4096,
                        Short.MIN_VALUE, Short.MAX_VALUE);
                PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook moveLookPacket =
                        new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(
                                armorStand.getId(),
                                (short) 0,
                                protocolDeltaY,
                                (short) 0,
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