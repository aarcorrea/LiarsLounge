package com.kooy29.liarslounge.storage.yaml;

import com.kooy29.liarslounge.LiarsLounge;
import com.kooy29.liarslounge.api.storage.IConfiguration;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Configuration implements IConfiguration {
    private final LiarsLounge instance = LiarsLounge.getInstance();
    private final String fileName;
    private File configFile;
    private FileConfiguration config;

    public Configuration(String fileName, boolean saveResource) {
        this.fileName = fileName;
        loadConfig(saveResource);
    }

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    @Override
    public void loadConfig(boolean saveResource) {
        configFile = new File(instance.getDataFolder(), fileName);

        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            if (saveResource) instance.saveResource(fileName, false);
            else {
                try {
                    configFile.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        config = new YamlConfiguration();

        try {
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
        if (save)
            saveConfig();
        MsgUtil.sendConsoleMessage("&eReloading " + fileName + " file...");
        loadConfig(true);
    }
}
