package com.kooy29.liarslounge.storage;

import com.kooy29.liarslounge.api.storage.IPlayerStats;

import java.time.Instant;
import java.util.UUID;

public class PlayerStats implements IPlayerStats {
    private int id;
    private String name;
    private UUID uuid;
    private Instant firstPlay;
    private Instant lastPlay;
    private int wins;
    private int deaths;
    private int eliminations;
    private int calls;
    private int gamesPlayed;


    public PlayerStats(int id, String name, UUID uuid, Instant firstPlay,
                       Instant lastPlay, int wins, int deaths,
                       int eliminations, int calls, int gamesPlayed) {
        this.id = id;
        this.name = name;
        this.uuid = uuid;
        this.firstPlay = firstPlay;
        this.lastPlay = lastPlay;
        this.wins = wins;
        this.deaths = deaths;
        this.eliminations = eliminations;
        this.calls = calls;
        this.gamesPlayed = gamesPlayed;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Instant getFirstPlay() {
        return firstPlay;
    }

    public void setFirstPlay(Instant firstPlay) {
        this.firstPlay = firstPlay;
    }

    public Instant getLastPlay() {
        return lastPlay;
    }

    public void setLastPlay(Instant lastPlay) {
        this.lastPlay = lastPlay;
    }

    public int getWins() {
        return wins;
    }

    public void addWin() {
        wins++;
    }

    public int getDeaths() {
        return deaths;
    }

    public void addDeath() {
        deaths++;
    }

    public int getEliminations() {
        return eliminations;
    }

    public void addElimination() {
        eliminations++;
    }

    public int getCalls() {
        return calls;
    }

    public void addCalls() {
        calls++;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void addGamesPlayed() {
        gamesPlayed++;
    }


    @Override
    public String toString() {
        return "PlayerStats{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", uuid='" + uuid + '\'' +
                ", firstPlay=" + firstPlay +
                ", lastPlay=" + lastPlay +
                ", wins=" + wins +
                ", deaths=" + deaths +
                ", eliminations=" + eliminations +
                ", calls=" + calls +
                ", gamesPlayed=" + gamesPlayed +
                '}';
    }

    @Override
    public IPlayerStats clone() {
        return new PlayerStats(id, name, uuid, firstPlay, lastPlay, wins, deaths, eliminations, calls, gamesPlayed);
    }
}
