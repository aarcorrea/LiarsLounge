package com.kooy29.liarslounge.commands.sub;

import com.kooy29.liarslounge.arena.ArenaSetupSession;
import com.kooy29.liarslounge.commands.SubCommand;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HelpCommand implements SubCommand {

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (sender instanceof Player && ArenaSetupSession.getPlayerSession((Player) sender) != null)
            MsgUtil.sendSetupHelpMsg(sender, ArenaSetupSession.getCurrentSetupSession());
        MsgUtil.sendConfigMessageList(sender, MsgPath.HelpCommands.Ll.DEFAULT);
        if (sender.hasPermission("liarslounge.admin") || sender.hasPermission("liarslounge.setup"))
            MsgUtil.sendConfigMessageList(sender, MsgPath.HelpCommands.Ll.ADMIN);
    }
}
