package com.kooy29.liarslounge.nms.v1_8_R3;

import com.kooy29.liarslounge.api.nms.IWrapperMethods;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutAttachEntity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.event.entity.EntityDismountEvent;

public class Listeners implements Listener {

    IWrapperMethods wrapperMethods;
    JavaPlugin instance;

    public Listeners(IWrapperMethods wrapperMethods, JavaPlugin instance) {
        this.wrapperMethods = wrapperMethods;
        this.instance = instance;
    }

    @EventHandler
    public void onDismount(EntityDismountEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (wrapperMethods.canPlayerUnmount((Player) e.getEntity())) return;
        remountClientSide((Player) e.getEntity(), e.getDismounted());
    }

    public void remountClientSide(Player player, org.bukkit.entity.Entity vehicle) {
        EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        net.minecraft.server.v1_8_R3.Entity nmsVehicle =
                ((CraftEntity) vehicle).getHandle();

        PacketPlayOutAttachEntity packet =
                new PacketPlayOutAttachEntity(0, nmsPlayer, nmsVehicle);

        Bukkit.getScheduler().runTaskLater(instance, () -> nmsPlayer.playerConnection.sendPacket(packet), 1L);
    }
}
