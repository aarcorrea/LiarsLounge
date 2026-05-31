package com.kooy29.liarslounge.listeners;

import com.kooy29.liarslounge.api.arena.IArenaManager;
import com.kooy29.liarslounge.storage.MapBuild;
import com.kooy29.liarslounge.utils.ExtraUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlocksListener implements Listener {

    private IArenaManager arenaManager;

    public BlocksListener(IArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (MapBuild.canBuild(e.getPlayer())) return;
        if (e.getPlayer().getWorld() == ExtraUtil.getLobbyLocation().getWorld() || arenaManager.isPlayerInArena(e.getPlayer()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (MapBuild.canBuild(e.getPlayer())) return;
        if (e.getPlayer().getWorld() == ExtraUtil.getLobbyLocation().getWorld() || arenaManager.isPlayerInArena(e.getPlayer()))
            e.setCancelled(true);
    }
}