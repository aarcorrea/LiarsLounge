package com.kooy29.liarslounge.listeners;

import com.kooy29.liarslounge.LiarsLounge;
import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.api.arena.IArenaManager;
import com.kooy29.liarslounge.api.storage.IPlayerStats;
import com.kooy29.liarslounge.arena.ArenaSetupSession;
import com.kooy29.liarslounge.storage.PlayerStats;
import com.kooy29.liarslounge.utils.ExtraUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

public class PlayerListener implements Listener {

    private IArenaManager arenaManager;

    public PlayerListener(IArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        e.setJoinMessage(null);
        IArena.GamePlayer gp = new IArena.GamePlayer(p, null, 6, null);
        IArena.gamePlayers.put(p, gp);
        Bukkit.getScheduler().runTaskAsynchronously(LiarsLounge.getInstance(), () -> {
            IPlayerStats ps = LiarsLounge.getInstance().getDb().fetchStats(e.getPlayer().getUniqueId());
            gp.stats = Objects.requireNonNullElseGet(ps, () ->
                    new PlayerStats(-1, p.getName(), p.getUniqueId(), null, null, 0, 0, 0, 0, 0)
            );
        });
        LiarsLounge.getInstance().getCustomConnectionWrapper().injectPlayer(p);
        ExtraUtil.destroySeat(p);
        if (ExtraUtil.isLobbyLocation()) {
            p.setMaxHealth(20);
            p.setHealth(20);
            p.setFoodLevel(20);
            p.setExp(0);
            p.setLevel(0);
            p.teleport(ExtraUtil.getLobbyLocation());
            p.getInventory().clear();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        e.setQuitMessage(null);
        LiarsLounge.getInstance().getCustomConnectionWrapper().removePlayer(p);
        IArena arena = arenaManager.getArenaByPlayer(p);
        if (arena != null) {
            arena.removePlayer(p);
            arena.removeSpectator(p);
        }
        IArena.gamePlayers.remove(p);
        ArenaSetupSession.removePlayerFromSession(p);
    }

    @EventHandler
    public void onDamageListener(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            if (ExtraUtil.lobbyProtection(player.getWorld()) || arenaManager.isPlayerInArena(player))
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onRegeneration(EntityRegainHealthEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            IArena arena = arenaManager.getArenaByPlayer(player);
            if (arena != null && arena.getPlayers().contains(player)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            if (ExtraUtil.lobbyProtection(player.getWorld()) || arenaManager.isPlayerInArena(player))
                e.setCancelled(true);
        }
    }
}