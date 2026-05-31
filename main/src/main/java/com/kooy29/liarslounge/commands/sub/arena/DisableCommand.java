package com.kooy29.liarslounge.commands.sub.arena;

import com.kooy29.liarslounge.api.arena.GameState;
import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.commands.SubCommand;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.utils.ExtraUtil;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class DisableCommand implements SubCommand {
    @Override
    public String getName() {
        return "disable";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!ExtraUtil.hasPermission(sender, "liarslounge.admin", "liarslounge.arena.disable")) return;

        if (args.length == 0) {
            MsgUtil.sendConfigMessage(sender, MsgPath.Usage.DISABLE_ARENA);
            return;
        }

        String arenaName = args[0];

        IArena arena = arenaManager.getArena(arenaName);

        if (arena == null) {
            MsgUtil.sendConfigMessage(sender, MsgPath.Error.ARENA_NOT_FOUND);
            return;
        }

        if (arena.getGameState() == GameState.PLAYING) {
            MsgUtil.sendConfigMessage(sender, MsgPath.Error.ARENA_IN_GAME);
            return;
        }

        new ArrayList<>(arena.getPlayers()).forEach(player -> {
            MsgUtil.sendConfigMessage(sender, MsgPath.Error.ADMIN_DISABLED_ARENA);
            arena.removePlayer(player);
            player.teleport(ExtraUtil.getLobbyLocation());
        });

        new ArrayList<>(arena.getSpectators()).forEach(player -> {
            MsgUtil.sendConfigMessage(sender, MsgPath.Error.ADMIN_DISABLED_ARENA);
            arena.removeSpectator(player);
            player.teleport(ExtraUtil.getLobbyLocation());
        });

        arenaManager.unregisterArena(arenaName);
        sender.sendMessage(MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Success.Arena.DISABLE).replace("%arena%", arenaName)));
    }
}
