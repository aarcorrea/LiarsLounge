package com.kooy29.liarslounge.commands.sub.arena;

import com.kooy29.liarslounge.api.arena.GameState;
import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.arena.ArenaSetupSession;
import com.kooy29.liarslounge.commands.SubCommand;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.utils.ExtraUtil;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpectateCommand implements SubCommand {
    @Override
    public String getName() {
        return "spectate";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {

        if (ExtraUtil.notAPlayer(sender)) return;

        Player p = (Player) sender;

        if (!ExtraUtil.hasPermission(p, "liarslounge.spectate", "liarslounge.admin")) return;

        if (ArenaSetupSession.getPlayerSession(p) != null) {
            MsgUtil.sendConfigMessage(p, MsgPath.Error.IN_SETUP_SESSION);
            return;
        }

        if (args.length < 1) {
            MsgUtil.sendConfigMessage(p, MsgPath.Usage.SPECTATE);
            return;
        }

        String arenaName = args[0];
        IArena arena = arenaManager.getArena(arenaName);

        if (arena == null)
            MsgUtil.sendConfigMessage(p, MsgPath.Error.ARENA_NOT_FOUND);
        else {

            if (arenaManager.isPlayerInArena(p)) {
                MsgUtil.sendConfigMessage(p, MsgPath.Error.IN_AN_ARENA);
                return;
            }

            if (arena.getGameState() != GameState.PLAYING) {
                MsgUtil.sendConfigMessage(p, MsgPath.Error.ARENA_NOT_IN_GAME);
                return;
            }

            arena.makeSpectator(p, true, true);
            p.sendMessage(MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.SPECTATING)));
        }
    }
}
