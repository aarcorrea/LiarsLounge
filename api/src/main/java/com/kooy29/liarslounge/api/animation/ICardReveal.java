package com.kooy29.liarslounge.api.animation;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public interface ICardReveal {
    void revealTo(Player viewer, ItemStack[] cards, Player accuserPly, boolean isTruth);

    void revealToSpectators(Collection<Player> spectators, ItemStack[] cards, Player accuserPly, boolean isTruth);

    void forceStop();
}
