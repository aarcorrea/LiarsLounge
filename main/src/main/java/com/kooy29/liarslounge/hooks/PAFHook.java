package com.kooy29.liarslounge.hooks;

import com.kooy29.liarslounge.api.party.IParty;
import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayer;
import de.simonsator.partyandfriends.spigot.api.pafplayers.PAFPlayerManager;
import de.simonsator.partyandfriends.spigot.api.party.PartyManager;
import de.simonsator.partyandfriends.spigot.api.party.PlayerParty;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PAFHook implements IParty {
    private PlayerParty getParty(Player p) {
        return PartyManager.getInstance().getParty(PAFPlayerManager.getInstance().getPlayer(p.getUniqueId()));
    }

    @Override
    public boolean hasParty(Player p) {
        return getParty(p) != null;
    }

    @Override
    public List<Player> getMembers(Player p) {
        ArrayList<Player> playerList = new ArrayList<>();
        PlayerParty party = getParty(p);
        if (party == null)
            return playerList;
        for (PAFPlayer player : party.getPlayers()) {
            Player pl = Bukkit.getPlayer(player.getUniqueId());
            if (pl != null && pl.isOnline()) playerList.add(pl);
        }
        return playerList;
    }

    @Override
    public List<Player> getAllMembers(Player p) {
        ArrayList<Player> playerList = new ArrayList<>();
        PlayerParty party = getParty(p);
        if (party == null)
            return playerList;
        for (PAFPlayer player : party.getAllPlayers()) {
            Player pl = Bukkit.getPlayer(player.getUniqueId());
            if (pl != null && pl.isOnline()) playerList.add(pl);
        }
        return playerList;
    }

    @Override
    public int partySize(Player p) {
        PlayerParty party = getParty(p);
        if (party == null) return 0;
        return party.getAllPlayers().size();
    }

    @Override
    public boolean isLeader(Player p) {
        PAFPlayer pafplayer = PAFPlayerManager.getInstance().getPlayer(p.getUniqueId());
        PlayerParty party = PartyManager.getInstance().getParty(pafplayer);
        if (party == null) return false;
        return party.isLeader(pafplayer);
    }

    @Override
    public Player getLeader(Player p) {
        return Bukkit.getPlayer(getParty(p).getLeader().getUniqueId());
    }
}
