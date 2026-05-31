package com.kooy29.liarslounge.arena;

import com.kooy29.liarslounge.LiarsLounge;
import com.kooy29.liarslounge.api.arena.CardType;
import com.kooy29.liarslounge.api.arena.GameState;
import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.api.arena.IArenaManager;
import com.kooy29.liarslounge.api.nms.IVersionWrapper;
import com.kooy29.liarslounge.api.storage.IConfiguration;
import com.kooy29.liarslounge.storage.yaml.ArenaConfig;
import com.kooy29.liarslounge.storage.yaml.ConfigPath;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.storage.yaml.ValuesPath;
import com.kooy29.liarslounge.utils.ExtraUtil;
import com.kooy29.liarslounge.utils.ItemBuilder;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class ArenaManager implements IArenaManager {

    private final Map<String, IArena> enabledArenas = new HashMap<>();
    private final Map<String, Set<IArena>> enabledArenasByGroup = new HashMap<>();
    private final Map<String, Set<IArena>> enabledArenasByWorld = new HashMap<>();
    private LiarsLounge instance;
    private IVersionWrapper nms;

    public ArenaManager(LiarsLounge instance, IVersionWrapper nms) {
        this.instance = instance;
        this.nms = nms;
        loadItems();
    }

    private void loadItems() {
        gameItems.clear();
        for (CardType cardType : CardType.values()) {
            ItemStack cardItem = ItemBuilder.from(instance.getValuesConfig().getConfig().getConfigurationSection(ValuesPath.Game.Items.CARD.replace("%card%", cardType.name)), MsgPath.Game.Items.CARD.replace("%card%", cardType.name)).build();
            cardItem = nms.addCustomData(cardItem, "CARD_" + cardType.name);
            gameItems.put(cardType.name, cardItem);
            gameItems.put(cardType.name + "_verdict_right", ItemBuilder.from(instance.getValuesConfig().getConfig().getConfigurationSection(ValuesPath.Game.Items.VERDICT_CARDS_RIGHT.replace("%card%", cardType.name)), null).build());
            if (!cardType.name.equals("joker"))
                gameItems.put(cardType.name + "_verdict_wrong", ItemBuilder.from(instance.getValuesConfig().getConfig().getConfigurationSection(ValuesPath.Game.Items.VERDICT_CARDS_WRONG.replace("%card%", cardType.name)), null).build());
        }
        gameItems.put("hidden", ItemBuilder.from(instance.getValuesConfig().getConfig().getConfigurationSection(ValuesPath.Game.Items.CARD.replace("%card%", "hidden")), null)
                .build());
        ItemStack liarItem = ItemBuilder.from(
                        instance.getValuesConfig().getConfig().getConfigurationSection(ValuesPath.Game.Items.CALL_LIAR), MsgPath.Game.Items.CALL_LIAR)
                .build();
        liarItem = nms.addCustomData(liarItem, "LIAR_ITEM");
        gameItems.put("liar_item", liarItem);
        ItemStack leaveItem = ItemBuilder.from(
                        instance.getValuesConfig().getConfig().getConfigurationSection(ValuesPath.Game.Items.LEAVE_ITEM), MsgPath.Game.Items.LEAVE_ITEM)
                .build();
        leaveItem = nms.addCustomData(leaveItem, "LEAVE_ITEM");
        gameItems.put("leave_item", leaveItem);
        ItemStack guideItem = ItemBuilder.from(
                        instance.getValuesConfig().getConfig().getConfigurationSection(ValuesPath.Game.Items.GUIDE_ITEM), MsgPath.Game.Items.GUIDE_ITEM)
                .build();
        guideItem = nms.addCustomData(guideItem, "GUIDE_ITEM");
        gameItems.put("guide_item", guideItem);
    }

    @Override
    public boolean registerArena(String name, IConfiguration config) {
        if (config == null) {
            File file = new File(instance.getDataFolder() + "/arenas", name + ".yml");
            if (file.exists()) {
                config = new ArenaConfig(name + ".yml", file);
            } else {
                Bukkit.getLogger().severe("Arena with name " + name + " does not exist.");
                return false;
            }
        }

        IArena arena = new Arena(name, config);
        StringBuilder s = new StringBuilder();
        arena.getChairLocations().forEach(l -> s.append(ExtraUtil.locationToString(l) + ", "));
//         remove all liarslounge armorstands
//        MessageUtil.sendConsoleMessage("&cCleaning unused seats from the arena...");
        List<ArmorStand> toRemove = arena.getWorld()
                .getEntitiesByClass(ArmorStand.class).stream()
                .filter(armorStand -> IArena.SEAT_CUSTOM_NAME.equals(armorStand.getCustomName()))
                .collect(Collectors.toList());
        toRemove.forEach(Entity::remove);

        MsgUtil.sendConsoleMessage("&aRegistered arena: " + name + " | Arena Info:\n" +
                "&eWorld:&r " + arena.getWorld().getName() + "\n" +
                "&eWaiting Location:&r " + ExtraUtil.locationToString(arena.getWaitingLocation()) + "\n" +
                "&eTable Location:&r " + ExtraUtil.locationToString(arena.getTableLocation()) + "\n" +
                "&eChair Locations:&r " + s);
        enabledArenas.put(name, arena);
        if (arena.getGroup() != null) {
            Set<IArena> existingArenas = enabledArenasByGroup.get(arena.getGroup());
            if (existingArenas != null)
                existingArenas.add(arena);
            else
                enabledArenasByGroup.put(arena.getGroup(), new HashSet<>(Collections.singletonList(arena)));
        }
        Set<IArena> existingArenas = enabledArenasByWorld.get(arena.getWorld().getName());
        if (existingArenas != null)
            existingArenas.add(arena);
        else
            enabledArenasByWorld.put(arena.getWorld().getName(), new HashSet<>(Collections.singletonList(arena)));
        return true;
    }

    @Override
    public void saveArena(String name, World world, Location waitingLocation, Location tableLocation, List<Location> chairLocations, List<Location> actionItemsLocation) {
        ArenaConfig config = new ArenaConfig(name + ".yml");
        ConfigurationSection section = config.getConfig();

        // arena settings
        section.set(ConfigPath.ARENA_ENABLED, true);
        section.set(ConfigPath.ARENA_WORLD, world.getName());
        section.set(ConfigPath.ARENA_GROUP, "default");

        // waiting settings
        ConfigurationSection waitingSection = section.createSection(ConfigPath.ARENA_WAITING);
        waitingSection.set(ConfigPath.ARENA_MIN_PLAYERS, 2);
        waitingSection.set(ConfigPath.ARENA_AUTO_START_DELAY, 20);
        waitingSection.set(ConfigPath.ARENA_START_DELAY_ON_FULL, 5);
        waitingSection.set(ConfigPath.ARENA_CARD_THROW_TIMEOUT, 60);

        // waiting location settings
        ConfigurationSection waitingLocationSection = waitingSection.createSection(ConfigPath.LOCATION);
        waitingLocationSection.set("x", waitingLocation.getX());
        waitingLocationSection.set("y", waitingLocation.getY());
        waitingLocationSection.set("z", waitingLocation.getZ());
        waitingLocationSection.set("yaw", waitingLocation.getYaw());
        waitingLocationSection.set("pitch", waitingLocation.getPitch());

        // table settings
        ConfigurationSection tableSection = section.createSection(ConfigPath.ARENA_TABLE + ConfigPath.LOCATION);
        tableSection.set("x", tableLocation.getX());
        tableSection.set("y", tableLocation.getY());
        tableSection.set("z", tableLocation.getZ());

        // chair settings
        ConfigurationSection chairSection = section.createSection(ConfigPath.ARENA_CHAIRS);
        for (Location chair : chairLocations) {
            ConfigurationSection chairLocSection = chairSection.createSection(chairLocations.indexOf(chair) + "");
            chairLocSection.set("x", chair.getX());
            chairLocSection.set("y", chair.getY());
            chairLocSection.set("z", chair.getZ());
            chairLocSection.set("yaw", chair.getYaw());
            chairLocSection.set("pitch", chair.getPitch());
        }

        // action item settings
        ConfigurationSection actionItems = section.createSection(ConfigPath.ARENA_ACTION_ITEMS);
        for (Location actionItem : actionItemsLocation) {
            ConfigurationSection itemLocSection = actionItems.createSection(actionItemsLocation.indexOf(actionItem) + "");
            itemLocSection.set("x", actionItem.getX());
            itemLocSection.set("y", actionItem.getY());
            itemLocSection.set("z", actionItem.getZ());
        }

        config.saveConfig();
    }

    @Override
    public Collection<IArena> getArenas() {
        return enabledArenas.values();
    }

    @Override
    public Collection<IArena> getArenasSorted() {
        return enabledArenas.values().stream()
                .sorted(Comparator.comparingInt(a -> {
                    if (a.getGameState() == GameState.PLAYING) {
                        return 2;
                    } else if (a.getPlayers().size() < a.getChairLocations().size()) {
                        return 0;
                    } else {
                        return 1;
                    }
                })).collect(Collectors.toList());
    }

    @Override
    public IArena getArena(String name) {
        if (!enabledArenas.containsKey(name)) {
            Bukkit.getLogger().severe("No arena found with name " + name);
            return null;
        }
        return enabledArenas.get(name);
    }

    @Override
    public void unregisterArena(String name) {
        if (!enabledArenas.containsKey(name)) {
            Bukkit.getLogger().severe("No arena found with name " + name);
            return;
        }
        IArena arena = enabledArenas.get(name);
        String world = arena.getWorld().getName();
        enabledArenas.remove(name);
        enabledArenasByWorld.get(world).remove(arena);
        if (arena.getGroup() != null)
            enabledArenasByGroup.get(arena.getGroup()).remove(arena);
    }

    @Override
    public boolean arenaExists(String name) {
        return enabledArenas.containsKey(name);
    }

    @Override
    public void clearArenas() {
        enabledArenas.clear();
    }

    @Override
    public int getArenaCount() {
        return enabledArenas.size();
    }

    @Override
    public Set<IArena> getArenaByWorld(String worldName) {
        return enabledArenasByWorld.get(worldName);
    }

    @Override
    public IArena getArenaByPlayer(Player player) {
        return IArena.gamePlayers.get(player).arena;
    }

    @Override
    public boolean isPlayerInArena(Player player) {
        IArena.GamePlayer gp = IArena.gamePlayers.get(player);
        return gp != null && gp.arena != null;
    }

    @Override
    public boolean isArenaByWorld(String worldName) {
        return enabledArenasByWorld.containsKey(worldName);
    }

    @Override
    public Map<String, Set<IArena>> getArenaGroupMap() {
        return enabledArenasByGroup;
    }

    @Override
    public IArena.GamePlayer getGamePlayer(Player player) {
        return IArena.gamePlayers.get(player);
    }

    @Override
    public IArena.GamePlayer removeGamePlayer(Player player) {
        return IArena.gamePlayers.remove(player);
    }
}