package com.kooy29.liarslounge.commands.sub.setup;

import com.kooy29.liarslounge.arena.ArenaSetupSession;
import com.kooy29.liarslounge.commands.SubCommand;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.utils.ExtraUtil;
import com.kooy29.liarslounge.utils.MsgUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SaveCommand implements SubCommand {

    @Override
    public String getName() {
        return "save";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        Player p = (Player) sender;
        ArenaSetupSession arenaSetupSession = ArenaSetupSession.getCurrentSetupSession();
        if (arenaSetupSession == null) {
            MsgUtil.sendConfigMessage(p, MsgPath.Error.NO_SESSION);
            return;
        }

        if (arenaSetupSession.saveSetup()) {
            MsgUtil.sendConfigMessage(p, MsgPath.Success.Setup.SAVE);
            TextComponent component = new TextComponent(MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Success.Setup.CLICK_TO_ENABLE)));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ll arena enable " + arenaSetupSession.getArenaName()));
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Enable - " + arenaSetupSession.getArenaName()).color(ChatColor.GOLD).create()));
            p.teleport(ExtraUtil.getLobbyLocation());
            p.spigot().sendMessage(component);
        } else
            MsgUtil.sendConfigMessage(p, MsgPath.Error.SETUP_ERROR);
    }
}
