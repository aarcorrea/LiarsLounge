package com.kooy29.liarslounge.nms.v1_8_R3;

import com.kooy29.liarslounge.api.nms.IVersionWrapper;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.Collection;


public class VersionWrapper implements IVersionWrapper {

    public VersionWrapper(Plugin plugin) {
    }

    public static String getCustomDataNMS(Object i) {
        net.minecraft.server.v1_8_R3.ItemStack itemStack = (net.minecraft.server.v1_8_R3.ItemStack) i;
        NBTTagCompound tag = itemStack.getTag();
        if (tag == null) return "";
        return tag.getString(IVersionWrapper.TAG_KEY);
    }

    public static org.bukkit.inventory.ItemStack addCustomDataNMS(org.bukkit.inventory.ItemStack i, String data) {
        net.minecraft.server.v1_8_R3.ItemStack itemStack = CraftItemStack.asNMSCopy(i);
        NBTTagCompound tag = itemStack.getTag();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        tag.setString(IVersionWrapper.TAG_KEY, data);
        itemStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(itemStack);
    }

    public static void sendPacket(Player p, Packet<?> packet) {
        if (p == null || !p.isOnline()) return;
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
    }

    public static void sendPackets(Player p, Packet<?>... packets) {
        if (p == null || !p.isOnline()) return;
        PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
        for (Packet<?> packet : packets) {
            connection.sendPacket(packet);
        }
    }

    @Override
    public void sendActionBar(String message, Player p) {
        IChatBaseComponent chatComponent = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + message + "\"}");
        PacketPlayOutChat packet = new PacketPlayOutChat(chatComponent, (byte) 2);
        sendPacket(p, packet);
    }

    @Override
    public void sendTitle(Player p, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (title != null) {
            if (!title.isEmpty()) {
                IChatBaseComponent bc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + title + "\"}");
                PacketPlayOutTitle tit = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, bc);
                PacketPlayOutTitle length = new PacketPlayOutTitle(fadeIn, stay, fadeOut);
                sendPackets(p, tit, length);
            }
        }
        if (subtitle != null) {
            IChatBaseComponent bc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + subtitle + "\"}");
            PacketPlayOutTitle tit = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, bc);
            PacketPlayOutTitle length = new PacketPlayOutTitle(fadeIn, stay, fadeOut);
            sendPackets(p, tit, length);
        }
    }

    @Override
    public org.bukkit.inventory.ItemStack addCustomData(org.bukkit.inventory.ItemStack i, String data) {
        return addCustomDataNMS(i, data);
    }

    @Override
    public boolean isCustomItem(org.bukkit.inventory.ItemStack i) {
        net.minecraft.server.v1_8_R3.ItemStack itemStack = CraftItemStack.asNMSCopy(i);
        if (itemStack == null) return false;
        NBTTagCompound tag = itemStack.getTag();
        if (tag == null) return false;
        return tag.hasKey(IVersionWrapper.TAG_KEY);
    }

    @Override
    public String getCustomData(org.bukkit.inventory.ItemStack i) {
        if (i == null) return "";
        net.minecraft.server.v1_8_R3.ItemStack itemStack = CraftItemStack.asNMSCopy(i);
        if (itemStack == null) return "";
        NBTTagCompound tag = itemStack.getTag();
        if (tag == null) return "";
        return tag.getString(IVersionWrapper.TAG_KEY);
    }

    private void sendTeamPacket(Player viewer, String visibility, Collection<Player> targets, int type) {
        PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
        try {
            java.lang.reflect.Field a = packet.getClass().getDeclaredField("a"); // team name
            java.lang.reflect.Field b = packet.getClass().getDeclaredField("b"); // display name
            java.lang.reflect.Field e = packet.getClass().getDeclaredField("e"); // nametag visibility
            java.lang.reflect.Field g = packet.getClass().getDeclaredField("g"); // players
            java.lang.reflect.Field h = packet.getClass().getDeclaredField("h"); // mode

            a.setAccessible(true);
            b.setAccessible(true);
            e.setAccessible(true);
            g.setAccessible(true);
            h.setAccessible(true);

            Collection<String> playerNames = targets.stream()
                    .map(Player::getName)
                    .collect(java.util.stream.Collectors.toList());

            a.set(packet, "hideTag_ll");
            b.set(packet, "hideTag_ll");
            e.set(packet, visibility);
            g.set(packet, playerNames); // Correct player list field
            h.set(packet, type);
         /* 0 Create team
            1 Remove team (delete)
            2 Update team
            3 Add players
            4 Remove players */

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        sendPacket(viewer, packet);
    }

    public void sendHideNametag(Player viewer, Collection<Player> targets) {
        sendTeamPacket(viewer, "never", targets, 0);
    }

    public void sendShowNametag(Player viewer, Collection<Player> targets) {
        sendTeamPacket(viewer, "always", targets, 1);
    }

    @Override
    public ItemStack setTexture(ItemStack stack, String texture, ItemMeta meta) {
        return null;
    }
}