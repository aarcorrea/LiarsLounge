package com.kooy29.liarslounge.api.nms;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.Collection;

public interface IVersionWrapper {
    String TAG_KEY = "LiarsLounge";

    void sendActionBar(String message, Player p);

    void sendTitle(Player p, String title, String subtitle, int fadeIn, int stay, int fadeOut);

    org.bukkit.inventory.ItemStack addCustomData(org.bukkit.inventory.ItemStack i, String data);

    boolean isCustomItem(org.bukkit.inventory.ItemStack i);

    String getCustomData(org.bukkit.inventory.ItemStack i);

    void sendHideNametag(Player viewer, Collection<Player> targets);

    void sendShowNametag(Player viewer, Collection<Player> targets);

    ItemStack setTexture(ItemStack stack, String texture, ItemMeta meta);

    default void potionEffect(Player player, boolean punish) {
        if (punish) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 4, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 0, false, false));
        } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 4, false, false));
        }
    }

    default World loadExistingWorld(String worldName, String namespace) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) return world;
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (!worldFolder.exists()) {
            Bukkit.getLogger().warning("World folder '" + worldName + "' does not exist.");
            return null;
        }

        return new WorldCreator(worldName).createWorld();
    }
}