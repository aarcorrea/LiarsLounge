package com.kooy29.liarslounge.arena;

import com.kooy29.liarslounge.LiarsLounge;
import com.kooy29.liarslounge.api.arena.IArenaSetupSession;
import com.kooy29.liarslounge.storage.yaml.ArenaConfig;
import com.kooy29.liarslounge.storage.yaml.ConfigPath;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ArenaSetupSession implements IArenaSetupSession {
    private static ArenaSetupSession currentArenaSetupSession = null;
    private static Set<Player> setupPlayers = new HashSet<>();
    private static LiarsLounge instance = LiarsLounge.getInstance();
    private String arenaName;
    private World world;
    private List<HoloLoc> chairLocations;
    private List<HoloLoc> actionItemsLocation;
    private Location waitingLocation;
    private Location tableLocation;

    public ArenaSetupSession(String arenaName, World world, Player player) {
        this.arenaName = arenaName;
        File file = new File(instance.getDataFolder() + "/arenas", arenaName + ".yml");
        if (file.exists()) {
            FileConfiguration config = new ArenaConfig(arenaName, file).getConfig();

            String worldName = config.getString(ConfigPath.ARENA_WORLD);
            String namespace = config.getString(ConfigPath.ARENA_WORLD_NAMESPACE, "minecraft");
            this.world = instance.getVersionWrapper().loadExistingWorld(worldName, namespace);
            ConfigurationSection waiting = config.getConfigurationSection(ConfigPath.ARENA_WAITING + ConfigPath.LOCATION);
            this.waitingLocation = new Location(this.world, waiting.getDouble("x"), waiting.getDouble("y"), waiting.getDouble("z"), (float) waiting.getDouble("yaw"), (float) waiting.getDouble("pitch"));
            ConfigurationSection table = config.getConfigurationSection(ConfigPath.ARENA_TABLE + ConfigPath.LOCATION);
            this.tableLocation = new Location(this.world, table.getDouble("x"), table.getDouble("y"), table.getDouble("z"), 0f, 0f);
            this.chairLocations = new ArrayList<>();
            ConfigurationSection chairs = config.getConfigurationSection(ConfigPath.ARENA_CHAIRS);
            if (chairs != null) {
                for (String key : chairs.getKeys(false)) {
                    ConfigurationSection chairSec = chairs.getConfigurationSection(key);
                    if (chairSec != null) {
                        Location chairLoc = new Location(this.world,
                                chairSec.getDouble("x"),
                                chairSec.getDouble("y"),
                                chairSec.getDouble("z"),
                                (float) chairSec.getDouble("yaw"),
                                (float) chairSec.getDouble("pitch"));
                        this.chairLocations.add(new HoloLoc(chairLoc, instance.getPlayerHolo().getHoloSetupSession(player, "&9Chair - " + (this.chairLocations.size() + 1), chairLoc)));
                    }
                }
            }
            this.actionItemsLocation = new ArrayList<>();
            ConfigurationSection actionItem = config.getConfigurationSection(ConfigPath.ARENA_ACTION_ITEMS);
            if (actionItem != null) {
                for (String key : actionItem.getKeys(false)) {
                    ConfigurationSection actionItemSec = actionItem.getConfigurationSection(key);
                    if (actionItemSec != null) {
                        Location actionItemLoc = new Location(this.world,
                                actionItemSec.getDouble("x"),
                                actionItemSec.getDouble("y"),
                                actionItemSec.getDouble("z"),
                                0f,
                                0f);
                        this.actionItemsLocation.add(new HoloLoc(actionItemLoc, instance.getPlayerHolo().getHoloSetupSession(player, "&dAction Item - " + (this.actionItemsLocation.size() + 1), actionItemLoc)));
                    }
                }
            }
            MsgUtil.sendConfigMessage(player, MsgPath.Error.FOUND_SAVED_ARENA);
        } else {
            this.world = world;
            this.waitingLocation = null;
            this.tableLocation = null;
            this.chairLocations = new ArrayList<>();
            this.actionItemsLocation = new ArrayList<>();
        }
        currentArenaSetupSession = this;
        if (player != null) setupPlayers.add(player);
    }

    public static void addPlayerToSession(Player player) {
        if (currentArenaSetupSession != null) {
            setupPlayers.add(player);
        }
    }

    public static void clearSession() {
        setupPlayers.clear();
    }

    public static void removePlayerFromSession(Player player) {
        if (currentArenaSetupSession != null) {
            setupPlayers.remove(player);
        }
    }

    public static ArenaSetupSession getPlayerSession(Player player) {
        if (currentArenaSetupSession != null && setupPlayers.contains(player)) {
            return currentArenaSetupSession;
        }
        return null;
    }

    public static boolean isCurrentSetupSession() {
        return currentArenaSetupSession != null;
    }

    public static ArenaSetupSession getCurrentSetupSession() {
        return currentArenaSetupSession;
    }

    public static void endSession() {
        if (currentArenaSetupSession != null) {
            currentArenaSetupSession.chairLocations.forEach(hl -> hl.getHologram().remove());
            currentArenaSetupSession.actionItemsLocation.forEach(hl -> hl.getHologram().remove());
            currentArenaSetupSession = null;
        }
        clearSession();
    }

    @Override
    public String getArenaName() {
        return arenaName;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public Location getWaitingLocation() {
        return waitingLocation;
    }

    @Override
    public void setWaitingLocation(Location waitingLocation) {
        this.waitingLocation = waitingLocation;
    }

    @Override
    public Location getTableLocation() {
        return tableLocation;
    }

    @Override
    public void setTableLocation(Location tableLocation) {
        this.tableLocation = tableLocation;
    }

    @Override
    public List<HoloLoc> getChairLocations() {
        return chairLocations;
    }

    @Override
    public void addChairLocation(HoloLoc holoLoc) {
        this.chairLocations.add(holoLoc);
    }

    @Override
    public void removeChairLocation(int index) {
        this.chairLocations.remove(index);
    }

    @Override
    public List<HoloLoc> getActionItemsLocation() {
        return actionItemsLocation;
    }

    @Override
    public void addActionItemsLocation(HoloLoc holoLoc) {
        this.actionItemsLocation.add(holoLoc);
    }

    @Override
    public void removeActionItemsLocation(int index) {
        this.actionItemsLocation.remove(index);
    }

    @Override
    public boolean saveSetup() {
        if (arenaName == null || world == null || waitingLocation == null || tableLocation == null || chairLocations.size() < 2 || actionItemsLocation.size() != chairLocations.size()) {
            Bukkit.getLogger().severe("Setup session is incomplete. Please ensure all locations are set and at least 2 chair locations are added along with equivalent action items location.");
            return false;
        }

        instance.getArenaManager().saveArena(arenaName, world, waitingLocation, tableLocation, chairLocations.stream().map(HoloLoc::getLocation).collect(Collectors.toList()), actionItemsLocation.stream().map(HoloLoc::getLocation).collect(Collectors.toList()));
        endSession();
        return true;
    }
}
