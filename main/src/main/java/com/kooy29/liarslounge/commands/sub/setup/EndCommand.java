package com.kooy29.liarslounge.commands.sub.setup;

import com.kooy29.liarslounge.arena.ArenaSetupSession;
import com.kooy29.liarslounge.commands.SubCommand;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.utils.ExtraUtil;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EndCommand implements SubCommand {

    @Override
    public String getName() {
        return "end";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player p = (Player) sender;
        ArenaSetupSession arenaSetupSession = ArenaSetupSession.getCurrentSetupSession();
        if (arenaSetupSession == null) {
            MsgUtil.sendConfigMessage(p, MsgPath.Error.NO_SESSION);
            return;
        }

        ArenaSetupSession.endSession();
        p.teleport(ExtraUtil.getLobbyLocation());
        MsgUtil.sendConfigMessage(p, MsgPath.Success.Setup.END);
    }
}