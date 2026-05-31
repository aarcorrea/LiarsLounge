package com.kooy29.liarslounge.utils;

import com.kooy29.liarslounge.api.arena.IArenaSetupSession;
import com.kooy29.liarslounge.api.storage.IConfiguration;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MsgUtil {
    private static final String PREFIX = "&r[LiarsLounge]";
    private static IConfiguration messagesConfig;

    public static void setMsgConfig(IConfiguration msgConfig) {
        messagesConfig = msgConfig;
    }

    public static String colorize(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public static List<String> colorize(List<String> msg) {
        List<String> colored = new ArrayList<>();
        for (String line : msg) {
            colored.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        return colored;
    }

    public static void sendConsoleMessage(String msg) {
        Bukkit.getConsoleSender().sendMessage(colorize(PREFIX + " " + msg));
    }

    public static void sendMessage(CommandSender sender, String msg) {
        sender.sendMessage(colorize(msg));
    }

    public static void sendMessageList(CommandSender sender, List<String> msg) {
        for (String line : msg) {
            sender.sendMessage(colorize(line));
        }
    }

    public static void sendMessage(CommandSender[] senders, String msg) {
        msg = colorize(msg);
        for (CommandSender sender : senders.clone()) {
            sender.sendMessage(msg);
        }
    }

    @SafeVarargs
    public static void sendMessage(String msg, Collection<Player>... senders) {
        msg = colorize(msg);
        for (Collection<Player> senderList : senders) {
            for (Player sender : senderList) {
                sender.sendMessage(msg);
            }
        }
    }

    public static String getConfigMessage(String path) {
        return messagesConfig.getConfig().getString(path, "&c " + path + " not found");
    }

    public static List<String> getConfigMessageList(String path) {
        return messagesConfig.getConfig().getStringList(path);
    }

    public static void sendConfigMessage(CommandSender sender, String path) {
        sendMessage(sender, getConfigMessage(path));
    }

    @SafeVarargs
    public static void sendConfigMessage(String path, Collection<Player>... senders) {
        sendMessage(getConfigMessage(path), senders);
    }

    public static void sendConfigMessageList(CommandSender sender, String path) {
        sendMessageList(sender, getConfigMessageList(path));
    }

    public static void sendSetupHelpMsg(CommandSender sender, IArenaSetupSession setupSession) {
        sender.sendMessage(" ");
        Player player = (Player) sender;

        List<String> msg = MsgUtil.getConfigMessageList(MsgPath.HelpCommands.Setup.IN_SESSION);
        String notSet = MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Error.LOC_NOT_SET));
        String chairList = setupSession.getChairLocations().stream()
                .map(IArenaSetupSession.HoloLoc::getLocation)
                .map(MsgUtil::locationToString)
                .collect(Collectors.joining(", "));
        String itemsList = setupSession.getActionItemsLocation().stream()
                .map(IArenaSetupSession.HoloLoc::getLocation)
                .map(MsgUtil::locationToString)
                .collect(Collectors.joining(", "));
        Location waitingLocation = setupSession.getWaitingLocation();
        Location tableLocation = setupSession.getTableLocation();

        for (String line : msg) {
            line = MsgUtil.colorize(line);

            TextComponent component;

            if (line.startsWith("%exec_waiting%")) {
                line = line.replace("%exec_waiting%", "");
                component = new TextComponent(line.replace("%waiting_location%",
                        waitingLocation != null ? locationToString(waitingLocation) : notSet));
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ll setup waiting"));
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("/ll setup waiting").create()));
            } else if (line.startsWith("%exec_table%")) {
                line = line.replace("%exec_table%", "");
                component = new TextComponent(line.replace("%table_location%",
                        tableLocation != null ? locationToString(tableLocation) : notSet));
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ll setup table"));
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("/ll setup table").create()));
            } else if (line.startsWith("%cmd_chair%")) {
                line = line.replace("%cmd_chair%", "");
                component = new TextComponent(line.replace("%chair_location%",
                        !chairList.isEmpty() ? chairList : notSet));
                component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ll setup chair"));
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("/ll setup chair").create()));
            } else if (line.startsWith("%cmd_action_item%")) {
                line = line.replace("%cmd_action_item%", "");
                component = new TextComponent(line.replace("%item_location%",
                        !itemsList.isEmpty() ? itemsList : notSet));
                component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ll setup action_item"));
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("/ll setup action_item").create()));
            } else if (line.startsWith("%exec_save%")) {
                line = line.replace("%exec_save%", "");
                component = new TextComponent(line);
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ll setup save"));
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("/ll setup save").create()));
            } else if (line.startsWith("%exec_end%")) {
                line = line.replace("%exec_end%", "");
                component = new TextComponent(line);
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ll setup end"));
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("/ll setup end").create()));
            } else {
                component = new TextComponent(line);
            }

            player.spigot().sendMessage(component);
        }
    }

    private static String locationToString(Location l) {
        return String.format("[X: %.2f, Y: %.2f, Z: %.2f, Yaw: %.2f, Pitch: %.2f]", l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
    }

    public static ConfigurationSection bookGUISection() {
        return messagesConfig.getConfig().getConfigurationSection(MsgPath.Gui.BOOK_GUIDE);
    }

    public static void reload() {
        messagesConfig.reloadConfig(false);
    }
}
