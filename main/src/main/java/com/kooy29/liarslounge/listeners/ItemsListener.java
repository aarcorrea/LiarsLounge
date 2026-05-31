package com.kooy29.liarslounge.listeners;

import com.kooy29.liarslounge.LiarsLounge;
import com.kooy29.liarslounge.api.arena.CardType;
import com.kooy29.liarslounge.api.arena.GameState;
import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.api.arena.IArenaManager;
import com.kooy29.liarslounge.api.nms.IVersionWrapper;
import com.kooy29.liarslounge.storage.MapBuild;
import com.kooy29.liarslounge.storage.yaml.ConfigPath;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.storage.yaml.SoundsPath;
import com.kooy29.liarslounge.utils.ExtraUtil;
import com.kooy29.liarslounge.utils.MsgUtil;
import com.kooy29.liarslounge.utils.SoundUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemsListener implements Listener {

    IVersionWrapper nms;
    IArenaManager arenaManager;
    boolean protipEnabled;

    public ItemsListener(IVersionWrapper nms, IArenaManager arenaManager, FileConfiguration config) {
        this.nms = nms;
        this.arenaManager = arenaManager;
        this.protipEnabled = config.getBoolean(ConfigPath.SEND_PROTIP, true);
    }

    @EventHandler
    public void onDropListener(PlayerDropItemEvent e) {
        Player player = e.getPlayer();

        IArena.GamePlayer gamePlayer = arenaManager.getGamePlayer(player);
        if (gamePlayer == null) return;

        IArena arena = gamePlayer.arena;

        if (arena == null) {
            if (player.getWorld() == ExtraUtil.getLobbyLocation().getWorld())
                e.setCancelled(true);
            return;
        }

        if (arena.getGameState() != GameState.PLAYING) {
            e.setCancelled(true);
            return;
        }

        if (arena.isSpectator(player)) {
            e.setCancelled(true);
            return;
        }

        if (!arena.isCurrentTurn(gamePlayer)) {
            MsgUtil.sendConfigMessage(player, MsgPath.Game.NOT_YOUR_TURN);
            e.setCancelled(true);
            return;
        }

        ItemStack item = e.getItemDrop().getItemStack();

        if (item == null) return;

        if (arena.isAnimating()) {
            e.setCancelled(true);
            return;
        }

        if (nms.isCustomItem(item)) {
            if (nms.getCustomData(item).equals("LIAR_ITEM")) {
                e.setCancelled(true);
                return;
            }
            List<IArena.Card> items = arena.getSelectedCards(gamePlayer);

            if (items == null || items.isEmpty()) {
                boolean showProTip = gamePlayer.pro_tip && protipEnabled;
                MsgUtil.sendConfigMessage(player, MsgPath.Game.CARD_NOT_SELECTED);
                if (showProTip) MsgUtil.sendConfigMessage(player, MsgPath.Game.PROTIP);
                e.setCancelled(true);
                return;
            }

            boolean hadHandItem = false;
            int handSlot = player.getInventory().getHeldItemSlot();
            ItemStack handItem = e.getItemDrop().getItemStack();

            List<CardType> cardTypes = new ArrayList<>();
            for (IArena.Card card : items) {
                ItemStack itemStack;
                if (card.slot == handSlot) {
                    itemStack = handItem;
                    hadHandItem = true;
                } else
                    itemStack = player.getInventory().getItem(card.slot);

                if (itemStack != null && nms.getCustomData(itemStack).startsWith("CARD_")) {
                    cardTypes.add(CardType.fromName(nms.getCustomData(itemStack).split("_")[1]));
                }
                player.getInventory().setItem(card.slot, null);
            }

            String msg = "";
            if (cardTypes.size() > 1)
                msg = MsgUtil.getConfigMessage(MsgPath.Game.CARDS_DROPPED).replace("%cards%", cardTypes.size() + "").replace("%player_name%", player.getName());
            else
                msg = MsgUtil.getConfigMessage(MsgPath.Game.CARD_DROPPED).replace("%player_name%", player.getName());

            arena.throwCard(gamePlayer, cardTypes, arena::notifyPlayersOfTurn);

            MsgUtil.sendMessage(msg, arena.getPlayers(), arena.getSpectators());
            if (hadHandItem) {
                e.getItemDrop().remove();
            } else {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void onEntityClickListener(PlayerInteractEntityEvent e) {
        handle(e);
    }

    @EventHandler
    public void onItemFrameInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() == null) return;
        if (event.getRightClicked().getWorld() == ExtraUtil.getLobbyLocation().getWorld() || arenaManager.isArenaByWorld(event.getRightClicked().getWorld().getName())) {
            if (event.getRightClicked().getType() == EntityType.ITEM_FRAME) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onItemFrameClickListener(EntityDamageByEntityEvent e) {
        if (e.getEntity().getWorld() == ExtraUtil.getLobbyLocation().getWorld() || arenaManager.isArenaByWorld(e.getEntity().getWorld().getName())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent e) {
        if (e.getEntity().getWorld() == ExtraUtil.getLobbyLocation().getWorld() || arenaManager.isArenaByWorld(e.getEntity().getWorld().getName())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClickListener(PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL) return;
        handle(e);
    }

    @EventHandler
    public void onWaitingClickListener(PlayerInteractEvent e) {
        if (e.getAction() == Action.PHYSICAL) return;

        ItemStack item = e.getItem();

        if (item == null) return;

        Player player = e.getPlayer();
        IArena arena = arenaManager.getArenaByPlayer(player);
        if (arena == null || arena.getGameState() == GameState.PLAYING) return;

        String itemData = nms.getCustomData(item);
        e.setCancelled(true);

        if (itemData.equals("LEAVE_ITEM")) {
            arena.removePlayer(player);
            arena.removeSpectator(player);
            player.teleport(ExtraUtil.getLobbyLocation());
            MsgUtil.sendConfigMessage(player, MsgPath.Success.Arena.LEAVE);
        } else if (itemData.equals("GUIDE_ITEM")) {
            LiarsLounge.getInstance().getBookGUI().open(player);
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent e) { // TODO: 1.12+ event replaced with EntityPickupItemEvent, fix for 1.8.8+ ver
        if (e.getPlayer().getWorld() == ExtraUtil.getLobbyLocation().getWorld() || arenaManager.isPlayerInArena(e.getPlayer()))
            e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player player = (Player) e.getWhoClicked();
        if (MapBuild.canBuild(player) && !nms.isCustomItem(e.getCurrentItem())) return;
        if (player.getWorld().equals(ExtraUtil.getLobbyLocation().getWorld()) || arenaManager.isPlayerInArena(player)) {
            e.setCancelled(true);

            // Prevent hotbar swapping with number keys
            if (e.getClick() == ClickType.NUMBER_KEY) {
                player.updateInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player player = (Player) e.getWhoClicked();
        if (MapBuild.canBuild(player) && (!nms.isCustomItem(e.getCursor())) || !nms.isCustomItem(e.getOldCursor()))
            return;

        if (player.getWorld().equals(ExtraUtil.getLobbyLocation().getWorld()) || arenaManager.isPlayerInArena(player)) {
            e.setCancelled(true);
            player.updateInventory(); // Check for desync
        }
    }

    public <T extends PlayerEvent & Cancellable> void handle(T e) {
        ItemStack item = e.getPlayer().getItemInHand();

        if (item == null) return;

        Player player = e.getPlayer();
        IArena.GamePlayer gamePlayer = arenaManager.getGamePlayer(player);
        if (gamePlayer == null) return;

        int itemSlot = player.getInventory().getHeldItemSlot();

        IArena arena = gamePlayer.arena;

        if (arena == null || arena.getGameState() != GameState.PLAYING) return;
        if (arena.isSpectator(player)) {
            e.setCancelled(true);
            return;
        }

        if (arena.isAnimating()) {
            e.setCancelled(true);
            return;
        }

        if (!arena.isCurrentTurn(gamePlayer)) {
            MsgUtil.sendConfigMessage(player, MsgPath.Game.NOT_YOUR_TURN);
            e.setCancelled(true);
            return;
        }

        String itemData = nms.getCustomData(item);

        if (itemData.equals("LIAR_ITEM")) {
            if (!arena.canCallLiar()) {
                MsgUtil.sendConfigMessage(player, MsgPath.Game.CANNOT_CALL_LIAR);
                return;
            }

            arena.callLiar(gamePlayer, arenaManager.getGamePlayer(arena.getLastPlayed().player));
            e.setCancelled(true);
        } else if (itemData.startsWith("CARD_")) {

            String cardName = itemData.replace("CARD_", "");
            if (arena.hasSelectedCard(gamePlayer, itemSlot)) {
                arena.deselectCard(gamePlayer, itemSlot, cardName);
                SoundUtil.playSound(player, SoundsPath.Card.SELECT);
                player.sendMessage(MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.CARD_DESELECT).replace("%card%", MsgUtil.getConfigMessage(MsgPath.Game.Cards.CARD.replace("%card%", cardName)))));
            } else {
                if (arena.getSelectedCards(gamePlayer).size() >= 3) {
                    MsgUtil.sendConfigMessage(player, MsgPath.Game.CARD_SELECT_LIMIT);
                    e.setCancelled(true);
                    return;
                }
                SoundUtil.playSound(player, SoundsPath.Card.DESELECT);
                arena.selectCard(gamePlayer, itemSlot, cardName);
                player.sendMessage(MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.CARD_SELECT).replace("%card%", MsgUtil.getConfigMessage(MsgPath.Game.Cards.CARD.replace("%card%", cardName)))));
            }
        }

        e.setCancelled(true);
    }
}
