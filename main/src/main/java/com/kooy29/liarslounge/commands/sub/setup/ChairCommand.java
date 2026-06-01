package com.kooy29.liarslounge.commands.sub.setup;

import com.kooy29.liarslounge.api.hologram.IHologram;
import com.kooy29.liarslounge.arena.ArenaSetupSession;
import com.kooy29.liarslounge.commands.SubCommand;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.utils.ExtraUtil;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.kooy29.liarslounge.utils.ExtraUtil.locationToString;

public class ChairCommand implements SubCommand {

    @Override
    public String getName() {
        return "chair";
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


        if (args.length < 1) {
            MsgUtil.sendConfigMessage(sender, MsgPath.Usage.CHAIR);
        } else if (args[0].equals("add")) {
            if (arenaSetupSession.getChairLocations().size() >= 4) {
                MsgUtil.sendConfigMessage(sender, MsgPath.Error.ADD_EXCEED);
                return;
            }
            IHologram holo = instance.getPlayerHolo().getHoloSetupSession(player, "&9Chair - " + (arenaSetupSession.getChairLocations().size() + 1), location);
            holo.show();
            arenaSetupSession.addChairLocation(new ArenaSetupSession.HoloLoc(location, holo));
            sender.sendMessage(MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Success.Setup.CHAIR_ADD).replace("%chair_location%", locationToString(location))));
        } else if (args[0].equals("remove")) {
            if (args.length != 2) {
                MsgUtil.sendConfigMessage(sender, MsgPath.Usage.CHAIR_REMOVE);
                return;
            }
            int index;
            try {
                index = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                MsgUtil.sendConfigMessage(sender, MsgPath.Error.INVALID_NUM);
                return;
            }
            if (index > arenaSetupSession.getChairLocations().size() - 1) {
                MsgUtil.sendConfigMessage(sender, MsgPath.Error.INVALID_NUM);
            } else {
                arenaSetupSession.getChairLocations().get(index).getHologram().remove();
                arenaSetupSession.removeChairLocation(index);
                sender.sendMessage(MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Success.Setup.CHAIR_REMOVE).replace("%chair_location%", locationToString(location)).replace("%index%", args[1])));
                for (int i = 0; i < arenaSetupSession.getChairLocations().size(); i++) {
                    ArenaSetupSession.HoloLoc hl = arenaSetupSession.getChairLocations().get(i);
                    hl.getHologram().setLine(0, MsgUtil.colorize("&9Chair - " + i), true);
                }
            }
        }

        if (!arenaSetupSession.getChairLocations().isEmpty()) {
            sender.sendMessage(" ");
            MsgUtil.sendConfigMessage(sender, MsgPath.Success.Setup.CHAIRS);
            for (int i = 0; i < arenaSetupSession.getChairLocations().size(); i++) {
                sender.sendMessage(i + ": " + locationToString(arenaSetupSession.getChairLocations().get(i).getLocation()));
            }
        }
    }
}
