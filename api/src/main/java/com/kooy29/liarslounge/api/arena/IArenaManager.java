package com.kooy29.liarslounge.api.arena;

import com.kooy29.liarslounge.api.storage.IConfiguration;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public interface IArenaManager {
    static Map<String, ItemStack> gameItems = new HashMap<>();

    static ItemStack getGameItem(String itemName) {
        return gameItems.get(itemName);
    }

    boolean registerArena(String name, IConfiguration config);

    void saveArena(String name, World world, Location waitingLocation, Location tableLocation, List<Location> chairLocations, List<Location> actionItemsLocation);

    Collection<IArena> getArenas();

    Collection<IArena> getArenasSorted();

    IArena getArena(String name);

    void unregisterArena(String name);

    boolean arenaExists(String name);

    void clearArenas();

    int getArenaCount();

    Set<IArena> getArenaByWorld(String worldName);

    IArena getArenaByPlayer(Player player);

    boolean isPlayerInArena(Player player);

    boolean isArenaByWorld(String worldName);

    IArena.GamePlayer getGamePlayer(Player player);

    IArena.GamePlayer removeGamePlayer(Player player);

    Map<String, Set<IArena>> getArenaGroupMap();
}
