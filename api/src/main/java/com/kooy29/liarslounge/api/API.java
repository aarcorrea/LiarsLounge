package com.kooy29.liarslounge.api;

import com.kooy29.liarslounge.api.arena.IArenaManager;
import com.kooy29.liarslounge.api.nms.IVersionWrapper;
import com.kooy29.liarslounge.api.storage.IConfiguration;
import com.kooy29.liarslounge.api.storage.database.IDatabase;

public interface API {
    IConfiguration getConfiguration();

    IConfiguration getValuesConfig();

    IArenaManager getArenaManager();

    IVersionWrapper getVersionWrapper();

    IDatabase getDb();
}
