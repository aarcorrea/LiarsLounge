package com.kooy29.liarslounge.commands.sub.setup;

import com.kooy29.liarslounge.arena.ArenaSetupSession;
import com.kooy29.liarslounge.commands.SubCommand;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.utils.ExtraUtil;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.kooy29.liarslounge.utils.ExtraUtil.locationToString;

public class WaitingCommand implements SubCommand {
    @Override
    public String getName() {
        return "waiting";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {

        if (ExtraUtil.notAPlayer(sender)) return;

        Player player = (Player) sender;
        Location location = player.getLocation();

        ArenaSetupSession arenaSetupSession = ArenaSetupSession.getPlayerSession(player);

        if (arenaSetupSession == null) {
            MsgUtil.sendConfigMessage(sender, MsgPath.Error.NO_SESSION);
            return;
        }

        arenaSetupSession.setWaitingLocation(location);
        MsgUtil.sendSetupHelpMsg(sender, arenaSetupSession);
        sender.sendMessage(MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Success.Setup.WAITING).replace("%waiting_location%", locationToString(location))));
    }
}
