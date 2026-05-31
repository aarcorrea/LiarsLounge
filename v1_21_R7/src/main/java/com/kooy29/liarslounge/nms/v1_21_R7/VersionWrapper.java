package com.kooy29.liarslounge.nms.v1_21_R7;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kooy29.liarslounge.api.nms.IVersionWrapper;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.UUID;

public class VersionWrapper implements IVersionWrapper {

    private static NamespacedKey dataKey;

    public VersionWrapper(Plugin plugin) {
        dataKey = new NamespacedKey(plugin, TAG_KEY);
    }

    public static ItemStack addCustomDataNMS(ItemStack item, String data) {
        if (item == null) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.getPersistentDataContainer()
                .set(dataKey, PersistentDataType.STRING, data);

        item.setItemMeta(meta);
        return item;
    }

    public static String getCustomDataNMS(ItemStack item) {
        if (item == null) return "";

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return "";

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.getOrDefault(dataKey, PersistentDataType.STRING, "");
    }

    private static URL getUrlFromBase64(String base64) {
        try {
            String json = new String(
                    Base64.getDecoder().decode(base64),
                    StandardCharsets.UTF_8
            );

            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            String url = obj
                    .getAsJsonObject("textures")
                    .getAsJsonObject("SKIN")
                    .get("url")
                    .getAsString();

            return URI.create(url).toURL();
        } catch (Exception e) {
            return null;
        }
    }

    public static void sendPacket(Player player, Packet<?> packet) {
        ((CraftPlayer) player).getHandle().g.b(packet);
    }

    public static void sendPackets(Player player, Packet<?>... packets) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().g;
        for (Packet<?> packet : packets) {
            connection.b(packet);
        }
    }

    @Override
    public ItemStack addCustomData(ItemStack item, String data) {
        if (item == null) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.getPersistentDataContainer()
                .set(dataKey, PersistentDataType.STRING, data);

        item.setItemMeta(meta);
        return item;
    }

    @Override
    public boolean isCustomItem(ItemStack item) {
        if (item == null) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        return meta.getPersistentDataContainer()
                .has(dataKey, PersistentDataType.STRING);
    }

    @Override
    public String getCustomData(ItemStack item) {
        if (item == null) return "";

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return "";

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.getOrDefault(dataKey, PersistentDataType.STRING, "");
    }

    @Override
    public void sendActionBar(String message, Player player) {
        player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                new TextComponent(ChatColor.translateAlternateColorCodes('&', message))
        );
    }

    @Override
    public void sendTitle(Player player, String title, String subtitle,
                          int fadeIn, int stay, int fadeOut) {
        player.sendTitle(
                title == null ? " " : title,
                subtitle == null ? " " : subtitle,
                fadeIn, stay, fadeOut
        );
    }

    private Team getOrCreateTeam(Scoreboard board) {
        Team team = board.getTeam("hideTag_ll");
        if (team == null) {
            team = board.registerNewTeam("hideTag_ll");
        }
        return team;
    }

    public void sendHideNametag(Player viewer, Collection<Player> targets) {
        Scoreboard board = viewer.getScoreboard();
        if (board == Bukkit.getScoreboardManager().getMainScoreboard()) {
            board = Bukkit.getScoreboardManager().getNewScoreboard();
            viewer.setScoreboard(board);
        }

        Team team = getOrCreateTeam(board);
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);

        for (Player target : targets) {
            team.addEntry(target.getName());
        }
    }

    public void sendShowNametag(Player viewer, Collection<Player> targets) {
        Scoreboard board = viewer.getScoreboard();
        Team team = board.getTeam("hideTag_ll");
        if (team == null) return;

        for (Player target : targets) {
            team.removeEntry(target.getName());
        }
    }

    @Override
    public ItemStack setTexture(ItemStack stack, String texture, ItemMeta meta) {
        if (texture == null || texture.isEmpty()) return stack;

        URL skinUrl = getUrlFromBase64(texture);
        if (skinUrl == null) return stack;

        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
        profile.getTextures().setSkin(skinUrl);

        SkullMeta meta2 = (SkullMeta) stack.getItemMeta();
        meta2.setOwnerProfile(profile);
        stack.setItemMeta(meta2);

        return stack;
    }
}
