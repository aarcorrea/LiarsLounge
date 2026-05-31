package com.kooy29.liarslounge.storage.yaml;

import com.kooy29.liarslounge.LiarsLounge;
import com.kooy29.liarslounge.api.arena.IArenaManager;
import com.kooy29.liarslounge.api.storage.IConfiguration;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ArenaConfig implements IConfiguration {

    private static final IArenaManager arenaManager = LiarsLounge.getInstance().getArenaManager();
    private final LiarsLounge instance = LiarsLounge.getInstance();
    private final String fileName;
    private File configFile;
    private FileConfiguration config;

    public ArenaConfig(String fileName) {
        this.fileName = fileName;
        loadConfig(false);
    }

    public ArenaConfig(String fileName, File configFile) {
        this.fileName = fileName;
        this.configFile = configFile;
        loadConfigFile();
    }

    public static void loadArenas() {
        File arenasFolder = new File(LiarsLounge.getInstance().getDataFolder(), "arenas");

        if (!arenasFolder.exists()) {
            arenasFolder.mkdirs();
        }

        File[] files = arenasFolder.listFiles();

        if (files == null) return;

        HashMap<String, IConfiguration> arenas = new HashMap<>();

        for (File file : files) {
            if (!file.isDirectory() && file.getName().endsWith(".yml")) {
                IConfiguration arenaConfig = new ArenaConfig(file.getName());
                arenas.put(file.getName().replace(".yml", ""), arenaConfig);
            }
        }

        for (Map.Entry<String, IConfiguration> arenaConfig : arenas.entrySet()) {
            boolean isEnabled = arenaConfig.getValue().getConfig().getBoolean(ConfigPath.ARENA_ENABLED);
            if (isEnabled) arenaManager.registerArena(arenaConfig.getKey(), arenaConfig.getValue());
        }

        int arenaCount = arenaManager.getArenaCount();
        if (arenaCount > 0)
            MsgUtil.sendConsoleMessage("&aAuto-Loaded " + arenaCount + " arena(s)");
    }

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    @Override
    public void loadConfig(boolean saveResource) {
        configFile = new File(instance.getDataFolder() + "/arenas", fileName);

        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
//            instance.saveResource(fileName, false); // arenas dont have default files
            }

            config = new YamlConfiguration();

            config.load(configFile);

        } catch (IOException | InvalidConfigurationException e) {
            instance.getLogger().severe("Error while trying to create or load " + fileName + " file.");
            e.printStackTrace();
        }
    }

    private void loadConfigFile() {
        try {
            config = new YamlConfiguration();

            config.load(configFile);

        } catch (IOException | InvalidConfigurationException e) {
            instance.getLogger().severe("Error while trying to create or load " + fileName + " file.");
            e.printStackTrace();
        }
    }

    @Override
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            instance.getLogger().severe("Error while saving " + fileName + " file.");
            e.printStackTrace();
        }
    }

    @Override
    public void reloadConfig(boolean save) {
        if (save) saveConfig();
        MsgUtil.sendConsoleMessage("&eReloading " + fileName + " file...");
        loadConfig(false);
    }
}