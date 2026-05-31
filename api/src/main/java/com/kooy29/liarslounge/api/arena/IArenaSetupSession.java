package com.kooy29.liarslounge.api.arena;

import com.kooy29.liarslounge.api.hologram.IHologram;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;

public interface IArenaSetupSession {
    String getArenaName();

    World getWorld();

    Location getWaitingLocation();

    void setWaitingLocation(Location waitingLocation);

    Location getTableLocation();

    void setTableLocation(Location tableLocation);

    List<IArenaSetupSession.HoloLoc> getChairLocations();

    void addChairLocation(IArenaSetupSession.HoloLoc holoLoc);

    void removeChairLocation(int index);

    List<IArenaSetupSession.HoloLoc> getActionItemsLocation();

    void addActionItemsLocation(IArenaSetupSession.HoloLoc holoLoc);

    void removeActionItemsLocation(int index);

    boolean saveSetup();

    class HoloLoc {
        private final Location location;
        private final IHologram hologram;

        public HoloLoc(Location location, IHologram hologram) {
            this.location = location;
            this.hologram = hologram;
        }

        public Location getLocation() {
            return location;
        }

        public IHologram getHologram() {
            return hologram;
        }
    }
}
