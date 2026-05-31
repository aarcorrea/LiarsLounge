package com.kooy29.liarslounge.storage;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class MapBuild {
    private static final Set<Player> canBuild = new HashSet<>();

    public static void addPlayer(Player player) {
        canBuild.add(player);
    }

    public static void removePlayer(Player player) {
        canBuild.remove(player);
    }

    public static boolean canBuild(Player player) {
        return canBuild.contains(player);
    }
}