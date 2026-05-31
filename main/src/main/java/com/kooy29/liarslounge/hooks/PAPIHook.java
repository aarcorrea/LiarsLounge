package com.kooy29.liarslounge.hooks;

import com.kooy29.liarslounge.LiarsLounge;
import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.api.storage.IPlayerStats;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PAPIHook extends PlaceholderExpansion {

    @Override
    @NotNull
    public String getAuthor() {
        return "kooy29";
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "liarslounge";
    }

    @Override
    @NotNull
    public String getVersion() {
        return LiarsLounge.getInstance().getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "player == null";
        if (IArena.gamePlayers.containsKey(player)) {
            IPlayerStats ps = IArena.gamePlayers.get(player).stats;
            if (ps == null) return "";
            if (params.equalsIgnoreCase("stats_firstplay"))
                return ps.getFirstPlay() != null ? new SimpleDateFormat("yyyy/MM/dd HH:mm").format(Date.from(ps.getFirstPlay())) : "--";
            else if (params.equalsIgnoreCase("stats_lastplay"))
                return ps.getLastPlay() != null ? new SimpleDateFormat("yyyy/MM/dd HH:mm").format(Date.from(ps.getLastPlay())) : "--";
            else if (params.equalsIgnoreCase("stats_wins"))
                return String.valueOf(ps.getWins());
            else if (params.equalsIgnoreCase("stats_deaths"))
                return String.valueOf(ps.getDeaths());
            else if (params.equalsIgnoreCase("stats_eliminations"))
                return String.valueOf(ps.getEliminations());
            else if (params.equalsIgnoreCase("stats_calls"))
                return String.valueOf(ps.getCalls());
            else if (params.equalsIgnoreCase("stats_gamesplayed"))
                return String.valueOf(ps.getGamesPlayed());
        }
        return null;
    }
}