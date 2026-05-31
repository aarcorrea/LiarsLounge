package com.kooy29.liarslounge.api.nms;

import org.bukkit.entity.Player;

public interface CustomConnectionWrapper {
    void injectPlayer(Player player);

    void removePlayer(Player player);
}
