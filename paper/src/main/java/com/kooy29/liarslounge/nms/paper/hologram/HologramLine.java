package com.kooy29.liarslounge.nms.paper.hologram;

import com.kooy29.liarslounge.api.hologram.IHologram;
import com.kooy29.liarslounge.api.hologram.IHologramLine;
import com.kooy29.liarslounge.nms.paper.VersionWrapper;
import com.kooy29.liarslounge.nms.paper.animation.ArmorStandBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;

public class HologramLine implements IHologramLine {

    public final ArmorStand entity;
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
        entity.setCustomName(Component.nullToEmpty(text));
        int position = hologram.getLines().indexOf(this);
        entity.setPosRaw(hologram.getLocation().getX(), hologram.getLocation().getY() + position * hologram.getGap(), hologram.getLocation().getZ());
        if (destroyed) return;

        ClientboundSetEntityDataPacket metadataPacket = new ClientboundSetEntityDataPacket(entity.getId(), entity.getEntityData().getNonDefaultValues());

        final var delta = new Vec3(0,0,0);
        final var positionMoveRotation = new PositionMoveRotation(entity.trackingPosition(), delta, 0, entity.getXRot());
        final Set<Relative> set = new HashSet<>();
        ClientboundTeleportEntityPacket teleportPacket = new ClientboundTeleportEntityPacket(entity.getId(), positionMoveRotation, set, false);

        VersionWrapper.sendPackets(hologram.getPlayer(), metadataPacket, teleportPacket);
    }

    @Override
    public void show() {
        destroyed = false;

        ClientboundAddEntityPacket packet = ArmorStandBuilder.packetPlayOutSpawnEntity(entity);
        VersionWrapper.sendPacket(hologram.getPlayer(), packet);

        if (!hologram.getLines().contains(this)) hologram.addLine(this);
        hologram.update();
    }

    @Override
    public void hide() {
        VersionWrapper.sendPacket(hologram.getPlayer(), new ClientboundRemoveEntitiesPacket(entity.getId()));
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