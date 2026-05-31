package com.kooy29.liarslounge.api.arena;

import com.kooy29.liarslounge.api.storage.IPlayerStats;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public interface IArena {

    List<CardType> DEFAULT_DECK = Arrays.asList(
            CardType.KING, CardType.KING, CardType.KING, CardType.KING, CardType.KING, CardType.KING,
            CardType.QUEEN, CardType.QUEEN, CardType.QUEEN, CardType.QUEEN, CardType.QUEEN, CardType.QUEEN,
            CardType.ACE, CardType.ACE, CardType.ACE, CardType.ACE, CardType.ACE, CardType.ACE,
            CardType.JOKER, CardType.JOKER
    );

    String SEAT_CUSTOM_NAME = "liarslounge_seat";

    Map<Player, GamePlayer> gamePlayers = new HashMap<>();

    String getName();

    String getGroup();

    World getWorld();

    int getMinPlayers();

    List<Player> getPlayers();

    Location getWaitingLocation();

    List<Location> getChairLocations();

    List<Location> getActionItemsLocation();

    Location getTableLocation();

    GameState getGameState();

    CardType getTableCardType();

    int getCurrentRound();

    void updateWaitingSbLinesAsync(int seconds);

    void updateInGameSbLines();

    void addPlayer(Player p);

    void removePlayer(Player p);

    Set<Player> getSpectators();

    void addSpectator(Player p);

    void removeSpectator(Player p);

    boolean isSpectator(Player p);

    void makeSpectator(Player p, boolean teleport, boolean initHolo);

    void start(boolean instantStart);

    void cancelStartNotify();

    boolean checkAndFinish();

    void end();

    void selectCard(GamePlayer p, int slot, String cardConfigPath);

    void throwCard(GamePlayer player, List<CardType> cards, Runnable callback);

    boolean hasSelectedCard(GamePlayer player, int slot);

    List<Card> getSelectedCards(GamePlayer player);

    void deselectCard(GamePlayer player, int slot, String cardConfigPath);

    void deselectAllCards(GamePlayer player);

    boolean isCurrentTurn(GamePlayer player);

    boolean isAnimating();

    boolean isCurrentTurn(Player player);

    void notifyPlayersOfTurn();

    boolean canCallLiar();

    void callLiar(GamePlayer accuser, GamePlayer target);

    void shoot(GamePlayer mainPlayer, GamePlayer secondPlayer);

    void setQuickStart(int seconds);

    boolean isEnding();

    void sendDebugMsg(String msg);

    LastPlayed getLastPlayed();

    class GamePlayer {
        public Player bukkitPlayer;
        public IArena arena;
        public int shots;
        public List<Card> cards;
        public int calls;
        public int eliminations;
        public boolean pro_tip;
        public Map<GamePlayer, Object> holograms;
        public IPlayerStats stats;

        public GamePlayer(Player bukkitPlayer, IArena arena, int shots, List<Card> cards) {
            this.bukkitPlayer = bukkitPlayer;
            this.arena = arena;
            this.shots = shots;
            this.cards = cards;
            this.calls = 0;
            this.eliminations = 0;
            this.pro_tip = true;
            holograms = new HashMap<>();
        }

        public static void resetPlayer(Player p) {
            GamePlayer gp = gamePlayers.get(p);
            resetPlayer(gp);
        }

        public static void resetPlayer(GamePlayer gp) {
            gp.arena = null;
            gp.shots = 6;
            gp.cards = null;
            gp.calls = 0;
            gp.eliminations = 0;
            gp.holograms.clear();
        }
    }

    class Card {
        public CardType cardType;
        public int slot;
        public boolean isSelected;

        public Card(CardType cardType, int slot, boolean isSelected) {
            this.cardType = cardType;
            this.slot = slot;
            this.isSelected = isSelected;
        }
    }

    class LastPlayed {
        public Player player;
        public List<CardType> cardType;

        public LastPlayed(Player player, List<CardType> cardType) {
            this.player = player;
            this.cardType = cardType;
        }
    }
}
