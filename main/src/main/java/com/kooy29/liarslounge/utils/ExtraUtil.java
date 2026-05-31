package com.kooy29.liarslounge.utils;

import com.kooy29.liarslounge.APIProvider;
import com.kooy29.liarslounge.LiarsLounge;
import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.api.arena.IArenaManager;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.storage.yaml.ValuesPath;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Stairs;

import java.io.File;
import java.lang.reflect.Method;

public class ExtraUtil {

    private static Location location;

    public static String locationToString(Location l) {
        return String.format("[X: %.2f, Y: %.2f, Z: %.2f, Yaw: %.2f, Pitch: %.2f]", l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
    }

    public static Location getLobbyLocation() {
        if (location != null) return location;
        ConfigurationSection section = LiarsLounge.getInstance().getValuesConfig().getConfig().getConfigurationSection(ValuesPath.Lobby.LOCATION);
        if (section == null)
            location = Bukkit.getWorlds().get(0).getSpawnLocation();
        else
            location = new Location(
                    Bukkit.getWorld(section.getString("world")),
                    section.getDouble("x"),
                    section.getDouble("y"),
                    section.getDouble("z"),
                    (float) section.getDouble("yaw"),
                    (float) section.getDouble("pitch"));
        return location;
    }

    public static boolean isLobbyLocation() {
        return LiarsLounge.getInstance()
                .getValuesConfig()
                .getConfig()
                .isConfigurationSection(ValuesPath.Lobby.LOCATION);
    }

    public static void setLobbyLocation(Location loc) {
        ExtraUtil.setGameRules(loc.getWorld());
        FileConfiguration valuesConfig = LiarsLounge.getInstance().getValuesConfig().getConfig();
        valuesConfig.set(ValuesPath.Lobby.LOCATION + ".world", loc.getWorld().getName());
        valuesConfig.set(ValuesPath.Lobby.LOCATION + ".x", loc.getX());
        valuesConfig.set(ValuesPath.Lobby.LOCATION + ".y", loc.getY());
        valuesConfig.set(ValuesPath.Lobby.LOCATION + ".z", loc.getZ());
        valuesConfig.set(ValuesPath.Lobby.LOCATION + ".yaw", loc.getYaw());
        valuesConfig.set(ValuesPath.Lobby.LOCATION + ".pitch", loc.getPitch());
        LiarsLounge.getInstance().getValuesConfig().saveConfig();
        location = loc;
    }

    public static ItemStack getHiddenCardItem() {
        return IArenaManager.getGameItem("hidden");
    }

    public static boolean notAPlayer(CommandSender sender) {
        if (sender instanceof Player) {
            return false;
        } else {
            MsgUtil.sendMessage(sender, "&cThis command can only be executed by a player.");
            return true;
        }
    }

    public static void spawnSeat(Location loc, Player p) {
        double forwardOffset = 0.15; // Adjust this value (bw 0.1 to 0.3)

        if (APIProvider.isHigherVersion)
            loc = loc.getBlock().getLocation().clone().add(0.5, 0.5, 0.5);
        else
            loc = loc.getBlock().getLocation().clone().add(0.5, 0.3, 0.5);

        BlockFace blockFace = BlockFace.NORTH;
        Block block = loc.getBlock();

        boolean isLegacy = false;
        try {
            Object data = block.getClass().getMethod("getBlockData").invoke(block);
            Method getFacing = data.getClass().getMethod("getFacing");
            blockFace = (BlockFace) getFacing.invoke(data);
        } catch (Exception e) {
            try {
                MaterialData legacyData = block.getState().getData();
                if (legacyData instanceof Stairs) {
                    blockFace = ((Stairs) legacyData).getFacing();
                }
                isLegacy = true;
            } catch (Exception ignored) {
            }
        }

        switch (blockFace) {
            case NORTH:
                loc.setYaw(isLegacy ? -180 : 0);
                if (APIProvider.isHigherVersion) {
                    loc.add(0, 0, forwardOffset);
                }
                break;
            case SOUTH:
                loc.setYaw(isLegacy ? 0 : -180);
                if (APIProvider.isHigherVersion) {
                    loc.add(0, 0, forwardOffset);
                }
                break;
            case EAST:
                loc.setYaw(isLegacy ? -90 : 90);
                if (APIProvider.isHigherVersion) {
                    loc.add(forwardOffset, 0, 0);
                }
                break;
            case WEST:
                loc.setYaw(isLegacy ? 90 : -90);
                if (APIProvider.isHigherVersion) {
                    loc.add(forwardOffset, 0, 0);
                }
                break;
            default:
                loc.setYaw(-180);
                if (APIProvider.isHigherVersion) {
                    loc.add(0, 0, forwardOffset);
                }
        }

        ArmorStand seat = loc.getWorld().spawn(loc, ArmorStand.class);
        seat.setVisible(false);
        seat.setGravity(false);
        seat.setMarker(true);
        seat.setSmall(true);
        seat.setCustomName(IArena.SEAT_CUSTOM_NAME);
        seat.setCustomNameVisible(false);

        seat.teleport(loc);
        p.teleport(loc);
        seat.setPassenger(p);
        LiarsLounge.getInstance().getVersionWrapper().sendActionBar("", p);
    }

    public static void destroySeat(Entity entity) {
        if (isSeat(entity)) {
            entity.remove();
        }
    }

    public static World loadExistingWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) return world;
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (!worldFolder.exists()) {
            Bukkit.getLogger().warning("World folder '" + worldName + "' does not exist.");
            return null;
        }

        return new WorldCreator(worldName).createWorld();
    }

    public static void setGameRules(World world) {
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("announceAdvancements", "false");
        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("doFireTick", "false");
        world.setGameRuleValue("mobGriefing", "false");
        world.setGameRuleValue("doWeatherCycle", "false");
    }

    public static boolean isSeat(Entity entity) {
        return entity instanceof ArmorStand && IArena.SEAT_CUSTOM_NAME.equals(entity.getCustomName());
    }

    public static boolean hasPermission(CommandSender sender, String... permissions) {
        for (String permission : permissions) {
            if (sender.hasPermission(permission)) {
                return true;
            }
        }
        MsgUtil.sendConfigMessage(sender, MsgPath.Error.NO_PERMISSION);
        return false;
    }

    public static boolean hasPermissionNP(CommandSender sender, String... permissions) {
        for (String permission : permissions) {
            if (sender.hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }
}
