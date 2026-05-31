package com.kooy29.liarslounge.commands.sub.arena;

import com.kooy29.liarslounge.commands.SubCommand;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ArenaGroup implements SubCommand {

    public static SubCommand joinCommand;
    public static SubCommand spectateCommand;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public ArenaGroup() {
        register(new EnableCommand());
        register(new DisableCommand());
        register(new JoinCommand());
        register(new StartCommand());
        register(new QStartCommand());
        register(new ListCommand());
        joinCommand = new JoinCommand();
        spectateCommand = new SpectateCommand();
        register(spectateCommand);
        register(joinCommand);
    }

    public void register(SubCommand cmd) {
        subCommands.put(cmd.getName(), cmd);
    }

    @Override
    public String getName() {
        return "arena";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            MsgUtil.sendConfigMessageList(sender, MsgPath.HelpCommands.Arena.DEFAULT);
            if (sender.hasPermission("liarslounge.admin") || sender.hasPermission("liarslounge.setup"))
                MsgUtil.sendConfigMessageList(sender, MsgPath.HelpCommands.Arena.ADMIN);
            return;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);
        if (subCommand != null) {
            subCommand.execute(sender, label, Arrays.copyOfRange(args, 1, args.length));
        } else {
            MsgUtil.sendConfigMessage(sender, MsgPath.Error.UNKNOWN_SUBCOMMAND);
        }
    }
}
