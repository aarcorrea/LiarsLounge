package com.kooy29.liarslounge.api.storage;

import java.time.Instant;
import java.util.UUID;

public interface IPlayerStats {
    int getId();

    String getName();

    void setName(String name);

    UUID getUuid();

    void setUuid(UUID uuid);

    Instant getFirstPlay();

    void setFirstPlay(Instant firstPlay);

    Instant getLastPlay();

    void setLastPlay(Instant lastPlay);

    int getWins();

    void addWin();

    int getDeaths();

    void addDeath();

    int getEliminations();

    void addElimination();

    int getCalls();

    void addCalls();

    int getGamesPlayed();

    void addGamesPlayed();

    IPlayerStats clone();
}