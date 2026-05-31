package com.kooy29.liarslounge.commands.sub.arena;

import com.kooy29.liarslounge.api.arena.GameState;
import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.commands.SubCommand;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.utils.ExtraUtil;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QStartCommand implements SubCommand {
    @Override
    public String getName() {
        return "qstart";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {

        if (ExtraUtil.notAPlayer(sender)) return;

        if (!ExtraUtil.hasPermission(sender, "liarslounge.admin")) return;

        Player p = (Player) sender;
        IArena arena = arenaManager.getArenaByPlayer(p);
        if (arena == null) {
            MsgUtil.sendConfigMessage(sender, MsgPath.Error.NOT_IN_ARENA);
            return;
        }

        if (arena.getGameState() == GameState.PLAYING) {
            MsgUtil.sendConfigMessage(sender, MsgPath.Error.ARENA_NOT_IN_WAITING);
            return;
        }

        if (arena.getGameState() == GameState.STARTING)
            arena.setQuickStart(0);
        else
            arena.start(true);
    }
}
