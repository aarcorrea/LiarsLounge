package com.kooy29.liarslounge.nms.paper_v1_21_R7;

import com.kooy29.liarslounge.nms.v1_21_R7.CustomConnection;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class Listeners implements Listener {


    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(PlayerConnectionValidateLoginEvent event) {
        if (!(event.getConnection() instanceof PlayerConfigurationConnection connection)) {
            return; // player isn't exiting configuration phase, skip
        }
        CustomConnection.setChannel(event.getConnection(), connection.getProfile().getId());
    }

    public void onQuit(PlayerQuitEvent event) {
        CustomConnection.removeChannel(event.getPlayer().getUniqueId());
    }
}