package com.kooy29.liarslounge.nms.v1_21_R7.hologram;

import com.kooy29.liarslounge.api.hologram.IHologram;
import com.kooy29.liarslounge.api.hologram.IHologramLine;
import com.kooy29.liarslounge.nms.v1_21_R7.VersionWrapper;
import com.kooy29.liarslounge.nms.v1_21_R7.animation.ArmorStandBuilder;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R7.util.CraftChatMessage;

import java.util.HashSet;
import java.util.Set;

public class HologramLine implements IHologramLine {

    public final EntityArmorStand entity;
    private String text;
    private IHologram hologram;
    private boolean destroyed = false;

    public HologramLine(String text, Hologram hologram) {
        this.text = text;
        this.hologram = hologram;
        Location loc = hologram.getLocation();
        entity = new ArmorStandBuilder(loc.getWorld())
                .setInvisible(true)
                .setGravity(false)
                .setMarker(true)
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
        entity.b(CraftChatMessage.fromStringOrNull(text)); // setCustomName
        int position = hologram.getLines().indexOf(this);
        entity.n(hologram.getLocation().getX(), hologram.getLocation().getY() + position * hologram.getGap(), hologram.getLocation().getZ()); //setPosRaw
        if (destroyed) return;

        PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(entity.aA(), entity.aD().c()); // getId(), getEntityData()

        final var delta = new Vec3D(0,0,0);
        final var positionMoveRotation = new PositionMoveRotation(entity.dJ(), delta, 0, entity.ee());  // trackingPosition(), , , getXRot()
        final Set<Relative> set = new HashSet<>();
        PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(entity.aA(), positionMoveRotation, set, false); // getId()

        VersionWrapper.sendPackets(hologram.getPlayer(), metadataPacket, teleportPacket);
    }

    @Override
    public void show() {
        destroyed = false;

        PacketPlayOutSpawnEntity packet = ArmorStandBuilder.packetPlayOutSpawnEntity(entity);
        VersionWrapper.sendPacket(hologram.getPlayer(), packet);

        if (!hologram.getLines().contains(this)) hologram.addLine(this);
        hologram.update();
    }

    @Override
    public void hide() {
        VersionWrapper.sendPacket(hologram.getPlayer(), new PacketPlayOutEntityDestroy(entity.aA()));
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