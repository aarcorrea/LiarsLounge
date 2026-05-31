package com.kooy29.liarslounge.commands.sub;

import com.kooy29.liarslounge.commands.SubCommand;
import com.kooy29.liarslounge.gui.ArenaSelectorGUI;
import com.kooy29.liarslounge.utils.ExtraUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GuiCommand implements SubCommand {
    @Override
    public String getName() {
        return "gui";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {

        if (ExtraUtil.notAPlayer(sender)) return;

        ArenaSelectorGUI.open((Player) sender, 0);
    }
}
