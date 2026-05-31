package com.kooy29.liarslounge.listeners;

import com.kooy29.liarslounge.api.arena.IArenaManager;
import com.kooy29.liarslounge.api.storage.IConfiguration;
import com.kooy29.liarslounge.storage.yaml.ConfigPath;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CmdListener implements Listener {

    IArenaManager arenaManager;
    FileConfiguration configuration;

    public CmdListener(IArenaManager arenaManager, IConfiguration configuration) {
        this.arenaManager = arenaManager;
        this.configuration = configuration.getConfig();
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        String cmdRaw = e.getMessage().replaceFirst("/", "");
        String[] cmd = cmdRaw.split(" ");
        if (cmd.length == 0) return;
        if (arenaManager.isPlayerInArena(p) && !p.hasPermission("liarslounge.allow_commands")) {
            if (!configuration.getStringList(ConfigPath.ALLOWED_COMMANDS).contains(cmd[0])) {
                MsgUtil.sendConfigMessage(p, MsgPath.Error.CMD_NOT_ALLOWED);
                e.setCancelled(true);
            }
        }
    }
}
