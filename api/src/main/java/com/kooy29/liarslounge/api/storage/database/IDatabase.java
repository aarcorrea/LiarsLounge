package com.kooy29.liarslounge.api.storage.database;

import com.kooy29.liarslounge.api.storage.IPlayerStats;

import java.util.UUID;

public interface IDatabase {

    void init();

    boolean hasStats(UUID uuid);

    void saveStats(IPlayerStats stats);

    IPlayerStats fetchStats(UUID uuid);

    int getColumn(UUID player, String column);
}
