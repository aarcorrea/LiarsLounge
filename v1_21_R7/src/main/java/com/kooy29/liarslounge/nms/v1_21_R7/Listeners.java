package com.kooy29.liarslounge.nms.v1_21_R7;

import com.kooy29.liarslounge.api.nms.IWrapperMethods;
import net.minecraft.network.protocol.game.PacketPlayOutMount;
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Listeners implements Listener {

    IWrapperMethods wrapperMethods;

    public Listeners(IWrapperMethods wrapperMethods, JavaPlugin instance) {
        this.wrapperMethods = wrapperMethods;
    }

    @EventHandler
    public void onDismount(EntityDismountEvent e) {
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }
        if (wrapperMethods.canPlayerUnmount(player)) return;
        if (!(e.getDismounted() instanceof CraftEntity craftEntity)) {
            return;
        }
        e.setCancelled(true);

        net.minecraft.world.entity.Entity nmsEntity = craftEntity.getHandle();
        PacketPlayOutMount packet = new PacketPlayOutMount(nmsEntity);
        VersionWrapper.sendPacket(player, packet);
    }
}