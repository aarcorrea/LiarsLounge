package com.kooy29.liarslounge.commands.sub.arena;

import com.kooy29.liarslounge.LiarsLounge;
import com.kooy29.liarslounge.api.arena.GameState;
import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.api.arena.IArenaManager;
import com.kooy29.liarslounge.commands.SubCommand;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.utils.ExtraUtil;
import com.kooy29.liarslounge.utils.MsgUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ListCommand implements SubCommand {
    @Override
    public String getName() {
        return "list";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {

        if (!ExtraUtil.hasPermission(sender, "liarslounge.list")) return;

        IArenaManager arenaManager = LiarsLounge.getInstance().getArenaManager();
        List<IArena> arenas = new ArrayList<>(arenaManager.getArenas());

        // For players, use clickable components
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.sendMessage(MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Success.Arena.LIST_HEADER)));

            for (int i = 0; i < arenas.size(); i++) {
                IArena arena = arenas.get(i);
                String entryText = MsgUtil.getConfigMessage(MsgPath.Success.Arena.LIST_ROW)
                        .replace("%sr_no%", String.valueOf(i + 1))
                        .replace("%arena_name%", arena.getName())
                        .replace("%players%", String.valueOf(arena.getPlayers().size()))
                        .replace("%max_players%", String.valueOf(arena.getChairLocations().size()))
                        .replace("%status%", arena.getGameState().toString());

                TextComponent message = new TextComponent(MsgUtil.colorize(entryText));

                if (arena.getGameState() == GameState.PLAYING) {
                    message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder(MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Success.Arena.LIST_SPECTATE))).create()));

                    message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/ll spectate " + arena.getName()));

                    player.spigot().sendMessage(message);
                } else {
                    message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder(MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Success.Arena.LIST_JOIN))).create()));

                    message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/ll join " + arena.getName()));

                    player.spigot().sendMessage(message);
                }
            }
        } else {
            StringBuilder message = new StringBuilder(MsgUtil.getConfigMessage(MsgPath.Success.Arena.LIST_HEADER));

            for (int i = 0; i < arenas.size(); i++) {
                IArena arena = arenas.get(i);
                String entryMessage = MsgUtil.getConfigMessage(MsgPath.Success.Arena.LIST_ROW)
                        .replace("%sr_no%", String.valueOf(i + 1))
                        .replace("%arena_name%", arena.getName())
                        .replace("%players%", String.valueOf(arena.getPlayers().size()))
                        .replace("%max_players%", String.valueOf(arena.getChairLocations().size()))
                        .replace("%status%", arena.getGameState().toString());

                message.append("\n").append(entryMessage);
            }

            sender.sendMessage(MsgUtil.colorize(message.toString()));
        }
    }
}