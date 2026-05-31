package com.kooy29.liarslounge.nms.v1_21_R7.hologram;

import com.kooy29.liarslounge.api.hologram.IHologram;
import com.kooy29.liarslounge.api.hologram.IHologramLine;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Hologram implements IHologram {

    private final Player viewer;
    private final Location origin;
    private final List<IHologramLine> lines = new ArrayList<>();
    private boolean visible = true;
    private double lineSpacing = 0.25D;

    public Hologram(Player viewer, Location origin, List<String> content) {
        this.viewer = viewer;
        this.origin = origin.clone();

        content.forEach(text -> appendLine(new HologramLine(text, this))
        );
    }

    @Override
    public Player getPlayer() {
        return viewer;
    }

    @Override
    public Location getLocation() {
        return origin.clone();
    }

    @Override
    public List<IHologramLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    @Override
    public IHologramLine getLine(int index) {
        return index >= 0 && index < lines.size()
                ? lines.get(index)
                : null;
    }

    @Override
    public void addLine(IHologramLine line) {
        appendLine(line);
    }

    public IHologramLine appendLine(String text) {
        IHologramLine line = new HologramLine(text, this);
        appendLine(line);
        return line;
    }

    private void appendLine(IHologramLine line) {
        if (line.hologram() == null) {
            line.hologram(this);
        }
        lines.add(line);
    }

    @Override
    public void setLine(int index, String text, boolean refresh) {
        while (lines.size() <= index) {
            lines.add(null);
        }
        IHologramLine current = lines.get(index);
        if (current == null) {
            lines.set(index, new HologramLine(text, this));
            return;
        }
        current.text(text, refresh);
    }

    @Override
    public void removeLine(IHologramLine line) {
        if (line == null) {
            return;
        }
        line.hide();
        lines.remove(line);
    }

    @Override
    public void removeLine(int index) {
        if (index < 0 || index >= lines.size()) {
            return;
        }
        IHologramLine line = lines.remove(index);
        if (line != null) {
            line.hide();
        }
    }

    @Override
    public void clearLines() {
        forEachLine(IHologramLine::hide);
        lines.clear();
    }

    @Override
    public int size() {
        return lines.size();
    }

    @Override
    public double getGap() {
        return lineSpacing;
    }

    @Override
    public void setGap(double gap) {
        this.lineSpacing = gap;
        update();
    }

    @Override
    public void update() {
        forEachLine(IHologramLine::refresh);
    }

    @Override
    public void show() {
        visible = true;
        forEachLine(IHologramLine::show);
    }

    @Override
    public void hide() {
        visible = false;
        forEachLine(IHologramLine::hide);
    }

    @Override
    public void remove() {
        clearLines();
    }

    @Override
    public boolean isShowing() {
        return visible;
    }

    private void forEachLine(Consumer<IHologramLine> consumer) {
        lines.stream()
                .filter(Objects::nonNull)
                .forEach(consumer);
    }
}