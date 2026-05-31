package com.kooy29.liarslounge.commands.sub.setup;

import com.kooy29.liarslounge.arena.ArenaSetupSession;
import com.kooy29.liarslounge.commands.SubCommand;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinCommand implements SubCommand {
    @Override
    public String getName() {
        return "join";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player p = (Player) sender;
        ArenaSetupSession arenaSetupSession = ArenaSetupSession.getCurrentSetupSession();
        if (arenaSetupSession == null) {
            MsgUtil.sendConfigMessage(p, MsgPath.Error.NO_SESSION);
            return;
        }

        ArenaSetupSession.addPlayerToSession(p);
        p.teleport(arenaSetupSession.getWorld().getSpawnLocation());
        p.setGameMode(GameMode.CREATIVE);
        p.setAllowFlight(true);
        p.setFlying(true);
        p.setAllowFlight(true);
        arenaSetupSession.getChairLocations().forEach(h -> h.getHologram().show());
        arenaSetupSession.getActionItemsLocation().forEach(h -> h.getHologram().show());
    }
}
