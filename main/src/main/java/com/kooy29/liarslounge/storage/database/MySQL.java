package com.kooy29.liarslounge.storage.database;

import com.kooy29.liarslounge.LiarsLounge;
import com.kooy29.liarslounge.api.storage.IPlayerStats;
import com.kooy29.liarslounge.api.storage.database.IDatabase;
import com.kooy29.liarslounge.storage.PlayerStats;
import com.kooy29.liarslounge.storage.yaml.ConfigPath;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.UUID;

public class MySQL implements IDatabase {

    private HikariDataSource dataSource;
    private String host;
    private int port;
    private String database;
    private String user;
    private String pass;
    private boolean ssl;
    private boolean verifyCertificate;
    private int poolSize;
    private int maxLifetime;
    private int idleTimeout;
    private int keepaliveTime;
    private int connectionTimeout;

    public MySQL(LiarsLounge instance) {
        host = instance.getConfig().getString(ConfigPath.DATABASE + ".host");
        port = instance.getConfig().getInt(ConfigPath.DATABASE + ".port");
        database = instance.getConfig().getString(ConfigPath.DATABASE + ".database");
        user = instance.getConfig().getString(ConfigPath.DATABASE + ".user");
        pass = instance.getConfig().getString(ConfigPath.DATABASE + ".pass");
        ssl = instance.getConfig().getBoolean(ConfigPath.DATABASE + ".ssl");
        verifyCertificate = instance.getConfig().getBoolean(ConfigPath.DATABASE + ".verify-certificate");
        poolSize = instance.getConfig().getInt(ConfigPath.DATABASE + ".pool-size");
        maxLifetime = instance.getConfig().getInt(ConfigPath.DATABASE + ".max-lifetime");
        idleTimeout = instance.getConfig().getInt(ConfigPath.DATABASE + ".idle-timeout");
        keepaliveTime = instance.getConfig().getInt(ConfigPath.DATABASE + ".keepalive-time");
        connectionTimeout = instance.getConfig().getInt(ConfigPath.DATABASE + ".connection-timeout");
    }

    public boolean connect() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setPoolName("LiarsLoungeMySQLPool");

        hikariConfig.setMaximumPoolSize(poolSize);
        hikariConfig.setMaxLifetime(maxLifetime);
        hikariConfig.setIdleTimeout(idleTimeout);
        hikariConfig.setKeepaliveTime(keepaliveTime);
        hikariConfig.setConnectionTimeout(connectionTimeout);
        hikariConfig.setConnectionTestQuery("SELECT 1");

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=" + ssl
                + "&verifyServerCertificate=" + verifyCertificate;
        hikariConfig.setJdbcUrl(jdbcUrl);

        hikariConfig.setUsername(user);
        hikariConfig.setPassword(pass);
        hikariConfig.addDataSourceProperty("characterEncoding", "utf8");
        hikariConfig.addDataSourceProperty("encoding", "UTF-8");
        hikariConfig.addDataSourceProperty("useUnicode", "true");

        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
        hikariConfig.addDataSourceProperty("jdbcCompliantTruncation", "false");

        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "275");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        hikariConfig.addDataSourceProperty("socketTimeout", "30000");

        dataSource = new HikariDataSource(hikariConfig);
        try {
            dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void init() {
        String sql = "CREATE TABLE IF NOT EXISTS player_stats (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(200)," +
                "uuid VARCHAR(36) UNIQUE," +
                "first_play TIMESTAMP NULL DEFAULT NULL," +
                "last_play TIMESTAMP NULL DEFAULT NULL," +
                "wins INT," +
                "deaths INT," +
                "eliminations INT," +
                "calls INT," +
                "games_played INT" +
                ");";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasStats(UUID uuid) {
        String sql = "SELECT uuid FROM player_stats WHERE uuid=?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void saveStats(IPlayerStats stats) {
        try (Connection conn = getConnection()) {
            if (hasStats(stats.getUuid())) {
                String update = "UPDATE player_stats SET last_play=?, wins=?, deaths=?, eliminations=?, calls=?, games_played=?, name=? WHERE uuid=?;";
                try (PreparedStatement ps = conn.prepareStatement(update)) {
                    ps.setTimestamp(1, Timestamp.from(stats.getLastPlay()));
                    ps.setInt(2, stats.getWins());
                    ps.setInt(3, stats.getDeaths());
                    ps.setInt(4, stats.getEliminations());
                    ps.setInt(5, stats.getCalls());
                    ps.setInt(6, stats.getGamesPlayed());
                    ps.setString(7, stats.getName());
                    ps.setString(8, stats.getUuid().toString());
                    ps.executeUpdate();
                }
            } else {
                String insert = "INSERT INTO player_stats (name, uuid, first_play, last_play, wins, deaths, eliminations, calls, games_played) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
                try (PreparedStatement ps = conn.prepareStatement(insert)) {
                    ps.setString(1, stats.getName());
                    ps.setString(2, stats.getUuid().toString());
                    ps.setTimestamp(3, Timestamp.from(stats.getFirstPlay()));
                    ps.setTimestamp(4, Timestamp.from(stats.getLastPlay()));
                    ps.setInt(5, stats.getWins());
                    ps.setInt(6, stats.getDeaths());
                    ps.setInt(7, stats.getEliminations());
                    ps.setInt(8, stats.getCalls());
                    ps.setInt(9, stats.getGamesPlayed());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PlayerStats fetchStats(UUID uuid) {
        String sql = "SELECT * FROM player_stats WHERE uuid=?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new PlayerStats(
                            rs.getInt("id"),
                            rs.getString("name"),
                            UUID.fromString(rs.getString("uuid")),
                            rs.getTimestamp("first_play") != null ? rs.getTimestamp("first_play").toInstant() : null,
                            rs.getTimestamp("last_play") != null ? rs.getTimestamp("last_play").toInstant() : null,
                            rs.getInt("wins"),
                            rs.getInt("deaths"),
                            rs.getInt("eliminations"),
                            rs.getInt("calls"),
                            rs.getInt("games_played")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getColumn(UUID uuid, String column) {
        String sql = "SELECT " + column + " FROM player_stats WHERE uuid=?;";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(column);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
