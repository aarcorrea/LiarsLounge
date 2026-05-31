package com.kooy29.liarslounge.api.animation;

import org.bukkit.entity.Player;

import java.util.Collection;

public interface ICardThrow {
    void clearOldData();

    void throwCards(Collection<Player> players, Collection<Player> spectators, Player thrower, int amount, Runnable endTask);

    void forceStop();
}
