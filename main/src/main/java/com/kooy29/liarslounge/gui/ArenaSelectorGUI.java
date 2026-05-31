package com.kooy29.liarslounge.gui;

import com.kooy29.liarslounge.LiarsLounge;
import com.kooy29.liarslounge.api.arena.GameState;
import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.storage.yaml.ValuesPath;
import com.kooy29.liarslounge.utils.ItemBuilder;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArenaSelectorGUI {

    public static void open(Player player, int page) {
        int size = LiarsLounge.getInstance().getValuesConfig().getConfig().getInt(ValuesPath.Gui.ArenaSelector.SIZE);
        if (size % 9 != 0) size = 45;
        if (size > 54) size = 54;

        ArenaSelectorGUIHolder arenaSelectorGUIHolder = new ArenaSelectorGUIHolder(page);
        Inventory inventory = Bukkit.createInventory(
                arenaSelectorGUIHolder,
                size,
                MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Gui.ArenaSelector.TITLE))
        );

        List<Integer> slots = Stream.of(
                        LiarsLounge.getInstance().getValuesConfig().getConfig().getString(ValuesPath.Gui.ArenaSelector.JoinItem.SLOTS).split(","))
                .map(s -> Integer.parseInt(s.trim()))
                .collect(Collectors.toList());

        List<IArena> arenas = new ArrayList<>(LiarsLounge.getInstance().getArenaManager().getArenasSorted());

        int arenasPerPage = slots.size();
        int fromIndex = page * arenasPerPage;
        int toIndex = Math.min(fromIndex + arenasPerPage, arenas.size());
        boolean hasNextPage = toIndex < arenas.size();
        List<IArena> arenasOnPage = arenas.subList(fromIndex, toIndex);

        for (int i = 0; i < arenasOnPage.size(); i++) {
            IArena arena = arenasOnPage.get(i);
            String gameState = arena.getGameState().name().toLowerCase();
            String path1 = ValuesPath.Gui.ArenaSelector.JoinItem.BASE.replace("%status%", gameState);
            String path2 = MsgPath.Gui.ArenaSelector.JoinItem.replace("%status%", gameState);

            ItemStack arenaItem = ItemBuilder.from(LiarsLounge.getInstance().getValuesConfig().getConfig().getConfigurationSection(path1), path2, arena).build();

            int slot = slots.get(i);
            arenaItem = LiarsLounge.getInstance().getVersionWrapper().addCustomData(arenaItem, "ASG-ARENA" + (arena.getGameState() == GameState.PLAYING ? "S" : "J") + "=" + arena.getName());
            inventory.setItem(slot, arenaItem);
        }

        int joinRandomSlot = LiarsLounge.getInstance().getValuesConfig().getConfig()
                .getInt(ValuesPath.Gui.ArenaSelector.JOIN_RANDOM + ".slot");
        ItemStack joinRandomItem = ItemBuilder.from(LiarsLounge.getInstance().getValuesConfig().getConfig().getConfigurationSection(
                ValuesPath.Gui.ArenaSelector.JOIN_RANDOM), MsgPath.Gui.ArenaSelector.JoinRandom).build();
        joinRandomItem = LiarsLounge.getInstance().getVersionWrapper().addCustomData(joinRandomItem, "ASG-ARENAJ=random");
        inventory.setItem(joinRandomSlot, joinRandomItem);

        if (hasNextPage) {
            int nextPageSlot = LiarsLounge.getInstance().getValuesConfig().getConfig()
                    .getInt(ValuesPath.Gui.ArenaSelector.NEXT_PAGE + ".slot");
            ItemStack nextPageItem = ItemBuilder.from(LiarsLounge.getInstance().getValuesConfig().getConfig().getConfigurationSection(
                    ValuesPath.Gui.ArenaSelector.NEXT_PAGE), MsgPath.Gui.ArenaSelector.NextPage).build();
            nextPageItem = LiarsLounge.getInstance().getVersionWrapper().addCustomData(nextPageItem, "ASG-PAGE_NEXT");
            inventory.setItem(nextPageSlot, nextPageItem);
        } else {
            int nextPageSlot = LiarsLounge.getInstance().getValuesConfig().getConfig()
                    .getInt(ValuesPath.Gui.ArenaSelector.NEXT_PAGE + ".slot");
            inventory.setItem(nextPageSlot, null);
        }

        if (page != 0) {
            int prevPageSlot = LiarsLounge.getInstance().getValuesConfig().getConfig()
                    .getInt(ValuesPath.Gui.ArenaSelector.PREVIOUS_PAGE + ".slot");
            ItemStack prevPageItem = ItemBuilder.from(LiarsLounge.getInstance().getValuesConfig().getConfig().getConfigurationSection(
                    ValuesPath.Gui.ArenaSelector.PREVIOUS_PAGE), MsgPath.Gui.ArenaSelector.PreviousPage).build();
            prevPageItem = LiarsLounge.getInstance().getVersionWrapper().addCustomData(prevPageItem, "ASG-PAGE_PREVIOUS");
            inventory.setItem(prevPageSlot, prevPageItem);
        } else {
            int prevPageSlot = LiarsLounge.getInstance().getValuesConfig().getConfig()
                    .getInt(ValuesPath.Gui.ArenaSelector.PREVIOUS_PAGE + ".slot");
            inventory.setItem(prevPageSlot, null);
        }

        // TODO: play sound on open
        player.openInventory(inventory);
    }

    public static void refresh(Player player) {
        if (player == null || player.getOpenInventory() == null || !(player.getOpenInventory().getTopInventory().getHolder() instanceof ArenaSelectorGUIHolder)) {
            return;
        }
        ArenaSelectorGUIHolder arenaSelectorGUIHolder = ((ArenaSelectorGUIHolder) player.getOpenInventory().getTopInventory().getHolder());

        int page = arenaSelectorGUIHolder.getPage();
        Inventory inventory = player.getOpenInventory().getTopInventory();

        List<Integer> slots = Stream.of(
                        LiarsLounge.getInstance().getValuesConfig().getConfig().getString(ValuesPath.Gui.ArenaSelector.JoinItem.SLOTS).split(","))
                .map(s -> Integer.parseInt(s.trim()))
                .collect(Collectors.toList());

        List<IArena> arenas = new ArrayList<>(LiarsLounge.getInstance().getArenaManager().getArenasSorted());

        int arenasPerPage = slots.size();
        int fromIndex = page * arenasPerPage;
        int toIndex = Math.min(fromIndex + arenasPerPage, arenas.size());
        boolean hasNextPage = toIndex < arenas.size();
        List<IArena> arenasOnPage = arenas.subList(fromIndex, toIndex);

        for (int slot : slots) {
            inventory.setItem(slot, null);
        }

        for (int i = 0; i < arenasOnPage.size(); i++) {
            IArena arena = arenasOnPage.get(i);
            String gameState = arena.getGameState().name().toLowerCase();
            String path1 = ValuesPath.Gui.ArenaSelector.JoinItem.BASE.replace("%status%", gameState);
            String path2 = MsgPath.Gui.ArenaSelector.JoinItem.replace("%status%", gameState);

            ItemStack arenaItem = ItemBuilder.from(LiarsLounge.getInstance().getValuesConfig().getConfig().getConfigurationSection(path1), path2, arena).build();

            int slot = slots.get(i);
            arenaItem = LiarsLounge.getInstance().getVersionWrapper().addCustomData(arenaItem, "ASG-ARENA" + (arena.getGameState() == GameState.PLAYING ? "S" : "J") + "=" + arena.getName());
            inventory.setItem(slot, arenaItem);
        }

        if (hasNextPage) {
            int nextPageSlot = LiarsLounge.getInstance().getValuesConfig().getConfig()
                    .getInt(ValuesPath.Gui.ArenaSelector.NEXT_PAGE + ".slot");
            ItemStack nextPageItem = ItemBuilder.from(LiarsLounge.getInstance().getValuesConfig().getConfig().getConfigurationSection(
                    ValuesPath.Gui.ArenaSelector.NEXT_PAGE), MsgPath.Gui.ArenaSelector.NextPage).build();
            nextPageItem = LiarsLounge.getInstance().getVersionWrapper().addCustomData(nextPageItem, "ASG-PAGE_NEXT");
            inventory.setItem(nextPageSlot, nextPageItem);
        } else {
            int nextPageSlot = LiarsLounge.getInstance().getValuesConfig().getConfig()
                    .getInt(ValuesPath.Gui.ArenaSelector.NEXT_PAGE + ".slot");
            inventory.setItem(nextPageSlot, null);
        }

        if (page != 0) {
            int prevPageSlot = LiarsLounge.getInstance().getValuesConfig().getConfig()
                    .getInt(ValuesPath.Gui.ArenaSelector.PREVIOUS_PAGE + ".slot");
            ItemStack prevPageItem = ItemBuilder.from(LiarsLounge.getInstance().getValuesConfig().getConfig().getConfigurationSection(
                    ValuesPath.Gui.ArenaSelector.PREVIOUS_PAGE), MsgPath.Gui.ArenaSelector.PreviousPage).build();
            prevPageItem = LiarsLounge.getInstance().getVersionWrapper().addCustomData(prevPageItem, "ASG-PAGE_PREVIOUS");
            inventory.setItem(prevPageSlot, prevPageItem);
        } else {
            int prevPageSlot = LiarsLounge.getInstance().getValuesConfig().getConfig()
                    .getInt(ValuesPath.Gui.ArenaSelector.PREVIOUS_PAGE + ".slot");
            inventory.setItem(prevPageSlot, null);
        }

        player.updateInventory();
    }

    public static class ArenaSelectorGUIHolder implements InventoryHolder {

        private int page;

        public ArenaSelectorGUIHolder(int page) {
            this.page = page;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }
    }
}