package com.kooy29.liarslounge.listeners;

import com.kooy29.liarslounge.LiarsLounge;
import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.api.arena.IArenaManager;
import com.kooy29.liarslounge.storage.yaml.ConfigPath;
import com.kooy29.liarslounge.utils.ExtraUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    private IArenaManager arenaManager;

    public ChatListener(IArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if (e == null) return;
        if (ExtraUtil.getLobbyLocation().getWorld() == e.getPlayer().getWorld()) {
            e.getRecipients().clear();
            e.getRecipients().addAll(e.getPlayer().getWorld().getPlayers());
            return;
        }

        IArena arena = arenaManager.getArenaByPlayer(e.getPlayer());

        if (arena != null) {
            if (!LiarsLounge.getInstance().getConfiguration().getConfig().getBoolean(ConfigPath.ALLOW_CHAT_ARENA)) {
                e.setCancelled(true);
                return;
            }
            e.getRecipients().clear();
            if (!arena.isSpectator(e.getPlayer()))
                e.getRecipients().addAll(arena.getPlayers());
            e.getRecipients().addAll(arena.getSpectators());
        }
    }
}
