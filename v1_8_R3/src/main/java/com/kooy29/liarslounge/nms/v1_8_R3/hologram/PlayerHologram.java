package com.kooy29.liarslounge.nms.v1_8_R3.hologram;

import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.api.hologram.IHologram;
import com.kooy29.liarslounge.api.hologram.IPlayerHologram;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PlayerHologram implements IPlayerHologram {
    @Override
    public IHologram getHoloSS(Player player, String msg, Location loc) {
        return new Hologram(player, new Location(loc.getWorld(), loc.getBlockX() + 0.5, loc.getBlockY() - 1.2, loc.getBlockZ() + 0.5), MsgUtil.colorize(Collections.singletonList(msg)));
    }

    @Override
    public void initHealthHolo(IArena.GamePlayer mainGp, List<Player> playingPlayers) {
        for (Player p : playingPlayers) {
            IArena.GamePlayer gp1 = IArena.gamePlayers.get(p);
            if (mainGp == gp1) continue;
            IHologram holo = new Hologram(mainGp.bukkitPlayer, p.getEyeLocation().clone().subtract(0, 2.25, 0), Arrays.asList(MsgUtil.colorize("&c❤ &7(" + gp1.shots + "/" + "6" + ")"), p.getName()));
            holo.show();
            mainGp.holograms.put(gp1, holo);
        }
    }

    @Override
    public void updateHealthHolo(IArena.GamePlayer mainGp) {
        for (Map.Entry<IArena.GamePlayer, Object> entry : mainGp.holograms.entrySet()) {
            IHologram h = (Hologram) entry.getValue();
            h.setLine(0, MsgUtil.colorize("&c❤ &7(" + entry.getKey().shots + "/" + "6" + ")"), true);
        }
    }

    @Override
    public void removeAllHolo(World world) {
        for (Player p : world.getPlayers()) {
            IArena.GamePlayer gp = IArena.gamePlayers.get(p);
            removePlayerHoloSelf(gp);
        }
    }

    @Override
    public void removePlayerHoloFromAll(World world, IArena.GamePlayer target) {
        for (Player p : world.getPlayers()) {
            IArena.GamePlayer gp = IArena.gamePlayers.get(p);
            if (gp.holograms.containsKey(target)) {
                IHologram holo = (Hologram) gp.holograms.get(target);
                holo.remove();
                gp.holograms.remove(target);
            }
        }
    }

    @Override
    public void removePlayerHoloSelf(IArena.GamePlayer player) {
        for (Map.Entry<IArena.GamePlayer, Object> entry : player.holograms.entrySet()) {
            IHologram holo = (Hologram) entry.getValue();
            holo.remove();
        }
        player.holograms.clear();
    }
}
