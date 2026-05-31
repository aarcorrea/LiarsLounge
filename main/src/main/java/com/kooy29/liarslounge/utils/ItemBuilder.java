package com.kooy29.liarslounge.utils;

import com.kooy29.liarslounge.APIProvider;
import com.kooy29.liarslounge.LiarsLounge;
import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.storage.yaml.ValuesPath;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ItemBuilder {

    private ItemStack stack;


    public ItemBuilder(Material mat) {
        stack = new ItemStack(mat);
    }

    public ItemBuilder(Material mat, short sh) {
        stack = new ItemStack(mat, 1, sh);
        addItemFlag(ItemFlag.HIDE_POTION_EFFECTS); // hides extra description
    }

    public ItemBuilder(ItemStack item) {
        this.stack = item;
    }

    public ItemBuilder(String texture) {
        String bukkitVersion = Bukkit.getBukkitVersion();
        String[] versionSplit = bukkitVersion.split("\\.");

        int major = Integer.parseInt(versionSplit[0]);
        int minor = Integer.parseInt(versionSplit[1]);
        if (major > 1 || (major == 1 && minor >= 13 && minor >= 2)) {
            stack = new ItemStack(Material.getMaterial("PLAYER_HEAD"));
        } else {
            stack = new ItemStack(Material.SKULL_ITEM);
            stack.setDurability((short) 3);
        }
        addItemFlag(ItemFlag.HIDE_POTION_EFFECTS);
        ItemMeta meta = getItemMeta();
        stack.setItemMeta(meta);
        if (APIProvider.isHigherVersion) {
            LiarsLounge.getInstance().getVersionWrapper().setTexture(stack, texture, meta);
            return;
        }
        SkullMeta headMeta = (SkullMeta) stack.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", texture));
        Method setProfileField = null;
        try {
            setProfileField = getItemMeta().getClass().getDeclaredMethod("setProfile", GameProfile.class);
        } catch (NoSuchMethodException | SecurityException ignored) {
        }
        if (setProfileField == null) {
            try {
                Field profileField = headMeta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(headMeta, profile);
            } catch (Exception ex) {
            }
        } else {
            setProfileField.setAccessible(true);
            try {
                setProfileField.invoke(headMeta, profile);
            } catch (IllegalAccessException | InvocationTargetException e) {
            }
        }
        stack.setItemMeta(headMeta);
    }

    // Extra Methods
    public static void setGlow(ItemStack item, boolean glow) {
        ItemMeta meta = item.getItemMeta();
        if (glow) {
            meta.addEnchant(Enchantment.KNOCKBACK, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            for (Enchantment enchantment : meta.getEnchants().keySet()) {
                meta.removeEnchant(enchantment);
            }
        }
        item.setItemMeta(meta);
    }

    public static ItemBuilder from(ConfigurationSection itemConfig, String path) {
        ItemBuilder item;
        if (itemConfig.getString(ValuesPath.Game.Items.MATERIAL).equals("CUSTOM_SKULL")) {
            item = new ItemBuilder(itemConfig.getString(ValuesPath.Game.Items.TEXTURE));
        } else {
            item = new ItemBuilder(Material.valueOf(itemConfig.getString(ValuesPath.Game.Items.MATERIAL)),
                    (short) itemConfig.getDouble(ValuesPath.Game.Items.DATA));
        }
        if (path == null) return item;
        if (path.contains(".cards.")) {
            item.setDisplayName(MsgUtil.colorize(MsgUtil.getConfigMessage(path + MsgPath.Game.Items.NOT_SELECTED_NAME)));
            item.setLore(MsgUtil.colorize(MsgUtil.getConfigMessageList(path + MsgPath.Game.Items.NOT_SELECTED_LORE)));
        } else {
            item.setDisplayName(MsgUtil.colorize(MsgUtil.getConfigMessage(path + MsgPath.Game.Items.NAME)));
            item.setLore(MsgUtil.colorize(MsgUtil.getConfigMessageList(path + MsgPath.Game.Items.LORE)));
        }
        return item;
    }

    public static ItemBuilder from(ConfigurationSection itemConfig, String path, IArena arena) {
        ItemBuilder item;
        if (itemConfig.getString(ValuesPath.Game.Items.MATERIAL).equals("CUSTOM_SKULL")) {
            item = new ItemBuilder(itemConfig.getString(ValuesPath.Game.Items.TEXTURE));
        } else {
            item = new ItemBuilder(Material.valueOf(itemConfig.getString(ValuesPath.Game.Items.MATERIAL)),
                    (short) itemConfig.getDouble(ValuesPath.Game.Items.DATA));
        }
        if (path == null) return item;
        item.setDisplayName(MsgUtil.colorize(MsgUtil.getConfigMessage(path + MsgPath.Game.Items.NAME).replace("%arena_name%", arena.getName())));
        item.setLore(MsgUtil.colorize(MsgUtil.getConfigMessageList(path + MsgPath.Game.Items.LORE).stream().map(l ->
                        l.replace("%players%", arena.getPlayers().size() + "").replace("%max_players%", arena.getChairLocations().size() + ""))
                .collect(Collectors.toList())));
        return item;
    }

    public ItemMeta getItemMeta() {
        return stack.getItemMeta();
    }

    public ItemBuilder setItemMeta(ItemMeta meta) {
        stack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setColor(Color color) {
        LeatherArmorMeta meta = (LeatherArmorMeta) stack.getItemMeta();
        meta.setColor(color);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder setGlow(boolean glow) {
        if (glow) {
            addEnchant(Enchantment.KNOCKBACK, 1);
            addItemFlag(ItemFlag.HIDE_ENCHANTS);
        } else {
            ItemMeta meta = getItemMeta();
            for (Enchantment enchantment : meta.getEnchants().keySet()) {
                meta.removeEnchant(enchantment);
                setItemMeta(meta);
            }
        }
        return this;
    }

    public ItemBuilder setUnbreakable(boolean unbreakable) {
        ItemMeta meta = stack.getItemMeta();
        meta.spigot().setUnbreakable(unbreakable);
        stack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setBannerColor(DyeColor color) {
        BannerMeta meta = (BannerMeta) stack.getItemMeta();
        meta.setBaseColor(color);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        stack.setAmount(amount);
        return this;
    }

    public ItemBuilder setHead(String owner) {
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        meta.setOwner(owner);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder setDisplayName(String displayname) {
        ItemMeta meta = getItemMeta();
        meta.setDisplayName(displayname);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder setItemStack(ItemStack stack) {
        this.stack = stack;
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        ItemMeta meta = getItemMeta();
        meta.setLore(lore);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder addLore(String lore) {
        ArrayList<String> loreList = new ArrayList<>();
        loreList.add(lore);
        ItemMeta meta = getItemMeta();
        meta.setLore(loreList);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder addEnchant(Enchantment enchantment, int level) {
        ItemMeta meta = getItemMeta();
        meta.addEnchant(enchantment, level, true);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder addItemFlag(ItemFlag flag) {
        ItemMeta meta = getItemMeta();
        meta.addItemFlags(flag);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder removeItemFlag(ItemFlag flag) {
        ItemMeta meta = getItemMeta();
        meta.removeItemFlags(flag);
        setItemMeta(meta);
        return this;
    }

    public ItemStack build() {
        return stack;
    }
}