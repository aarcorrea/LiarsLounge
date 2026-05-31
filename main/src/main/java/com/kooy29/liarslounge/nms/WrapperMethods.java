package com.kooy29.liarslounge.nms;

import com.kooy29.liarslounge.LiarsLounge;
import com.kooy29.liarslounge.api.arena.GameState;
import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.api.nms.IWrapperMethods;
import com.kooy29.liarslounge.utils.ExtraUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WrapperMethods implements IWrapperMethods {

    @Override
    public boolean canPlayerUnmount(Player player) {
        if (!LiarsLounge.getInstance().getArenaManager().isPlayerInArena(player)) return true;
        IArena arena = LiarsLounge.getInstance().getArenaManager().getArenaByPlayer(player);
        return arena.isSpectator(player)
                || arena.getGameState() != GameState.PLAYING || arena.isEnding();
    }

    @Override
    public boolean isPlayerInArena(Player player) {
        return LiarsLounge.getInstance().getArenaManager().isPlayerInArena(player);
    }

    @Override
    public ItemStack getHiddenCardItem() {
        return ExtraUtil.getHiddenCardItem();
    }
}
