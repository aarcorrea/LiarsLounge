package com.kooy29.liarslounge.commands.sub.setup;

import com.kooy29.liarslounge.arena.ArenaSetupSession;
import com.kooy29.liarslounge.commands.SubCommand;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.utils.ExtraUtil;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SetupGroup implements SubCommand {

    private final Map<String, SubCommand> subCommands = new HashMap<>();
    private final SubCommand baseCommand;

    public SetupGroup() {

        SubCommand lobbyCommand = new LobbyCommand();
        // Base command to setup an arena
        baseCommand = new SetupCommand(lobbyCommand);

        // Registering all sub-commands
        register(new ChairCommand());
        register(new JoinCommand());
        register(new ActionItemCommand());
        register(new EndCommand());
        register(new SaveCommand());
        register(new TableCommand());
        register(new WaitingCommand());
        register(lobbyCommand);

    }

    public void register(SubCommand cmd) {
        subCommands.put(cmd.getName(), cmd);
    }

    @Override
    public String getName() {
        return "setup";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (ExtraUtil.notAPlayer(sender)) return;
        Player player = (Player) sender;
        ArenaSetupSession arenaSetupSession = ArenaSetupSession.getPlayerSession(player);
        if (!ExtraUtil.hasPermission(player, "liarslounge.setup", "liarslounge.admin")) return;
        if (args.length == 0) {
            if (arenaSetupSession != null)
                MsgUtil.sendSetupHelpMsg(player, arenaSetupSession);
            else
                MsgUtil.sendConfigMessageList(player, MsgPath.HelpCommands.Setup.NO_SESSION);
            return;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);
        if (subCommand != null) {
            if (arenaSetupSession != null) subCommand.execute(player, label, Arrays.copyOfRange(args, 1, args.length));
            else baseCommand.execute(player, label, args);
        } else {
            if (arenaSetupSession == null) {
                baseCommand.execute(player, label, args);
                return;
            }
            MsgUtil.sendConfigMessage(player, MsgPath.Usage.SETUP);
        }
    }
}
