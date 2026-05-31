package com.kooy29.liarslounge.commands;

import com.kooy29.liarslounge.LiarsLounge;
import com.kooy29.liarslounge.api.arena.IArenaManager;
import org.bukkit.command.CommandSender;

public interface SubCommand {
    LiarsLounge instance = LiarsLounge.getInstance();
    IArenaManager arenaManager = LiarsLounge.getInstance().getArenaManager();

    String getName();

    void execute(CommandSender sender, String label, String[] args);
}
