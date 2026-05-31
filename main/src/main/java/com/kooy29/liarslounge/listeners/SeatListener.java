package com.kooy29.liarslounge.listeners;

import com.kooy29.liarslounge.api.hologram.IHologram;
import com.kooy29.liarslounge.api.hologram.IPlayerHologram;
import com.kooy29.liarslounge.arena.ArenaSetupSession;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import static com.kooy29.liarslounge.utils.ExtraUtil.locationToString;

public class SeatListener implements Listener {

    private IPlayerHologram playerHolo;

    public SeatListener(IPlayerHologram playerHolo) {
        this.playerHolo = playerHolo;
    }

    @EventHandler
    public void onStairClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (e.getClickedBlock() != null && e.getClickedBlock().getType().name().endsWith("STAIRS")) {

            Player p = e.getPlayer();

            ArenaSetupSession arenaSetupSession = ArenaSetupSession.getPlayerSession(p);

            if (arenaSetupSession == null) return;

            e.setCancelled(true);

            Location loc = e.getClickedBlock().getLocation();
            if (arenaSetupSession.getChairLocations().stream().anyMatch(h -> h.getLocation().equals(loc))) {
                MsgUtil.sendConfigMessage(p, MsgPath.Error.CHAIR_ALREADY_SET);
                return;
            }

            if (arenaSetupSession.getChairLocations().size() >= 4) {
                MsgUtil.sendConfigMessage(p, MsgPath.Error.ADD_EXCEED);
                return;
            }

            IHologram holo = playerHolo.getHoloSS(p, "&9Chair - " + (arenaSetupSession.getChairLocations().size() + 1), loc);
            holo.show();
            arenaSetupSession.addChairLocation(new ArenaSetupSession.HoloLoc(loc, holo));
            p.sendMessage(MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Success.Setup.CHAIR_ADD).replace("%chair_location%", locationToString(loc))));
        }
    }
}
