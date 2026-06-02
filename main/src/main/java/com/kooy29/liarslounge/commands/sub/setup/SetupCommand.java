package com.kooy29.liarslounge.commands.sub.setup;

import com.kooy29.liarslounge.LiarsLounge;
import com.kooy29.liarslounge.arena.ArenaSetupSession;
import com.kooy29.liarslounge.commands.SubCommand;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetupCommand implements SubCommand {

    SubCommand lobbyCommand;

    public SetupCommand(SubCommand lobbyCommand) {
        this.lobbyCommand = lobbyCommand;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length <= 1) {
            if (args[0].equalsIgnoreCase("lobby")) {
                lobbyCommand.execute(player, label, args);
            } else {
                MsgUtil.sendConfigMessageList(player, MsgPath.HelpCommands.Setup.NO_SESSION);
            }
            return;
        } else if (args.length != 2) {
            MsgUtil.sendConfigMessageList(player, MsgPath.HelpCommands.Setup.NO_SESSION);
            return;
        }

        if (arenaManager.isPlayerInArena(player)) {
            MsgUtil.sendConfigMessage(player, MsgPath.Error.IN_AN_ARENA);
            return;
        }

        if (ArenaSetupSession.getCurrentSetupSession() != null) {
            if (ArenaSetupSession.getPlayerSession(player) == null) {
                MsgUtil.sendConsoleMessage(MsgPath.Error.SETUP_JOIN);
            } else {
                MsgUtil.sendConfigMessage(player, MsgPath.Error.IN_SETUP_SESSION);
            }
            return;
        }

        String arenaName = args[0];
        if (arenaName.isEmpty()) {
            MsgUtil.sendConfigMessage(player, MsgPath.Error.ARENA_NAME_EMPTY);
            return;
        }


        World world = instance.getVersionWrapper().loadExistingWorld(args[1], "minecraft");
        if (world == null) {
                MsgUtil.sendMessage(player, MsgUtil.getConfigMessage(MsgPath.Error.WORLD_NOT_FOUND).replace("%world%", args[1]));
                return;
        }

        // Start the setup session
        if (LiarsLounge.getInstance().getArenaManager().arenaExists(arenaName)) {
            MsgUtil.sendConfigMessage(player, MsgPath.Error.REQUIRES_ARENA_DISABLED);
            return;
        }
        ArenaSetupSession arenaSetupSession = new ArenaSetupSession(arenaName, world, player);
        player.sendMessage(MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Success.Setup.STARTED).replace("%arena%", arenaName)));
        MsgUtil.sendSetupHelpMsg(player, arenaSetupSession);
        player.teleport(arenaSetupSession.getWorld().getSpawnLocation());
        player.setGameMode(GameMode.CREATIVE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setAllowFlight(true);
        arenaSetupSession.getChairLocations().forEach(h -> h.getHologram().show());
        arenaSetupSession.getActionItemsLocation().forEach(h -> h.getHologram().show());
    }
}
