package com.kooy29.liarslounge.commands.sub.arena;

import com.kooy29.liarslounge.api.arena.GameState;
import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.arena.ArenaSetupSession;
import com.kooy29.liarslounge.commands.SubCommand;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.utils.ExtraUtil;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class JoinCommand implements SubCommand {
    @Override
    public String getName() {
        return "join";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {

        if (ExtraUtil.notAPlayer(sender)) return;

        Player p = (Player) sender;

        if (!ExtraUtil.hasPermission(p, "liarslounge.join", "liarslounge.admin")) return;

        if (ArenaSetupSession.getPlayerSession(p) != null) {
            MsgUtil.sendConfigMessage(p, MsgPath.Error.IN_SETUP_SESSION);
            return;
        }

        if (args.length < 1) {
            MsgUtil.sendConfigMessage(p, MsgPath.Usage.JOIN);
            return;
        }

        String arenaName = args[0];
        IArena arena;
        if (arenaName.equalsIgnoreCase("random")) {
            arena = arenaManager.getArenas().stream()
                    .filter(a -> (a.getGameState() != GameState.PLAYING)
                            && a.getPlayers().size() < a.getChairLocations().size())
                    .findAny()
                    .orElse(null);

            if (arena == null) {
                MsgUtil.sendConfigMessage(p, MsgPath.Error.ARENA_NOT_FOUND);
                return;
            }
        } else {
            Set<IArena> groupArenas = arenaManager.getArenaGroupMap().get(arenaName);
            if (groupArenas != null && !groupArenas.isEmpty()) {
                // Find an available arena in the group
                arena = groupArenas.stream()
                        .filter(a -> (a.getGameState() == GameState.WAITING || a.getGameState() == GameState.STARTING)
                                && a.getPlayers().size() < a.getChairLocations().size())
                        .findAny()
                        .orElse(null);

                if (arena == null) {
                    MsgUtil.sendConfigMessage(p, MsgPath.Error.ARENA_NOT_FOUND);
                    return;
                }
            } else {
                arena = arenaManager.getArena(arenaName);
            }
        }

        if (arena == null)
            MsgUtil.sendConfigMessage(p, MsgPath.Error.ARENA_NOT_FOUND);
        else {

            if (arenaManager.isPlayerInArena(p)) {
                MsgUtil.sendConfigMessage(p, MsgPath.Error.IN_AN_ARENA);
                return;
            }

            if (arena.getPlayers().size() >= arena.getChairLocations().size()) {
                MsgUtil.sendConfigMessage(p, MsgPath.Error.ARENA_IS_FULL);
                return;
            }

            if (arena.getGameState() == GameState.PLAYING) {
                MsgUtil.sendConfigMessage(p, MsgPath.Error.ARENA_IN_GAME);
                return;
            }

            p.setGameMode(GameMode.ADVENTURE);
            p.teleport(arena.getWaitingLocation());
            arena.addPlayer(p);
            p.sendMessage(MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Success.Arena.JOIN).replace("%arena%", arena.getName())));
            if (instance.isPartyHook() && instance.getPartyHook().isLeader(p)) {
                instance.getPartyHook().getMembers(p).forEach(member -> {
                    execute(member, label, new String[]{arenaName});
                });
            }
        }
    }
}