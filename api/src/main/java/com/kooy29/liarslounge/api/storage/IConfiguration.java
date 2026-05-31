package com.kooy29.liarslounge.api.storage;

import org.bukkit.configuration.file.FileConfiguration;

public interface IConfiguration {
    FileConfiguration getConfig();

    void loadConfig(boolean saveResource);

    void saveConfig();

    void reloadConfig(boolean save);
}
