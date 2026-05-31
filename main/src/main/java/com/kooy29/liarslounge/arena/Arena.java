package com.kooy29.liarslounge.arena;

import com.kooy29.liarslounge.LiarsLounge;
import com.kooy29.liarslounge.api.animation.ICardReveal;
import com.kooy29.liarslounge.api.animation.ICardThrow;
import com.kooy29.liarslounge.api.animation.ILiarCall;
import com.kooy29.liarslounge.api.animation.ITableCard;
import com.kooy29.liarslounge.api.arena.CardType;
import com.kooy29.liarslounge.api.arena.GameState;
import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.api.arena.IArenaManager;
import com.kooy29.liarslounge.api.hologram.IPlayerHologram;
import com.kooy29.liarslounge.api.storage.IConfiguration;
import com.kooy29.liarslounge.api.storage.IPlayerStats;
import com.kooy29.liarslounge.scoreboard.Scoreboard;
import com.kooy29.liarslounge.scoreboard.ScoreboardManager;
import com.kooy29.liarslounge.storage.yaml.ConfigPath;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.storage.yaml.SoundsPath;
import com.kooy29.liarslounge.storage.yaml.ValuesPath;
import com.kooy29.liarslounge.utils.*;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class Arena implements IArena {

    CardType tableCardType = null;
    ConfigurationSection valuesConfig;
    IConfiguration arenaConfig;
    String arenaName;
    String group;
    World world;
    Location waitingLocation;
    Location tableLocation;
    GameState gameState;
    List<Location> chairLocations;
    List<Location> actionItemsLocation;
    List<Player> players;
    List<IPlayerStats> playerStats;
    Set<Player> spectators;
    List<String> latestScoreboardLines;
    Scoreboard scoreboard;
    LiarsLounge instance = LiarsLounge.getInstance();
    ICardThrow cardThrow;
    ICardReveal cardReveal;
    ITableCard tableCard;
    ILiarCall liarCall;
    int cardThrowTimeout;
    int round = 1;
    int currentTurn = 0;
    boolean isAnimating = false;
    int minimumPlayers;
    int autoStartDelay;
    int fullStartDelay;
    int startTaskId = -1;
    int timeoutTaskId = -1;
    LastPlayed lastPlayed;
    boolean isEnding = false;
    int quickStart = -1;
    boolean debug = false;
    private IPlayerHologram playerHolo = instance.getPlayerHolo();


    public Arena(String name, IConfiguration arenaConfig) {
        this.arenaName = name;
        this.valuesConfig = instance.getValuesConfig().getConfig();
        this.arenaConfig = arenaConfig;
        this.group = arenaConfig.getConfig().getString(ConfigPath.ARENA_GROUP, "default");
        this.world = ExtraUtil.loadExistingWorld(arenaConfig.getConfig().getString(ConfigPath.ARENA_WORLD));
        ConfigurationSection waiting = arenaConfig.getConfig().getConfigurationSection(ConfigPath.ARENA_WAITING);
        this.minimumPlayers = waiting.getInt(ConfigPath.ARENA_MIN_PLAYERS, 2);
        this.autoStartDelay = waiting.getInt(ConfigPath.ARENA_AUTO_START_DELAY, 20);
        this.fullStartDelay = waiting.getInt(ConfigPath.ARENA_START_DELAY_ON_FULL, 5);
        this.cardThrowTimeout = waiting.getInt(ConfigPath.ARENA_CARD_THROW_TIMEOUT, 60);
        waiting = waiting.getConfigurationSection(ConfigPath.LOCATION);
        this.waitingLocation = new Location(this.world, waiting.getDouble("x"), waiting.getDouble("y"), waiting.getDouble("z"), (float) waiting.getDouble("yaw"), (float) waiting.getDouble("pitch"));
        ConfigurationSection table = arenaConfig.getConfig().getConfigurationSection(ConfigPath.ARENA_TABLE + ConfigPath.LOCATION);
        this.tableLocation = new Location(this.world, table.getDouble("x"), table.getDouble("y"), table.getDouble("z"), 0f, 0f);
        ConfigurationSection chair = arenaConfig.getConfig().getConfigurationSection(ConfigPath.ARENA_CHAIRS);
        this.chairLocations = new ArrayList<>();
        if (chair != null) {
            for (String key : chair.getKeys(false)) {
                ConfigurationSection chairSec = chair.getConfigurationSection(key);
                if (chairSec != null) {
                    Location chairLoc = new Location(this.world,
                            chairSec.getDouble("x"),
                            chairSec.getDouble("y"),
                            chairSec.getDouble("z"),
                            (float) chairSec.getDouble("yaw"),
                            (float) chairSec.getDouble("pitch"));
                    this.chairLocations.add(chairLoc);
                }
            }
        }
        this.chairLocations.subList(4, this.chairLocations.size()).clear();
        ConfigurationSection actionItems = arenaConfig.getConfig().getConfigurationSection(ConfigPath.ARENA_ACTION_ITEMS);
        this.actionItemsLocation = new ArrayList<>();
        if (actionItems != null) {
            for (String key : actionItems.getKeys(false)) {
                ConfigurationSection chairSec = actionItems.getConfigurationSection(key);
                if (chairSec != null) {
                    Location chairLoc = new Location(this.world,
                            chairSec.getDouble("x"),
                            chairSec.getDouble("y"),
                            chairSec.getDouble("z"),
                            0f,
                            0f);
                    this.actionItemsLocation.add(chairLoc);
                }
            }
        }
        this.actionItemsLocation.subList(4, this.actionItemsLocation.size()).clear();
        this.players = new ArrayList<>();
        this.playerStats = new ArrayList<>();
        this.spectators = new HashSet<>();
        this.scoreboard = new Scoreboard(this);
        this.scoreboard.waiting.setSRLines();
        this.scoreboard.inGame.setSRLines();
        this.gameState = GameState.WAITING;
        try {
            this.cardReveal = (ICardReveal) AnimationUtil.getCardReveal().newInstance(instance, this);
            this.cardThrow = (ICardThrow) AnimationUtil.getCardThrow().newInstance(instance, this);
            this.liarCall = (ILiarCall) AnimationUtil.getLiarCall().newInstance(instance, this);
            this.tableCard = (ITableCard) AnimationUtil.getTableCard().newInstance(instance, this);
        } catch (Exception ex) {
            Bukkit.getLogger().severe("Couldn't initialize animations");
            ex.printStackTrace();
        }
        ExtraUtil.setGameRules(world);
    }

    @Override
    public String getName() {
        return arenaName;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public void addPlayer(Player p) {
        sendDebugMsg("Added player to arena " + p.getName());
        players.add(p);
        gamePlayers.get(p).arena = this;
        Bukkit.getScheduler().runTaskLater(instance, () -> sendConnectionMsg(true, p), 1L);
        if (gameState != GameState.PLAYING) {
            p.getInventory().setItem(valuesConfig.getInt(ValuesPath.Game.Items.LEAVE_ITEM + ".slot", 8), IArenaManager.getGameItem("leave_item"));
            p.getInventory().setItem(0, IArenaManager.getGameItem("guide_item"));
        }
        boolean isStart = gameState != GameState.PLAYING && players.size() >= minimumPlayers;
        if (isStart) {
            start(false);
        }
        FastBoard board = ScoreboardManager.createBoard(p);
        if (isStart) {
            board.updateTitle(scoreboard.waiting.getTitle());
            board.updateLines(latestScoreboardLines);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                if (!p.isOnline()) return;
                if (gameState == GameState.WAITING) {
                    board.updateTitle(scoreboard.waiting.getTitle());
                    updateWaitingSbLinesAsync(-1);
                } else if (gameState == GameState.PLAYING) {
                    board.updateTitle(scoreboard.inGame.getTitle());
                    updateInGameSbLinesAsync();
                }
            });
        }
    }

    @Override
    public void removePlayer(Player p) {
        players.remove(p);
        if (gameState == GameState.PLAYING) {
            liarCall.destroyAxe(p, -1);
            if (currentTurn == players.indexOf(p)) {
                timeoutTaskId = -1;
                if (currentTurn == players.size() - 1) restartGame(true);
                else {
                    currentTurn++;
                    notifyPlayersOfTurn();
                }
            }
            GamePlayer gp = gamePlayers.get(p);
            playerHolo.removePlayerHoloSelf(gp);
            playerHolo.removePlayerHoloFromAll(world, gp);
            p.setMaxHealth(20);
            p.setHealth(20);
            ExtraUtil.destroySeat(p);
            p.removePotionEffect(PotionEffectType.BLINDNESS);
            p.removePotionEffect(PotionEffectType.SLOW);
            for (Player player : spectators) {
                p.showPlayer(player);
            }
            Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                if (!p.isOnline()) return;
                instance.getVersionWrapper().sendShowNametag(p, new ArrayList<>(players));
            });
            if (players.size() <= 1) checkAndFinish();
        }
        GamePlayer.resetPlayer(p);
        Bukkit.getScheduler().runTaskLater(instance, () -> sendConnectionMsg(false, p), 1L);
        ScoreboardManager.removeBoard(p);
        p.setAllowFlight(false);
        p.setFlying(false);
        p.getInventory().clear();
        updateInGameSbLinesAsync();
    }

    private void sendConnectionMsg(boolean join, Player player) {
        String msg = MsgUtil.colorize(join ? MsgUtil.getConfigMessage(MsgPath.Game.JOIN) : MsgUtil.getConfigMessage(MsgPath.Game.LEFT)).replace("%player_name%", player.getName());
        for (Player p : players) {
            if (player == p) continue;
            p.sendMessage(msg);
        }
        for (Player p : spectators) {
            p.sendMessage(msg);
        }
    }

    @Override
    public void addSpectator(Player p) {
        if (isEnding) return;
        spectators.add(p);
        gamePlayers.get(p).arena = this;
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            if (!p.isOnline() || isEnding) return;
            FastBoard board = ScoreboardManager.createIfNotExist(p);
            board.updateTitle(scoreboard.waiting.getTitle());
            if (latestScoreboardLines.isEmpty()) latestScoreboardLines = scoreboard.waiting.getNSRLines(0);
            board.updateLines(latestScoreboardLines);
        });
    }

    @Override
    public void removeSpectator(Player p) {
        if (spectators.remove(p)) {
            p.getInventory().clear();
            GamePlayer gp = gamePlayers.get(p);
            playerHolo.removePlayerHoloSelf(gp);
            GamePlayer.resetPlayer(gp);
            Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                instance.getVersionWrapper().sendShowNametag(p, new ArrayList<>(players));
                ScoreboardManager.removeBoard(p);
            });
            for (Player player : players) {
                player.showPlayer(p);
            }
            p.setAllowFlight(false);
            p.setFlying(false);
        }
    }

    @Override
    public boolean isSpectator(Player p) {
        return spectators.contains(p);
    }

    @Override
    public void makeSpectator(Player p, boolean teleport, boolean initHolo) {
        if (isEnding || !p.isOnline()) return;
        for (Player player : players) {
            player.hidePlayer(p);
        }
        addSpectator(p);
        instance.getVersionWrapper().sendHideNametag(p, world.getPlayers());
        Location spectatingLocation = p.getLocation().add(0, 1, 0);
        p.leaveVehicle();
        p.setMaxHealth(20);
        p.setHealth(20);
        ExtraUtil.destroySeat(p);
        p.setAllowFlight(true);
        p.setFlying(true);
        p.setAllowFlight(true);
        players.remove(p);
        p.getInventory().clear();
        if (teleport) p.teleport(tableLocation);
        else
            p.teleport(spectatingLocation);
        if (initHolo) playerHolo.initHealthHolo(gamePlayers.get(p), players);
    }

    @Override
    public void updateWaitingSbLinesAsync(int seconds) {
        latestScoreboardLines = scoreboard.waiting.getNSRLines(seconds);
        for (Player player : new ArrayList<>(players)) {
            FastBoard board = ScoreboardManager.getBoard(player);
            if (board != null)
                board.updateLines(latestScoreboardLines);
        }
        for (Player player : new ArrayList<>(spectators)) {
            FastBoard board = ScoreboardManager.getBoard(player);
            if (board != null)
                board.updateLines(latestScoreboardLines);
        }
    }

    @Override
    public void updateInGameSbLines() {
        latestScoreboardLines = scoreboard.inGame.getNSRLines(null);
        for (Player player : players) {
            ScoreboardManager.getBoard(player).updateLines(scoreboard.inGame.getNSRLines(gamePlayers.get(player)));
        }
        for (Player player : spectators) {
            ScoreboardManager.getBoard(player).updateLines(latestScoreboardLines);
        }
    }


    private void updateInGameSbLinesAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            latestScoreboardLines = scoreboard.inGame.getNSRLines(null);
            for (Player player : new ArrayList<>(players)) {
                FastBoard board = ScoreboardManager.getBoard(player);
                if (board != null)
                    ScoreboardManager.getBoard(player).updateLines(scoreboard.inGame.getNSRLines(gamePlayers.get(player)));
            }
            for (Player player : new ArrayList<>(spectators)) {
                FastBoard board = ScoreboardManager.getBoard(player);
                if (board != null)
                    ScoreboardManager.getBoard(player).updateLines(latestScoreboardLines);
            }
        });
    }

    private void updateInGameSbLinesWTAsync() {
        // WT - With Title
        latestScoreboardLines = scoreboard.inGame.getNSRLines(null);
        for (Player player : new ArrayList<>(players)) {
            FastBoard board = ScoreboardManager.getBoard(player);
            board.updateTitle(scoreboard.inGame.getTitle());
            board.updateLines(scoreboard.inGame.getNSRLines(gamePlayers.get(player)));
        }
        for (Player player : new ArrayList<>(spectators)) {
            FastBoard board = ScoreboardManager.getBoard(player);
            board.updateTitle(scoreboard.inGame.getTitle());
            board.updateLines(latestScoreboardLines);
        }
    }

    @Override
    public int getMinPlayers() {
        return minimumPlayers;
    }

    @Override
    public List<Player> getPlayers() {
        return players;
    }

    @Override
    public Set<Player> getSpectators() {
        return spectators;
    }

    @Override
    public Location getWaitingLocation() {
        return waitingLocation;
    }

    @Override
    public List<Location> getChairLocations() {
        return chairLocations;
    }

    @Override
    public List<Location> getActionItemsLocation() {
        return actionItemsLocation;
    }

    @Override
    public Location getTableLocation() {
        return tableLocation;
    }

    @Override
    public GameState getGameState() {
        return gameState;
    }

    @Override
    public int getCurrentRound() {
        return round;
    }

    @Override
    public CardType getTableCardType() {
        return tableCardType;
    }

    @Override
    public boolean canCallLiar() {
        return lastPlayed != null;
    }

    @Override
    public void callLiar(GamePlayer accuser, GamePlayer target) {
        if (!canCallLiar()) return;
        accuser.calls++;
        timeoutTaskId = -1;
        isAnimating = true;
        updateInGameSbLinesAsync();
        deselectAllCards(accuser);
        String msg = MsgUtil.getConfigMessage(MsgPath.Game.CallLiar.INITIAL).replace("%accuser_name%", accuser.bukkitPlayer.getName()).replace("%target_name%", target.bukkitPlayer.getName());
        MsgUtil.sendMessage(msg, players, spectators);
        List<CardType> cardTypes = lastPlayed.cardType;
        lastPlayed = null;
        boolean isTruth = cardTypes.stream()
                .allMatch(cardType -> cardType == tableCardType || cardType == CardType.JOKER);

        ItemStack[] cardItems = new ItemStack[cardTypes.size()];
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            for (int i = 0; i < cardTypes.size(); i++) {
                CardType currentCard = cardTypes.get(i);
                if (currentCard == CardType.JOKER || currentCard == tableCardType) {
                    cardItems[i] = IArenaManager.getGameItem(currentCard.name + "_verdict_right");
                } else {
                    cardItems[i] = IArenaManager.getGameItem(currentCard.name + "_verdict_wrong");
                }
            }

            for (Player p : new ArrayList<>(players)) {
                if (isEnding) {
                    cardReveal.forceStop();
                    break;
                }
                if (!target.bukkitPlayer.isOnline() || !accuser.bukkitPlayer.isOnline()) {
                    cardReveal.forceStop();
                    Bukkit.getScheduler().runTask(instance, () -> {
                        if (isEnding) return;
                        String msg2 = MsgUtil.getConfigMessage(MsgPath.Error.PLAYER_LEFT_RESTART)
                                .replace("%player_name%", !target.bukkitPlayer.isOnline()
                                        ? target.bukkitPlayer.getName()
                                        : accuser.bukkitPlayer.getName());
                        MsgUtil.sendMessage(msg2, players, spectators);
                        restartGame(true);
                    });
                    break;
                }
                if (!p.isOnline()) continue;

                SoundUtil.playSound(p, SoundsPath.CallLiar.CALL);
                cardReveal.revealTo(p, cardItems, accuser.bukkitPlayer, isTruth);
            }

            if (!spectators.isEmpty()) {
                cardReveal.revealToSpectators(spectators, cardItems, accuser.bukkitPlayer, isTruth);
            }
        });
        Bukkit.getScheduler().runTaskLater(instance, () -> {

            // Check if both players are still in the game

            if (isEnding) return;

            if (!players.contains(target.bukkitPlayer)) {
                currentTurn++;
                isAnimating = false;
                updateInGameSbLinesAsync();
                notifyPlayersOfTurn();
                return;
            }
            // Check ends

            if (isTruth) {
                String msg1 = MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.CallLiar.NOT_LIAR).replace("%accuser_name%", accuser.bukkitPlayer.getName()).replace("%target_name%", target.bukkitPlayer.getName()));
                for (Player player : players) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 180, 4, false, false));
                    player.sendMessage(msg1);
                    if (player == accuser.bukkitPlayer)
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 180, 0, false, false));
                    else
                        SoundUtil.playSound(player, SoundsPath.CallLiar.TRUTH);
                }
                for (Player player : spectators) {
                    player.sendMessage(msg1);
                }
                sendDebugMsg("Called callLiar for shoot(accuser, target)");
                shoot(accuser, target);
            } else {
                String msg1 = MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.CallLiar.IS_LIAR).replace("%accuser_name%", accuser.bukkitPlayer.getName()).replace("%target_name%", target.bukkitPlayer.getName()));
                for (Player player : players) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 180, 4, false, false));
                    player.sendMessage(msg1);
                    if (player == target.bukkitPlayer)
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 180, 0, false, false));
                    else
                        SoundUtil.playSound(player, SoundsPath.CallLiar.LIE);
                }
                for (Player player : spectators) {
                    player.sendMessage(msg1);
                }

                sendDebugMsg("Called callLiar for shoot(target, accuser)");
                shoot(target, accuser);
            }
        }, 30L);
    }

    @Override
    public void shoot(GamePlayer mainPlayer, GamePlayer secondPlayer) {
        liarCall.moveToPlayer(mainPlayer.bukkitPlayer, () -> {
            Bukkit.getScheduler().runTaskLater(instance, () -> {
                if (!players.contains(mainPlayer.bukkitPlayer)) return;
                if (luckShot(mainPlayer.shots)) {
                    playerStats.forEach(f -> {
                        if (f.getUuid() == mainPlayer.bukkitPlayer.getUniqueId())
                            f.addDeath();
                        if (f.getUuid() == secondPlayer.bukkitPlayer.getUniqueId()) {
                            f.addElimination();
                            f.addCalls();
                        }
                    });
                    liarCall.playAxeSwing(mainPlayer.bukkitPlayer, true, () -> {
                        if (!players.contains(mainPlayer.bukkitPlayer)) return;
                        String msg = MsgUtil.getConfigMessage(MsgPath.Game.ShotHit.MSG).replace("%player_name%", mainPlayer.bukkitPlayer.getName());
                        MsgUtil.sendMessage(msg, players, spectators);
                        secondPlayer.eliminations++;
                        instance.getVersionWrapper().sendTitle(mainPlayer.bukkitPlayer, MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.ShotHit.TITLE)), MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.ShotHit.SUBTITLE)),
                                valuesConfig.getInt(ValuesPath.Game.Titles.SHOT + ValuesPath.Game.Titles.FADE_IN, 10), valuesConfig.getInt(ValuesPath.Game.Titles.SHOT + ValuesPath.Game.Titles.STAY, 30), valuesConfig.getInt(ValuesPath.Game.Titles.SHOT + ValuesPath.Game.Titles.FADE_OUT, 10));
                        mainPlayer.bukkitPlayer.setMaxHealth(20);
                        mainPlayer.bukkitPlayer.setHealth(20);
                        makeSpectator(mainPlayer.bukkitPlayer, false, false);
                        playerHolo.removePlayerHoloFromAll(world, mainPlayer);
                        Bukkit.getScheduler().runTaskLater(instance, () -> {
                            if (!isEnding) restartGame(false);
                        }, 40L);
                    });
                } else {
                    playerStats.forEach(f -> {
                        if (f.getUuid() == secondPlayer.bukkitPlayer.getUniqueId())
                            f.addCalls();
                    });
                    liarCall.playAxeSwing(mainPlayer.bukkitPlayer, false, () -> {
                        if (!players.contains(mainPlayer.bukkitPlayer)) return;
                        String msg = MsgUtil.getConfigMessage(MsgPath.Game.SHOT_PASS).replace("%player_name%", mainPlayer.bukkitPlayer.getName());
                        MsgUtil.sendMessage(msg, players, spectators);
                        mainPlayer.shots -= 1;
                        int newHealth = (int) (mainPlayer.bukkitPlayer.getHealth() - 2);
                        if (newHealth > 0) mainPlayer.bukkitPlayer.setHealth(newHealth);
                        for (Player p : world.getPlayers()) playerHolo.updateHealthHolo(gamePlayers.get(p));
                        liarCall.moveBackToLoc(mainPlayer.bukkitPlayer);
                        currentTurn = players.indexOf(mainPlayer.bukkitPlayer);
                        Bukkit.getScheduler().runTaskLater(instance, () -> {
                            if (!isEnding) restartGame(false);
                        }, 40L);
                    });
                }
            }, 20L);
        });
    }

    public boolean luckShot(double luck) {
        if (luck <= 1) return true;

        double chance = 1.0 / luck;
        return Math.random() < chance;
    }

    @Override
    public void start(boolean instantStart) {
        sendDebugMsg("Called start method (isInstant=" + instantStart + ")");
        if (instantStart) {
            startGame();
            gameState = GameState.PLAYING;
            return;
        }
        gameState = GameState.STARTING;
        startTaskId = new BukkitRunnable() {
            int seconds = autoStartDelay;
            boolean fullStartDelaySet = false;

            @Override
            public void run() {
                sendDebugMsg("Running Start Timer. ID:" + startTaskId);
                if (gameState == GameState.PLAYING) {
                    cancel();
                    quickStart = -1;
                    startTaskId = -1;
                    sendDebugMsg("Cancelled start timer due to playing state");
                    return;
                }
                if (quickStart != -1 && !(seconds <= quickStart)) {
                    seconds = quickStart;
                }
                if (players.size() < minimumPlayers) {
                    cancel();
                    quickStart = -1;
                    startTaskId = -1;
                    gameState = GameState.WAITING;
                    Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                        cancelStartNotify();
                        updateWaitingSbLinesAsync(-1);
                    });
                    return;
                } else if (players.size() >= chairLocations.size() && !fullStartDelaySet) {
                    fullStartDelaySet = true;
                    seconds = fullStartDelay;
                }

                if (seconds % 10 == 0 && seconds > 9) {
                    String msg = MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.START_TIMER.MSG).replace("%seconds%", seconds + ""));
                    for (Player player : players) {
                        player.sendMessage(msg);
                        SoundUtil.playSound(player, SoundsPath.ArenaStart.TEN_MULTIPLE);
                    }
                    for (Player player : spectators) {
                        player.sendMessage(msg);
                        SoundUtil.playSound(player, SoundsPath.ArenaStart.TEN_MULTIPLE);
                    }
                    seconds--;
                } else if (seconds <= 0) {
                    cancel();
                    quickStart = -1;
                    gameState = GameState.PLAYING;
                    startTaskId = -1;
                    startGame();
                    return;
                } else if (seconds <= 5) {
                    String msg = MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.START_TIMER.MSG).replace("%seconds%", seconds + ""));
                    int fade_in = valuesConfig.getInt(ValuesPath.Game.Titles.START_TIMER + ValuesPath.Game.Titles.START_TIMER, 10);
                    int stay = valuesConfig.getInt(ValuesPath.Game.Titles.START_TIMER + ValuesPath.Game.Titles.STAY, 20);
                    int fade_out = valuesConfig.getInt(ValuesPath.Game.Titles.START_TIMER + ValuesPath.Game.Titles.FADE_OUT, 10);
                    String title = MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.START_TIMER.TITLE.replace("%seconds%", seconds + "")));
                    for (Player player : players) {
                        MsgUtil.sendMessage(player, msg);
                        SoundUtil.playSound(player, SoundsPath.ArenaStart.ONE_TO_FIVE);
                        instance.getVersionWrapper().sendTitle(player,
                                title,
                                "",
                                fade_in, stay, fade_out
                        );
                    }
                    for (Player player : spectators) {
                        MsgUtil.sendMessage(player, msg);
                        SoundUtil.playSound(player, SoundsPath.ArenaStart.ONE_TO_FIVE);
                        instance.getVersionWrapper().sendTitle(player,
                                title,
                                "",
                                fade_in, stay, fade_out
                        );
                    }
                    seconds--;
                } else {
                    seconds--;
                }
                Bukkit.getScheduler().runTaskAsynchronously(instance, () -> updateWaitingSbLinesAsync(seconds + 1));
            }
        }.runTaskTimer(instance, 0L, 20L).getTaskId();
    }

    @Override
    public void cancelStartNotify() {
        String title = MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.START_TIMER.TITLE_WAITING));
        int fade_in = valuesConfig.getInt(ValuesPath.Game.Titles.WAITING + ValuesPath.Game.Titles.FADE_IN, 10);
        int stay = valuesConfig.getInt(ValuesPath.Game.Titles.WAITING + ValuesPath.Game.Titles.STAY, 20);
        int fade_out = valuesConfig.getInt(ValuesPath.Game.Titles.WAITING + ValuesPath.Game.Titles.FADE_OUT, 10);
        for (Player player : new ArrayList<>(players)) {
            instance.getVersionWrapper().sendTitle(player, title, "", fade_in, stay, fade_out);
            SoundUtil.playSound(player, SoundsPath.ArenaStart.CANCEL_START_1);
            SoundUtil.playSound(player, SoundsPath.ArenaStart.CANCEL_START_2);
        }
        for (Player player : new ArrayList<>(spectators)) {
            instance.getVersionWrapper().sendTitle(player, title, "", fade_in, stay, fade_out);
            SoundUtil.playSound(player, SoundsPath.ArenaStart.CANCEL_START_1);
            SoundUtil.playSound(player, SoundsPath.ArenaStart.CANCEL_START_2);
        }
    }

    private void startGame() {
        sendDebugMsg("Finally startGame() is called.");
        isAnimating = false;
        tableCardType = getRandomTableCard();
        currentTurn = 0;
        List<CardType> roundDeck = getShuffledDeck();
        Bukkit.getScheduler().runTaskAsynchronously(instance, this::updateInGameSbLinesWTAsync);
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            GamePlayer gp = gamePlayers.get(p);
            IPlayerStats ps = gp.stats.clone();
            ps.addGamesPlayed();
            if (ps.getFirstPlay() == null) ps.setFirstPlay(Instant.now());
            ps.setLastPlay(Instant.now());
            playerStats.add(ps);
            instance.getVersionWrapper().sendHideNametag(p, world.getPlayers());
            ExtraUtil.spawnSeat(chairLocations.get(i), p);
            p.setGameMode(GameMode.ADVENTURE);
            p.getInventory().clear();
            p.getInventory().setItem(0, IArenaManager.getGameItem("liar_item").clone()); // give liar item
            p.getInventory().setHeldItemSlot(1);
            p.setMaxHealth(12);
            p.setHealth(12);
            giveCards(p, drawCards(roundDeck));
            String msg = MsgUtil.getConfigMessage(MsgPath.Game.Started.MSG).replace("%table_card%", MsgUtil.getConfigMessage(MsgPath.Game.Cards.CARD.replace("%card%", tableCardType.name)));
            if (!msg.isEmpty())
                MsgUtil.sendMessage(p, msg);
            instance.getVersionWrapper().sendTitle(p, MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.Started.TITLE)), MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.Started.SUBTITLE)),
                    valuesConfig.getInt(ValuesPath.Game.Titles.STARTED + ValuesPath.Game.Titles.FADE_IN, 10), valuesConfig.getInt(ValuesPath.Game.Titles.STARTED + ValuesPath.Game.Titles.STAY, 20), valuesConfig.getInt(ValuesPath.Game.Titles.STARTED + ValuesPath.Game.Titles.FADE_OUT, 10));
            int finalI = i;
            new BukkitRunnable() {
                @Override
                public void run() {
                    liarCall.setupAxe(p, Arena.this.actionItemsLocation.get(finalI));
                }
            }.runTask(instance);
        }
        isAnimating = true;
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> tableCard.run(() -> {
            isAnimating = false;
            notifyPlayersOfTurn();
        }));
        for (Player p : spectators) makeSpectator(p, true, false);
        for (Player p : world.getPlayers()) playerHolo.initHealthHolo(gamePlayers.get(p), players);
    }

    private void restartGame(boolean resetTurn) {
        sendDebugMsg("restartGame(" + resetTurn + ") is called");
        isAnimating = false;
        lastPlayed = null;
        if (!resetTurn && currentTurn >= players.size()) {
            currentTurn = 0;
        }
        if (checkAndFinish()) return;
        round++;
        updateInGameSbLinesAsync();

        tableCardType = getRandomTableCard();
        List<CardType> roundDeck = getShuffledDeck();
        String msg = MsgUtil.getConfigMessage(MsgPath.Game.TABLE_CARD_MSG).replace("%round%", String.valueOf(round)).replace("%table_card%", MsgUtil.getConfigMessage(MsgPath.Game.Cards.CARD.replace("%card%", tableCardType.name)));
        for (Player p : players) {
            giveCards(p, drawCards(roundDeck));
            MsgUtil.sendMessage(p, msg);
        }
        if (resetTurn)
            currentTurn = 0;
        isAnimating = true;
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> tableCard.run(() -> {
            isAnimating = false;
            notifyPlayersOfTurn();
        }));
    }

    @Override
    public void notifyPlayersOfTurn() {
        sendDebugMsg("Notifying players of their turn.");
        boolean allEmpty = true;
        // TODO: enable after animation is done
        for (Player p : players) {
            if (isCurrentTurn(p)) continue;
            GamePlayer gamePlayer = gamePlayers.get(p);
            if (!gamePlayer.cards.isEmpty()) {
                allEmpty = false;
                break;
            }
        }
        if (allEmpty && lastPlayed != null) {
            callLiar(gamePlayers.get(players.get(currentTurn)), gamePlayers.get(lastPlayed.player));
            return;
        }
        // TODO: enable after animation is done

        if (players.isEmpty()) return;
        String waitingMsg = MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.NotifyTurn.WAITING).replace("%player_name%", players.get(currentTurn).getName()));
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (i == currentTurn) {
                MsgUtil.sendConfigMessage(p, MsgPath.Game.NotifyTurn.YOUR_MSG);
                instance.getVersionWrapper().sendTitle(p,
                        MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.NotifyTurn.YOUR_TITLE)),
                        MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.NotifyTurn.YOUR_SUBTITLE)),
                        valuesConfig.getInt(ValuesPath.Game.Titles.YOUR_TURN + ValuesPath.Game.Titles.FADE_IN, 10), valuesConfig.getInt(ValuesPath.Game.Titles.YOUR_TURN + ValuesPath.Game.Titles.STAY, 30), valuesConfig.getInt(ValuesPath.Game.Titles.YOUR_TURN + ValuesPath.Game.Titles.FADE_OUT, 10));
                p.setExp(1.0f);
                p.setLevel(cardThrowTimeout);
                timeoutTaskId = new BukkitRunnable() {
                    final int totalTicks = cardThrowTimeout * 20;
                    int currentTotalExp = cardThrowTimeout;
                    int currentTick = totalTicks;

                    @Override
                    public void run() {
                        sendDebugMsg("Running task to set level and exp bar of player");
                        if (timeoutTaskId != getTaskId()) {
                            cancel();
                            sendDebugMsg("cancelling due to modifiction in taskid: " + timeoutTaskId);
                            p.setExp(0);
                            p.setLevel(0);
                            return;
                        }
                        GamePlayer gp = gamePlayers.get(p);
                        if (gp == null) {
                            cancel();
                            return;
                        }
                        List<Card> cards = gp.cards;
                        if (cards == null || cards.isEmpty()) cancel();
                        if (--currentTick % 20 == 0) {
                            if (--currentTotalExp <= 0) {
                                cancel();
                                sendDebugMsg("cancelling due to time over");
                                p.setExp(0);
                                p.setLevel(0);
                                List<Card> pCards = getSelectedCards(gp);
                                if (pCards.isEmpty()) {
                                    assert cards != null;
                                    Card card = cards.get(0);
                                    gp.cards.remove(0);
                                    p.getInventory().setItem(card.slot, null);
                                    String msg = MsgUtil.getConfigMessage(MsgPath.Game.CARD_DROPPED).replace("%player_name%", p.getName());
                                    throwCard(gp, Collections.singletonList(card.cardType), Arena.this::notifyPlayersOfTurn);
                                    MsgUtil.sendMessage(msg, new ArrayList<>(players), new ArrayList<>(spectators));
                                } else {
                                    List<CardType> cardTypes = new ArrayList<>();
                                    for (Card c : pCards) {
                                        cardTypes.add(c.cardType);
                                        p.getInventory().setItem(c.slot, null);
                                    }
                                    gp.cards.removeAll(pCards);
                                    String msg = MsgUtil.getConfigMessage(MsgPath.Game.CARD_DROPPED).replace("%player_name%", p.getName());
                                    throwCard(gp, cardTypes, Arena.this::notifyPlayersOfTurn);
                                    MsgUtil.sendMessage(msg, new ArrayList<>(players), new ArrayList<>(spectators));
                                }
                                return;
                            }
                            p.setLevel(currentTotalExp);
                        }
                        p.setExp((float) currentTick / totalTicks);
                    }
                }.runTaskTimerAsynchronously(instance, 1L, 1L).getTaskId();
            } else {
                p.setExp(0);
                p.setLevel(0);
                MsgUtil.sendMessage(p, waitingMsg);
            }
        }
        MsgUtil.sendMessage(waitingMsg, spectators);
    }

    private void giveCards(Player player, List<Card> deck) {
        gamePlayers.get(player).cards = deck;
        for (Card card : deck) {
            String cardTypeName = card.cardType.name;
            player.getInventory().setItem(card.slot, IArenaManager.getGameItem(cardTypeName).clone());
        }
    }

    @Override
    public LastPlayed getLastPlayed() {
        return lastPlayed;
    }

    @Override
    public boolean isCurrentTurn(GamePlayer player) {
        if (currentTurn < 0) return false;
        if (currentTurn >= players.size()) return false;
        return players.get(currentTurn).equals(player.bukkitPlayer);
    }

    @Override
    public boolean isCurrentTurn(Player player) {
        if (currentTurn < 0) return false;
        if (currentTurn >= players.size()) return false;
        return players.get(currentTurn).equals(player);
    }

    @Override
    public void selectCard(GamePlayer player, int slot, String cardConfigPath) {
        for (Card card : player.cards) {
            if (card.slot == slot) {
                card.isSelected = true;
                break;
            }
        }
        String path = MsgPath.Game.Items.CARD.replace("%card%", cardConfigPath);
        ItemStack item = player.bukkitPlayer.getInventory().getItem(slot);
        ItemBuilder.setGlow(item, true);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MsgUtil.colorize(MsgUtil.getConfigMessage(path + MsgPath.Game.Items.SELECTED_NAME)));
        meta.setLore(MsgUtil.colorize(MsgUtil.getConfigMessageList(path + MsgPath.Game.Items.SELECTED_LORE)));
        item.setItemMeta(meta);
    }

    @Override
    public boolean hasSelectedCard(GamePlayer player, int slot) {
        return player.cards.stream().anyMatch((card) -> card.slot == slot && card.isSelected);
    }

    @Override
    public List<Card> getSelectedCards(GamePlayer player) {
        return player.cards.stream().filter((card) -> card.isSelected).collect(Collectors.toList());
    }

    @Override
    public void throwCard(GamePlayer player, List<CardType> card, Runnable callback) {
        isAnimating = true;
        player.pro_tip = false;
        timeoutTaskId = -1;
        deselectAllCards(player);
        lastPlayed = new LastPlayed(player.bukkitPlayer, card);
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            cardThrow.throwCards(new ArrayList<>(players), new ArrayList<>(spectators), player.bukkitPlayer, card.size(), () -> {
                if (currentTurn < players.size() - 1) currentTurn++;
                else currentTurn = 0;
                updateInGameSbLinesAsync();
                callback.run();
                isAnimating = false;
            });
        });
    }

    @Override
    public void deselectCard(GamePlayer player, int slot, String cardConfigPath) {
        for (Card card : player.cards) {
            if (card.slot == slot) {
                card.isSelected = false;
                break;
            }
        }
        String path = MsgPath.Game.Items.CARD.replace("%card%", cardConfigPath);
        ItemStack item = player.bukkitPlayer.getInventory().getItem(slot);
        ItemBuilder.setGlow(item, false);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MsgUtil.colorize(MsgUtil.getConfigMessage(path + MsgPath.Game.Items.NOT_SELECTED_NAME)));
        meta.setLore(MsgUtil.colorize(MsgUtil.getConfigMessageList(path + MsgPath.Game.Items.NOT_SELECTED_LORE)));
        item.setItemMeta(meta);
    }

    @Override
    public void deselectAllCards(GamePlayer player) {
        List<Card> selectedCards = getSelectedCards(player);
        player.cards.removeAll(selectedCards);
    }

    private List<CardType> getShuffledDeck() {
        List<CardType> deckCopy = new ArrayList<>(DEFAULT_DECK);
        Collections.shuffle(deckCopy);
        return deckCopy;
    }

    private List<Card> drawCards(List<CardType> deck) {
        List<Card> hand = new ArrayList<>();

        int cardsToDraw = Math.min(5, deck.size());
        for (int i = 0; i < cardsToDraw; i++) {
            CardType type = deck.remove(0);
            hand.add(new Card(type, i + 2, false));
        }

        return hand;
    }

    private CardType getRandomTableCard() {
        CardType[] tableCards = {CardType.KING, CardType.QUEEN, CardType.ACE};
        int randomIndex = new Random().nextInt(tableCards.length);
        return tableCards[randomIndex];
    }

    public boolean isEnding() {
        return isEnding;
    }

    public void setQuickStart(int seconds) {
        if (quickStart != -1) return;
        quickStart = seconds;
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    @Override
    public boolean checkAndFinish() {
        sendDebugMsg("checkAndFinish() Called");
        if (isEnding || gameState != GameState.PLAYING) return true;
        if (players.size() == 1) {
            sendDebugMsg("playingPlayers size == 1, ending...");
            isEnding = true;
            currentTurn = 0;
            Player winner = players.get(0);
            playerStats.forEach(f -> {
                if (f.getUuid() == winner.getUniqueId())
                    f.addWin();
            });
            liarCall.destroyAxe(winner, -1);
            playerHolo.removeAllHolo(world);
            SoundUtil.playSound(winner, SoundsPath.GameEnd.WINNER);
            timeoutTaskId = -1;
            String msg = MsgUtil.getConfigMessage(MsgPath.Game.GAME_OVER.MSG).replace("%player_name%", winner.getName());
            MsgUtil.sendMessage(msg, players, spectators);
            String title = MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.GAME_OVER.TITLE).replace("%player_name%", winner.getName()));
            String subtitle = MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.GAME_OVER.SUBTITLE));
            int fadeIn = valuesConfig.getInt(ValuesPath.Game.Titles.GAMEOVER + ValuesPath.Game.Titles.FADE_IN, 10);
            int stay = valuesConfig.getInt(ValuesPath.Game.Titles.GAMEOVER + ValuesPath.Game.Titles.STAY, 30);
            int fadeOut = valuesConfig.getInt(ValuesPath.Game.Titles.GAMEOVER + ValuesPath.Game.Titles.FADE_OUT, 10);
            Location spectatingLocation = winner.getLocation().add(0, 1, 0);
            winner.leaveVehicle();
            ExtraUtil.destroySeat(winner);
            winner.setAllowFlight(true);
            winner.setFlying(true);
            winner.setAllowFlight(true);
            winner.teleport(spectatingLocation);
            winner.setMaxHealth(20);
            winner.setHealth(20);
            instance.getVersionWrapper().sendTitle(winner, title, subtitle,
                    fadeIn, stay, fadeOut);
            for (Player player : spectators) {
                instance.getVersionWrapper().sendTitle(player, title, subtitle,
                        fadeIn, stay, fadeOut);
                SoundUtil.playSound(player, SoundsPath.GameEnd.OTHERS);
            }
            Bukkit.getScheduler().runTaskLater(instance, this::end, 5 * 20);
            return true;
        } else if (players.isEmpty()) {
            sendDebugMsg("playingPlayers isEmpty, ending...");
            isEnding = true;
            playerHolo.removeAllHolo(world);
            MsgUtil.sendConfigMessage(MsgPath.Game.GAME_OVER.NO_PLAYERS_LEFT, spectators);
            end();
            return true;
        } else return false;
    }


    @Override
    public void end() {
        sendDebugMsg("ending arena...");
        tableCardType = null;
        lastPlayed = null;
        liarCall.clearOldData();
        cardThrow.clearOldData();

        playerStats.forEach(f -> {
            GamePlayer gp = gamePlayers.get(Bukkit.getPlayer(f.getUuid()));
            if (gp != null) gp.stats = f;
            Bukkit.getScheduler().runTaskAsynchronously(instance, () ->
                    instance.getDb().saveStats(f));
        });

        for (Player player : world.getPlayers()) // TODO reduce
            instance.getVersionWrapper().sendShowNametag(player, world.getPlayers());
        for (Player player : players) {
            for (Player spec : spectators) {
                player.showPlayer(spec);
            }
            ExtraUtil.destroySeat(player);
            GamePlayer.resetPlayer(player);
            Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                ScoreboardManager.removeBoard(player);
            });
            player.setAllowFlight(false);
            player.setFlying(false);
            player.teleport(ExtraUtil.getLobbyLocation());
            player.getInventory().clear();
        }
        for (Player player : spectators) {
            player.setAllowFlight(false);
            player.setFlying(false);
            GamePlayer.resetPlayer(player);
            Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
                ScoreboardManager.removeBoard(player);
            });
            player.teleport(ExtraUtil.getLobbyLocation());
            player.getInventory().clear();
        }
        List<ArmorStand> toRemove = world
                .getEntitiesByClass(ArmorStand.class).stream()
                .filter(armorStand -> IArena.SEAT_CUSTOM_NAME.equals(armorStand.getCustomName()))
                .collect(Collectors.toList());
        toRemove.forEach(Entity::remove);
        players.clear();
        playerStats.clear();
        spectators.clear();
        isAnimating = false;
        gameState = GameState.WAITING;
        isEnding = false;
    }

    @Override
    public void sendDebugMsg(String msg) {
        if (debug) MsgUtil.sendConsoleMessage("&cDEBUG&r | " + msg);
    }
}
