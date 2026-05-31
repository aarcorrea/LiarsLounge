package com.kooy29.liarslounge.commands.sub;

import com.kooy29.liarslounge.LiarsLounge;
import com.kooy29.liarslounge.commands.SubCommand;
import com.kooy29.liarslounge.utils.ExtraUtil;
import com.kooy29.liarslounge.utils.MsgUtil;
import com.kooy29.liarslounge.utils.SoundUtil;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements SubCommand {

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!ExtraUtil.hasPermission(sender, "liarslounge.admin")) return;
        MsgUtil.reload();
        SoundUtil.reload();
        LiarsLounge.getInstance().getBookGUI().initialize();
        MsgUtil.sendMessage(sender, "&aMessages Configuration reloaded successfully!");
    }
}
