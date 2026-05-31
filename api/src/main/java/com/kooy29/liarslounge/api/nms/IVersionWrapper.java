package com.kooy29.liarslounge.api.nms;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;

public interface IVersionWrapper {
    String TAG_KEY = "LiarsLoungeItem";

    void sendActionBar(String message, Player p);

    void sendTitle(Player p, String title, String subtitle, int fadeIn, int stay, int fadeOut);

    org.bukkit.inventory.ItemStack addCustomData(org.bukkit.inventory.ItemStack i, String data);

    boolean isCustomItem(org.bukkit.inventory.ItemStack i);

    String getCustomData(org.bukkit.inventory.ItemStack i);

    void sendHideNametag(Player viewer, Collection<Player> targets);

    void sendShowNametag(Player viewer, Collection<Player> targets);

    ItemStack setTexture(ItemStack stack, String texture, ItemMeta meta);
}