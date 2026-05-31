package com.kooy29.liarslounge.api.party;

import org.bukkit.entity.Player;

import java.util.List;

public interface IParty {
    boolean hasParty(Player p);

    List<Player> getAllMembers(Player p);

    List<Player> getMembers(Player p);

    int partySize(Player p);

    boolean isLeader(Player p);

    Player getLeader(Player p);
}
