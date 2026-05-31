package com.kooy29.liarslounge.api.animation;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface ILiarCall {
    void clearOldData();

    void setupAxe(Player player, Location actionItemLoc);

    void moveToPlayer(Player player, Runnable task);

    void moveBackToLoc(Player player);

    void playAxeSwing(Player player, boolean fullSwing, Runnable task);

    void destroyAxe(Player player, int entityId);
}
