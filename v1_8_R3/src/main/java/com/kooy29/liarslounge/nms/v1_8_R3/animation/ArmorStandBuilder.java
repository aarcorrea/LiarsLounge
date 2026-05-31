package com.kooy29.liarslounge.nms.v1_8_R3.animation;

import com.kooy29.liarslounge.nms.v1_8_R3.VersionWrapper;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ArmorStandBuilder {

    private EntityArmorStand armorStand;
    private ItemStack helmet;
    private World world;

    public ArmorStandBuilder(World world) {
        this.world = world;
        armorStand = new EntityArmorStand(((CraftWorld) world).getHandle());
    }

    public double getMotY() {
        return armorStand.motY;
    }

    public void setMotY(double motY) {
        armorStand.motY = motY;
    }

    public float getYaw() {
        return armorStand.yaw;
    }

    public void setYaw(float yaw) {
        armorStand.yaw = yaw;
    }

    public ArmorStandBuilder setLocation(Location location) {
        this.armorStand.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        return this;
    }

    public ArmorStandBuilder setLocation(double x, double y, double z, float yaw, float pitch) {
        this.armorStand.setLocation(x, y, z, yaw, pitch);
        return this;
    }

    public ArmorStandBuilder setHelmet(ItemStack helmet) {
        this.helmet = helmet;
        return this;
    }

    public ArmorStandBuilder setName(String name) {
        this.armorStand.setCustomName(name);
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

    public ArmorStandBuilder setGravity(boolean gravity) {
        this.armorStand.setGravity(gravity);
        return this;
    }

    public ArmorStandBuilder spawn() {
        PacketPlayOutSpawnEntityLiving spawnPacket =
                new PacketPlayOutSpawnEntityLiving(armorStand);

        for (Player player : world.getPlayers()) {
            VersionWrapper.sendPacket(player, spawnPacket);
        }

        if (helmet != null) {
            PacketPlayOutEntityEquipment equipmentPacket =
                    new PacketPlayOutEntityEquipment(
                            armorStand.getId(),
                            4,
                            CraftItemStack.asNMSCopy(helmet)
                    );

            for (Player player : world.getPlayers()) {
                VersionWrapper.sendPacket(player, equipmentPacket);
            }
        }
        return this;
    }

    public ArmorStandBuilder spawn(Player player) {
        PacketPlayOutSpawnEntityLiving spawnPacket =
                new PacketPlayOutSpawnEntityLiving(armorStand);

        VersionWrapper.sendPacket(player, spawnPacket);

        if (helmet != null) {
            PacketPlayOutEntityEquipment equipmentPacket =
                    new PacketPlayOutEntityEquipment(
                            armorStand.getId(),
                            4,
                            CraftItemStack.asNMSCopy(helmet)
                    );
            VersionWrapper.sendPacket(player, equipmentPacket);
        }
        return this;
    }

    public EntityArmorStand getArmorStand() {
        return armorStand;
    }

    public int getId() {
        return armorStand.getId();
    }
}