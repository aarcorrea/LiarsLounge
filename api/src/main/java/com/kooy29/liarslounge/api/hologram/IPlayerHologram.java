package com.kooy29.liarslounge.api.hologram;

import com.kooy29.liarslounge.api.arena.IArena;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

public interface IPlayerHologram {
    IHologram getHoloSetupSession(Player player, String msg, Location loc);

    void initHealthHolo(IArena.GamePlayer mainGp, List<Player> playingPlayers);

    void updateHealthHolo(IArena.GamePlayer mainGp);

    void removeAllHolo(World world);

    void removePlayerHoloFromAll(World world, IArena.GamePlayer target);

    void removePlayerHoloSelf(IArena.GamePlayer player);
}
