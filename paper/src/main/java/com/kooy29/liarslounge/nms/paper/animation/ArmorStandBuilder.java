package com.kooy29.liarslounge.nms.paper.animation;

import com.kooy29.liarslounge.nms.paper.VersionWrapper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ArmorStandBuilder {

    private ArmorStand armorStand;
    private ItemStack helmet;
    private World world;

    public ArmorStandBuilder(World world) {
        this.world = world;
        armorStand = new ArmorStand(((CraftWorld) world).getHandle(), 0, 0, 0);
    }

    public static ClientboundAddEntityPacket packetPlayOutSpawnEntity(Entity nmsEntity) {
        return new ClientboundAddEntityPacket(
                nmsEntity.getId(),
                nmsEntity.getUUID(), // getUUID
                nmsEntity.getX(),
                nmsEntity.getY(),
                nmsEntity.getZ(),
                nmsEntity.getXRot(),
                nmsEntity.getBukkitYaw(),
                nmsEntity.getType(),
                0,
                nmsEntity.getDeltaMovement(),
                nmsEntity.getYHeadRot()
        );
    }

    public float getYaw() {
        return armorStand.getYRot();
    }

    public void setYaw(float yaw) {
        armorStand.setYRot(yaw);
    }

    public ArmorStandBuilder setLocation(Location location) {
        this.armorStand.absSnapTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        return this;
    }

    public ArmorStandBuilder setLocation(double x, double y, double z, float yaw, float pitch) {
        this.armorStand.absSnapTo(x, y, z, yaw, pitch);
        return this;
    }

    public ArmorStandBuilder setHelmet(ItemStack helmet) {
        this.helmet = helmet;
        return this;
    }

    public ArmorStandBuilder setName(String name) {
        this.armorStand.setCustomName(Component.nullToEmpty(name));
        this.armorStand.setCustomNameVisible(true);
        return this;
    }

    public ArmorStandBuilder setSmall(boolean small) {
        this.armorStand.setSmall(small);
        return this;
    }

    public ArmorStandBuilder setInvisible(boolean invisible) {
        this.armorStand.setInvisible(invisible);
        return this;
    }

    public ArmorStandBuilder setMarker(boolean marker) {
        // no collision & smaller hitbox results in custom name appearing below helmet
        this.armorStand.setMarker(marker);
        return this;
    }

    public ArmorStandBuilder setGravity(boolean gravity) {
        this.armorStand.setNoGravity(!gravity);
        return this;
    }

    public ArmorStandBuilder spawn() {
        ClientboundAddEntityPacket spawnPacket = packetPlayOutSpawnEntity(armorStand);
        ClientboundSetEntityDataPacket metadata = new ClientboundSetEntityDataPacket(armorStand.getId(), armorStand.getEntityData().packDirty());

        for (Player player : world.getPlayers()) {
            VersionWrapper.sendPackets(player, spawnPacket, metadata);
        }

        if (helmet != null) {
            ClientboundSetEquipmentPacket equipmentPacket =
                    new ClientboundSetEquipmentPacket(armorStand.getId(), List.of(Pair.of(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(helmet))));

            for (Player player : world.getPlayers()) {
                VersionWrapper.sendPacket(player, equipmentPacket);
            }
        }
        return this;
    }

    public ArmorStandBuilder spawn(Player player) {
        ClientboundAddEntityPacket spawnPacket = packetPlayOutSpawnEntity(armorStand);
        ClientboundSetEntityDataPacket metadata = new ClientboundSetEntityDataPacket(armorStand.getId(), armorStand.getEntityData().packDirty());

        VersionWrapper.sendPackets(player, spawnPacket, metadata);


        if (helmet != null) {
            ClientboundSetEquipmentPacket equipmentPacket =
                    new ClientboundSetEquipmentPacket(armorStand.getId(), List.of(Pair.of(EquipmentSlot.HEAD, CraftItemStack.asNMSCopy(helmet))));

            VersionWrapper.sendPacket(player, equipmentPacket);

        }
        return this;
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public int getId() {
        return armorStand.getId();
    }
}