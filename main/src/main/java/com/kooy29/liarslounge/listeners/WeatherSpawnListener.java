package com.kooy29.liarslounge.listeners;

import com.kooy29.liarslounge.api.arena.IArenaManager;
import com.kooy29.liarslounge.utils.ExtraUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WeatherSpawnListener implements Listener {

    IArenaManager arenaManager;

    public WeatherSpawnListener(IArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e) {
        if ((ExtraUtil.lobbyProtection(e.getWorld()) || arenaManager.isArenaByWorld(e.getWorld().getName())) && e.toWeatherState())
            e.setCancelled(true);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;
        if (ExtraUtil.lobbyProtection(e.getLocation().getWorld()) || arenaManager.isArenaByWorld(e.getLocation().getWorld().getName())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onArmorManipulate(PlayerArmorStandManipulateEvent e) {
        if (ExtraUtil.hasPermissionNP(e.getPlayer(), "liarslounge.build")) return;
        if (ExtraUtil.lobbyProtection(e.getPlayer().getWorld()) || arenaManager.isArenaByWorld(e.getPlayer().getWorld().getName()))
            e.setCancelled(true);
    }
}
