package com.kooy29.liarslounge.commands.sub.arena;

import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.commands.SubCommand;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.utils.ExtraUtil;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand implements SubCommand, CommandExecutor {
    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {

        if (ExtraUtil.notAPlayer(sender)) return;

        if (!ExtraUtil.hasPermission(sender, "liarslounge.leave", "liarslounge.admin")) return;

        Player p = (Player) sender;
        IArena arena = arenaManager.getArenaByPlayer(p);
        if (arena == null) {
            MsgUtil.sendConfigMessage(sender, MsgPath.Error.NOT_IN_ARENA);
            return;
        }

        arena.removeSpectator(p);
        arena.removePlayer(p);
        p.teleport(ExtraUtil.getLobbyLocation());
        MsgUtil.sendConfigMessage(sender, MsgPath.Success.Arena.LEAVE);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        execute(commandSender, s, strings);
        return true;
    }
}
