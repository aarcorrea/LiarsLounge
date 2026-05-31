package com.kooy29.liarslounge.api.nms;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public interface IWrapperMethods {

    Set<Integer> armorStands = new HashSet<>();

    boolean canPlayerUnmount(Player player);

    boolean isPlayerInArena(Player player);

    ItemStack getHiddenCardItem();
}
