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

public class ActionItemCommand implements SubCommand {
    @Override
    public String getName() {
        return "action_item";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {

        if (ExtraUtil.notAPlayer(sender)) return;

        Player player = (Player) sender;
        Location location = player.getLocation().clone().subtract(0, 1, 0).getBlock().getLocation().add(0.5, 0, 0.5);

        ArenaSetupSession arenaSetupSession = ArenaSetupSession.getPlayerSession(player);

        if (arenaSetupSession == null) {
            MsgUtil.sendConfigMessage(sender, MsgPath.Error.NO_SESSION);
            return;
        }


        if (args.length < 1) {
            MsgUtil.sendConfigMessage(sender, MsgPath.Usage.ACTION_ITEM);
        } else if (args[0].equals("add")) {
            IHologram holo = instance.getPlayerHolo().getHoloSetupSession(player, "&dAction Item - " + (arenaSetupSession.getActionItemsLocation().size() + 1), location);
            arenaSetupSession.addActionItemsLocation(new ArenaSetupSession.HoloLoc(location, holo));
            holo.show();
            sender.sendMessage(MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Success.Setup.ACTION_ITEM_ADD).replace("%item_location%", locationToString(location))));
        } else if (args[0].equals("remove")) {
            if (args.length != 2) {
                MsgUtil.sendConfigMessage(sender, MsgPath.Usage.ACTION_ITEM_REMOVE);
                return;
            }
            int index;
            try {
                index = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                MsgUtil.sendConfigMessage(sender, MsgPath.Error.INVALID_NUM);
                return;
            }
            if (index > arenaSetupSession.getActionItemsLocation().size() - 1) {
                MsgUtil.sendConfigMessage(sender, MsgPath.Error.INVALID_NUM);
            } else {
                arenaSetupSession.getActionItemsLocation().get(index).getHologram().remove();
                arenaSetupSession.removeActionItemsLocation(index);
                sender.sendMessage(MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Success.Setup.ACTION_ITEM_REMOVE).replace("%item_location%", locationToString(location)).replace("%index%", args[1])));
                for (int i = 0; i < arenaSetupSession.getActionItemsLocation().size(); i++) {
                    ArenaSetupSession.HoloLoc hl = arenaSetupSession.getActionItemsLocation().get(i);
                    hl.getHologram().setLine(0, MsgUtil.colorize("&dAction Item - " + i), true);
                }
            }
        }

        if (!arenaSetupSession.getActionItemsLocation().isEmpty()) {
            sender.sendMessage(" ");
            MsgUtil.sendConfigMessage(sender, MsgPath.Success.Setup.ACTION_ITEMS);
            for (int i = 0; i < arenaSetupSession.getActionItemsLocation().size(); i++) {
                sender.sendMessage(i + ": " + locationToString(arenaSetupSession.getActionItemsLocation().get(i).getLocation()));
            }
        }
    }
}
