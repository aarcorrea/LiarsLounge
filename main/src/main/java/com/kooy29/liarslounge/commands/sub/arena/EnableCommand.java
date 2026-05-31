package com.kooy29.liarslounge.commands.sub.arena;

import com.kooy29.liarslounge.commands.SubCommand;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.utils.ExtraUtil;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.command.CommandSender;

public class EnableCommand implements SubCommand {
    @Override
    public String getName() {
        return "enable";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {

        if (!ExtraUtil.hasPermission(sender, "liarslounge.admin", "liarslounge.arena.enable")) return;

        if (args.length == 0) {
            MsgUtil.sendConfigMessage(sender, MsgPath.Usage.ENABLE_ARENA);
            return;
        }

        String arenaName = args[0];
        if (arenaManager.arenaExists(arenaName)) {
            MsgUtil.sendConfigMessage(sender, MsgPath.Error.ARENA_ALREADY_ENABLED);
            return;
        }

        if (arenaManager.registerArena(arenaName, null)) {
            sender.sendMessage(MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Success.Arena.ENABLE).replace("%arena%", arenaName)));
        } else {
            MsgUtil.sendConfigMessage(sender, MsgPath.Error.ARENA_NOT_FOUND);
        }
    }
}
