package com.kooy29.liarslounge.nms.v1_21_R7.animation;

import com.kooy29.liarslounge.nms.v1_21_R7.VersionWrapper;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R7.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R7.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_21_R7.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ArmorStandBuilder {

    private EntityArmorStand armorStand;
    private ItemStack helmet;
    private World world;

    public ArmorStandBuilder(World world) {
        this.world = world;
        armorStand = new EntityArmorStand(((CraftWorld) world).getHandle(), 0, 0, 0);
    }

    public static PacketPlayOutSpawnEntity packetPlayOutSpawnEntity(Entity nmsEntity) {
        return new PacketPlayOutSpawnEntity(
                nmsEntity.aA(),
                nmsEntity.cY(), // getUUID
                nmsEntity.dP(),
                nmsEntity.dR(),
                nmsEntity.dV(),
                nmsEntity.ee(),
                nmsEntity.getBukkitYaw(),
                nmsEntity.ay(),
                0,
                nmsEntity.dN(),
                nmsEntity.cS()
        );
    }

    public float getYaw() {
        return armorStand.ec();
    }

    public void setYaw(float yaw) {
        armorStand.v(yaw);
    }

    public ArmorStandBuilder setLocation(Location location) {
        this.armorStand.a(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        return this;
    }

    public ArmorStandBuilder setLocation(double x, double y, double z, float yaw, float pitch) {
        this.armorStand.a(x, y, z, yaw, pitch);
        return this;
    }

    public ArmorStandBuilder setHelmet(ItemStack helmet) {
        this.helmet = helmet;
        return this;
    }

    public ArmorStandBuilder setName(String name) {
        this.armorStand.b(CraftChatMessage.fromStringOrNull(name));
        this.armorStand.p(true);
        return this;
    }

    public ArmorStandBuilder setSmall(boolean small) {
        this.armorStand.u(small);
        return this;
    }

    public ArmorStandBuilder setInvisible(boolean invisible) {
        this.armorStand.l(invisible);
        return this;
    }

    public ArmorStandBuilder setMarker(boolean marker) {
        // no collision & smaller hitbox results in custom name appearing below helmet
        this.armorStand.v(marker);
        return this;
    }

    public ArmorStandBuilder setGravity(boolean gravity) {
        // nms uses setnogravity
        this.armorStand.g(!gravity);
        return this;
    }

    public ArmorStandBuilder spawn() {
        PacketPlayOutSpawnEntity spawnPacket = packetPlayOutSpawnEntity(armorStand);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(armorStand.aA(), armorStand.aD().b());

        for (Player player : world.getPlayers()) {
            VersionWrapper.sendPackets(player, spawnPacket, metadata);
        }

        if (helmet != null) {
            PacketPlayOutEntityEquipment equipmentPacket =
                    new PacketPlayOutEntityEquipment(armorStand.aA(), List.of(Pair.of(EnumItemSlot.f, CraftItemStack.asNMSCopy(helmet))));

            for (Player player : world.getPlayers()) {
                VersionWrapper.sendPacket(player, equipmentPacket);
            }
        }
        return this;
    }

    public ArmorStandBuilder spawn(Player player) {
        PacketPlayOutSpawnEntity spawnPacket = packetPlayOutSpawnEntity(armorStand);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(armorStand.aA(), armorStand.aD().b());

        VersionWrapper.sendPackets(player, spawnPacket, metadata);

        if (helmet != null) {
            PacketPlayOutEntityEquipment equipmentPacket =
                    new PacketPlayOutEntityEquipment(armorStand.aA(), List.of(Pair.of(EnumItemSlot.f, CraftItemStack.asNMSCopy(helmet))));

            VersionWrapper.sendPacket(player, equipmentPacket);
        }
        return this;
    }

    public EntityArmorStand getArmorStand() {
        return armorStand;
    }

    public int getId() {
        return armorStand.aA();
    }
}