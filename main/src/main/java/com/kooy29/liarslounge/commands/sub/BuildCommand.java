package com.kooy29.liarslounge.commands.sub;

import com.kooy29.liarslounge.commands.SubCommand;
import com.kooy29.liarslounge.storage.MapBuild;
import com.kooy29.liarslounge.utils.ExtraUtil;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuildCommand implements SubCommand {

    @Override
    public String getName() {
        return "build";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (ExtraUtil.notAPlayer(sender)) return;
        Player p = (Player) sender;
        if (!ExtraUtil.hasPermission(p, "liarslounge.admin")) return;
        if (MapBuild.canBuild(p)) {
            MapBuild.removePlayer(p);
            MsgUtil.sendMessage(sender, "&c❌ &7No longer in build mode.");
        } else {
            MapBuild.addPlayer(p);
            MsgUtil.sendMessage(sender, "&a✅ &7You're now in build mode.");
        }
    }
}
