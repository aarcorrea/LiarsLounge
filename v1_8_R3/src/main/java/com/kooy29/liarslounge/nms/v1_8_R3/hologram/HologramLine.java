package com.kooy29.liarslounge.nms.v1_8_R3.hologram;

import com.kooy29.liarslounge.api.hologram.IHologram;
import com.kooy29.liarslounge.api.hologram.IHologramLine;
import com.kooy29.liarslounge.nms.v1_8_R3.VersionWrapper;
import com.kooy29.liarslounge.nms.v1_8_R3.animation.ArmorStandBuilder;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;

public class HologramLine implements IHologramLine {

    private final EntityArmorStand entity;
    private String text;
    private IHologram hologram;
    private boolean destroyed = false;

    public HologramLine(String text, Hologram hologram) {
        this.text = text;
        this.hologram = hologram;
        Location loc = hologram.getLocation();
        this.entity = new ArmorStandBuilder(loc.getWorld())
                .setInvisible(true)
                .setGravity(false)
                .setName(text)
                .setLocation(loc.getX(), loc.getY() + hologram.size() * hologram.getGap(), loc.getZ(), loc.getYaw(), loc.getPitch())
                .spawn(hologram.getPlayer())
                .getArmorStand();
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
        Location loc = hologram.getLocation();
        int position = hologram.getLines().indexOf(this);
        entity.setLocation(loc.getX(), loc.getY() + position * hologram.getGap(), loc.getZ(), loc.getYaw(), loc.getPitch());
        if (destroyed) return;

        PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport(entity);
        PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(entity.getId(), entity.getDataWatcher(), true);

        VersionWrapper.sendPackets(hologram.getPlayer(),packet, metadataPacket);
    }

    @Override
    public void show() {
        destroyed = false;
        VersionWrapper.sendPacket(hologram.getPlayer(), new PacketPlayOutSpawnEntityLiving(entity));
        if (!hologram.getLines().contains(this)) {
            hologram.addLine(this);
        }
        hologram.update();
    }

    @Override
    public void hide() {
        VersionWrapper.sendPacket(hologram.getPlayer(), new PacketPlayOutEntityDestroy(entity.getId()));
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
}