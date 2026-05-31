package com.kooy29.liarslounge.listeners.gui;

import com.kooy29.liarslounge.api.nms.IVersionWrapper;
import com.kooy29.liarslounge.commands.sub.arena.ArenaGroup;
import com.kooy29.liarslounge.gui.ArenaSelectorGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ArenaSelectorListener implements Listener {

    IVersionWrapper nms;

    public ArenaSelectorListener(IVersionWrapper nms) {
        this.nms = nms;
    }

    @EventHandler
    public void onArenaSelector(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (!(player.getOpenInventory().getTopInventory().getHolder() instanceof ArenaSelectorGUI.ArenaSelectorGUIHolder))
            return;

        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if (item == null) return;
        if (item.getType() == Material.AIR) return;
        if (!nms.isCustomItem(item)) return;

        String data = nms.getCustomData(item);

        if (data.startsWith("ASG-ARENAJ=")) {
            String arenaName = data.split("=")[1];
            ArenaGroup.joinCommand.execute(player, "", new String[]{arenaName});
            player.closeInventory();
        } else if (data.equals("ASG-ARENAS=")) {
            String arenaName = data.split("=")[1];
            ArenaGroup.spectateCommand.execute(player, "", new String[]{arenaName});
            player.closeInventory();
        } else if (data.equals("ASG-PAGE_NEXT")) {
            ArenaSelectorGUI.ArenaSelectorGUIHolder ih = ((ArenaSelectorGUI.ArenaSelectorGUIHolder) player.getOpenInventory().getTopInventory().getHolder());
            ih.setPage(ih.getPage() + 1);
            ArenaSelectorGUI.refresh(player);
        } else if (data.equals("ASG-PAGE_PREVIOUS")) {
            ArenaSelectorGUI.ArenaSelectorGUIHolder ih = ((ArenaSelectorGUI.ArenaSelectorGUIHolder) player.getOpenInventory().getTopInventory().getHolder());
            ih.setPage(ih.getPage() - 1);
            ArenaSelectorGUI.refresh(player);
        }
    }
}
