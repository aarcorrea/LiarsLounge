package com.kooy29.liarslounge.nms.v1_8_R3.hologram;

import com.kooy29.liarslounge.api.hologram.IHologram;
import com.kooy29.liarslounge.api.hologram.IHologramLine;
import com.kooy29.liarslounge.nms.v1_8_R3.animation.ArmorStandBuilder;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;

public class HologramLine implements IHologramLine {

    private final EntityArmorStand entity;
    private String text;
    private IHologram hologram;
    private boolean destroyed = false;

    public HologramLine(String text, Hologram hologram) {
        this.text = text;
        this.hologram = hologram;
        Location location = hologram.getLocation();
        this.entity = new ArmorStandBuilder(location.getWorld())
                .setInvisible(true)
                .setGravity(false)
                .setName(text).getArmorStand();
        updatePosition();
        show();
    }

    @Override
    public String text() {
        return text;
    }

    @Override
    public void text(String text) {
        this.text = text;
        refresh();
    }

    @Override
    public void text(String text, boolean refresh) {
        this.text = text;
        if (refresh) {
            refresh();
        }
    }

    @Override
    public IHologram hologram() {
        return hologram;
    }

    @Override
    public void hologram(IHologram hologram) {
        this.hologram = hologram;
    }

    @Override
    public void refresh() {
        entity.setCustomName(text);
        updatePosition();
        if (destroyed) {
            return;
        }
        sendPacket(new PacketPlayOutEntityTeleport(entity));
        sendPacket(new PacketPlayOutEntityMetadata(
                entity.getId(),
                entity.getDataWatcher(),
                true
        ));
    }

    @Override
    public void show() {
        destroyed = false;

        sendPacket(new PacketPlayOutSpawnEntityLiving(entity));
        sendPacket(new PacketPlayOutEntityMetadata(
                entity.getId(),
                entity.getDataWatcher(),
                true
        ));
        sendPacket(new PacketPlayOutEntityTeleport(entity));

        if (!hologram.getLines().contains(this)) {
            hologram.addLine(this);
        }
        hologram.update();
    }

    @Override
    public void hide() {
        sendPacket(new PacketPlayOutEntityDestroy(entity.getId()));
    }

    @Override
    public void destroy() {
        destroyed = true;
        hide();
        hologram.removeLine(this);
    }

    @Override
    public boolean destroyed() {
        return destroyed;
    }

    private void updatePosition() {
        Location location = hologram.getLocation();
        int position = hologram.getLines().indexOf(this);
        entity.setLocation(
                location.getX(),
                location.getY() + (position * hologram.getGap()),
                location.getZ(),
                location.getYaw(),
                location.getPitch()
        );
    }

    private void sendPacket(Object packet) {
        ((CraftPlayer) hologram.getPlayer())
                .getHandle()
                .playerConnection
                .sendPacket((net.minecraft.server.v1_8_R3.Packet<?>) packet);
    }
}