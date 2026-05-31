package com.kooy29.liarslounge.api.hologram;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public interface IHologram {
    Player getPlayer();

    Location getLocation();

    List<IHologramLine> getLines();

    IHologramLine getLine(int index);

    void addLine(IHologramLine line);

    void setLine(int index, String text, boolean update);

    void removeLine(IHologramLine line);

    void removeLine(int index);

    void clearLines();

    int size();

    double getGap();

    void setGap(double gap);

    void update();

    void show();

    void hide();

    void remove();

    boolean isShowing();
}
