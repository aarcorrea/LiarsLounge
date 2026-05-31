package com.kooy29.liarslounge.api.hologram;

public interface IHologramLine {

    String text();

    void text(String text);

    void text(String text, boolean refresh);

    IHologram hologram();

    void hologram(IHologram hologram);

    void refresh();

    void show();

    void hide();

    void destroy();

    boolean destroyed();
}