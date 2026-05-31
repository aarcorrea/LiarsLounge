package com.kooy29.liarslounge.storage.database;

import com.kooy29.liarslounge.LiarsLounge;
import com.kooy29.liarslounge.api.storage.IPlayerStats;
import com.kooy29.liarslounge.api.storage.database.IDatabase;
import com.kooy29.liarslounge.storage.PlayerStats;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.UUID;

public class SQLite implements IDatabase {

    private String url;

    private Connection connection;

    public SQLite() {
        File file = new File(LiarsLounge.getInstance().getDataFolder() + "/data.db");

        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    Bukkit.getLogger().severe("Could not create database file! (data.db)");
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        this.url = "jdbc:sqlite:" + file;
        try {
            Class.forName("org.sqlite.JDBC");
            DriverManager.getConnection(url);
        } catch (SQLException | ClassNotFoundException e) {
            if (e instanceof ClassNotFoundException) {
                Bukkit.getLogger().severe("Could not find SQLite Driver on your system!");
            }
            e.printStackTrace();
        }
    }

    @Override
    public void init() {
        String sql;
        try {
            checkConnection();

            sql = "CREATE TABLE IF NOT EXISTS player_stats (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name VARCHAR(200), uuid VARCHAR(36), first_play TIMESTAMP NULL DEFAULT NULL, " +
                    "last_play TIMESTAMP DEFAULT NULL, wins INTEGER(10), deaths INTEGER(10), " +
                    "eliminations INTEGER(10), calls INTEGER(10), games_played INTEGER(10));";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasStats(UUID uuid) {
        String sql = "SELECT uuid FROM player_stats WHERE uuid = ?;";
        try {
            checkConnection();

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    return result.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void saveStats(IPlayerStats stats) {
        String sql;
        try {
            checkConnection();

            if (hasStats(stats.getUuid())) {
                // UPDATE existing row
                sql = "UPDATE player_stats SET last_play=?, wins=?, deaths=?, eliminations=?, calls=?, games_played=?, name=? WHERE uuid=?;";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setTimestamp(1, Timestamp.from(stats.getLastPlay()));
                    statement.setInt(2, stats.getWins());
                    statement.setInt(3, stats.getDeaths());
                    statement.setInt(4, stats.getEliminations());
                    statement.setInt(5, stats.getCalls());
                    statement.setInt(6, stats.getGamesPlayed());
                    statement.setString(7, stats.getName());
                    statement.setString(8, stats.getUuid().toString());
                    statement.executeUpdate();
                }
            } else {
                // INSERT new row
                sql = "INSERT INTO player_stats (name, uuid, first_play, last_play, wins, deaths, eliminations, calls, games_played) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, stats.getName());
                    statement.setString(2, stats.getUuid().toString());
                    statement.setTimestamp(3, Timestamp.from(stats.getFirstPlay()));
                    statement.setTimestamp(4, Timestamp.from(stats.getLastPlay()));
                    statement.setInt(5, stats.getWins());
                    statement.setInt(6, stats.getDeaths());
                    statement.setInt(7, stats.getEliminations());
                    statement.setInt(8, stats.getCalls());
                    statement.setInt(9, stats.getGamesPlayed());
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PlayerStats fetchStats(UUID uuid) {
        PlayerStats stats = null;
        String sql = "SELECT * FROM player_stats WHERE uuid = ?;";
        try {
            checkConnection();

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        stats = new PlayerStats(
                                result.getInt("id"),
                                result.getString("name"),
                                UUID.fromString(result.getString("uuid")),
                                result.getTimestamp("first_play") != null ? result.getTimestamp("first_play").toInstant() : null,
                                result.getTimestamp("last_play") != null ? result.getTimestamp("last_play").toInstant() : null,
                                result.getInt("wins"),
                                result.getInt("deaths"),
                                result.getInt("eliminations"),
                                result.getInt("calls"),
                                result.getInt("games_played")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    @SuppressWarnings("unused")
    @Override
    public int getColumn(UUID player, String column) {
        String sql = "SELECT ? FROM player_stats WHERE uuid = ?;";
        try {
            checkConnection();

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, column);
                statement.setString(2, player.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getInt(column);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return 0;
        }
        return 0;
    }

    private void checkConnection() throws SQLException {
        boolean renew = false;

        if (this.connection == null)
            renew = true;
        else if (this.connection.isClosed())
            renew = true;

        if (renew)
            this.connection = DriverManager.getConnection(url);
    }

}