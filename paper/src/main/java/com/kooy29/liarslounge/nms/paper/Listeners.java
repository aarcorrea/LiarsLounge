package com.kooy29.liarslounge.nms.paper;

import com.kooy29.liarslounge.api.nms.IWrapperMethods;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Listeners implements Listener {

    IWrapperMethods wrapperMethods;

    public Listeners(IWrapperMethods wrapperMethods, JavaPlugin instance) {
        this.wrapperMethods = wrapperMethods;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(PlayerConnectionValidateLoginEvent event) {
        if (!(event.getConnection() instanceof PlayerConfigurationConnection connection)) {
            return; // player isn't exiting configuration phase, skip
        }
        CustomConnection.setChannel(event.getConnection(), connection.getProfile().getId());
    }

    @EventHandler
    public void onDismount(EntityDismountEvent e) {
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }
        if (wrapperMethods.canPlayerUnmount(player)) return;
        e.setCancelled(true);

        if (!(e.getDismounted() instanceof CraftEntity craftEntity)) {
            return;
        }

        Entity nmsEntity = craftEntity.getHandle();
        ClientboundSetPassengersPacket packet = new ClientboundSetPassengersPacket(nmsEntity);
        VersionWrapper.sendPacket(player, packet);
    }
}