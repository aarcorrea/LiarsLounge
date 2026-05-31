package com.kooy29.liarslounge.commands.sub.setup;

import com.kooy29.liarslounge.arena.ArenaSetupSession;
import com.kooy29.liarslounge.commands.SubCommand;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.utils.ExtraUtil;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.kooy29.liarslounge.utils.ExtraUtil.locationToString;

public class TableCommand implements SubCommand {

    @Override
    public String getName() {
        return "table";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {

        if (ExtraUtil.notAPlayer(sender)) return;

        Player player = (Player) sender;
        Location location = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation().add(0.5, 0, 0.5);
        ;

        ArenaSetupSession arenaSetupSession = ArenaSetupSession.getPlayerSession(player);

        if (arenaSetupSession == null) {
            MsgUtil.sendConfigMessage(sender, MsgPath.Error.NO_SESSION);
            return;
        }

        arenaSetupSession.setTableLocation(location);
        MsgUtil.sendSetupHelpMsg(sender, arenaSetupSession);
        sender.sendMessage(MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Success.Setup.TABLE).replace("%table_location%", locationToString(location))));
    }
}
