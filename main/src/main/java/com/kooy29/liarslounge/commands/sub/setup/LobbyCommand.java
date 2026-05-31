package com.kooy29.liarslounge.commands.sub.setup;

import com.kooy29.liarslounge.commands.SubCommand;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.utils.ExtraUtil;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LobbyCommand implements SubCommand {

    @Override
    public String getName() {
        return "lobby";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (ExtraUtil.notAPlayer(sender)) return;

        Location location = ((Player) sender).getLocation();
        ExtraUtil.setLobbyLocation(location);

        MsgUtil.sendConfigMessage(sender, MsgPath.Success.Setup.LOBBY);
    }
}
