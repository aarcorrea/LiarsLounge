package com.kooy29.liarslounge.scoreboard;

import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.ChatColor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Scoreboard {
    protected static String SERVER_IP;
    // Waiting scoreboard
    protected static String waitingScoreboardTitle;
    protected static List<String> waitingScoreboardLines;
    // Waiting scoreboard vars
    protected static String WAITING_MSG;
    protected static String STARTING_IN_MSG;
    protected static int waitingPlayersLine;
    protected static int waitingStatusLine;
    // In-game scoreboard
    protected static String inGameScoreboardTitle;
    protected static List<String> inGameScoreboardLines;
    // In-game scoreboard vars
    protected static int gameRoundLine;
    protected static int gameTableCardLine;
    protected static int gamePlayersLine;
    protected static int gameCallsLine;
    protected static int gameEliminationsLine;
    protected static String PLAYER_CARD_TURN;
    protected static String PLAYER_CARD_NOT_TURN;
    public Waiting waiting;
    public InGame inGame;
    private IArena arena;

    public Scoreboard() {
    }

    public Scoreboard(IArena arena) {
        this.waiting = new Waiting();
        this.inGame = new InGame();
        this.arena = arena;
    }

    public class Waiting {
        private List<String> staticReplacedVars;

        public String getTitle() {
            return waitingScoreboardTitle;
        }

        public void setSRLines() {
            staticReplacedVars = new ArrayList<>(waitingScoreboardLines);
            for (int i = 0; i < staticReplacedVars.size(); i++) {
                String line = staticReplacedVars.get(i);
                if (line.contains("%date%"))
                    line = line.replace("%date%", new SimpleDateFormat("dd/MM/yy").format(new Date(System.currentTimeMillis())));
                if (line.contains("%arena_name%")) line = line.replace("%arena_name%", arena.getName());
                if (line.contains("%arena_group%")) line = line.replace("%arena_group%", arena.getGroup());
                if (line.contains("%server_ip%")) line = line.replace("%server_ip%", SERVER_IP);
                if (line.contains("%max_players%"))
                    line = line.replace("%max_players%", arena.getChairLocations().size() + "");
                staticReplacedVars.set(i, line);
            }
        }

        public List<String> getNSRLines(int seconds) {
            List<String> replacedLines = new ArrayList<>(staticReplacedVars);
            String line1 = replacedLines.get(waitingPlayersLine);
            replacedLines.set(waitingPlayersLine, line1.replace("%players%", arena.getPlayers().size() + ""));
            String line2 = replacedLines.get(waitingStatusLine);
            replacedLines.set(waitingStatusLine, line2.replace("%waiting_status%", (seconds > 0 ? STARTING_IN_MSG.replace("%seconds%", seconds + "") : WAITING_MSG)));
            return replacedLines;
        }
    }

    public class InGame {
        private List<String> staticReplacedVars;

        public String getTitle() {
            return inGameScoreboardTitle;
        }

        public void setSRLines() {
            staticReplacedVars = new ArrayList<>(inGameScoreboardLines);
            for (int i = 0; i < staticReplacedVars.size(); i++) {
                String line = staticReplacedVars.get(i);
                if (line.contains("%date%"))
                    line = line.replace("%date%", new SimpleDateFormat("dd/MM/yy").format(new Date(System.currentTimeMillis())));
                if (line.contains("%server_ip%")) line = line.replace("%server_ip%", SERVER_IP);
                staticReplacedVars.set(i, line);
            }
        }

        public List<String> getNSRLines(IArena.GamePlayer gamePlayer) {
            String calls = "0";
            String eliminations = "0";
            if (gamePlayer != null) {
                calls = gamePlayer.calls + "";
                eliminations = gamePlayer.eliminations + "";
            }

            List<String> replacedLines = new ArrayList<>(staticReplacedVars);

            String line1 = replacedLines.get(gameRoundLine);
            replacedLines.set(gameRoundLine, line1.replace("%round%", arena.getCurrentRound() + ""));

            String line2 = replacedLines.get(gameTableCardLine);
            replacedLines.set(gameTableCardLine, line2.replace("%table_card%", ChatColor.stripColor(MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.Cards.CARD.replace("%card%", (arena.getTableCardType() == null ? "" : arena.getTableCardType().name)))))));

            String line4 = replacedLines.get(gameCallsLine);
            replacedLines.set(gameCallsLine, line4.replace("%calls%", calls));

            String line5 = replacedLines.get(gameEliminationsLine);
            replacedLines.set(gameEliminationsLine, line5.replace("%eliminations%", eliminations));

            if (gamePlayersLine != replacedLines.size() - 1) {
                List<String> relocateLines = new ArrayList<>(replacedLines.subList(gamePlayersLine + 1, replacedLines.size()));
                replacedLines.subList(gamePlayersLine, replacedLines.size()).clear();

                for (int i = 0; i < arena.getPlayers().size(); i++) {
                    replacedLines.add(getReplacedVars(i));
                }

                replacedLines.addAll(relocateLines);
            } else {
                for (int i = 0; i < arena.getPlayers().size(); i++) {
                    replacedLines.add(getReplacedVars(i));
                }
            }

            return replacedLines;
        }

        private String getReplacedVars(int index) {
            IArena.GamePlayer gamePlayer = IArena.gamePlayers.get(arena.getPlayers().get(index));
            if (arena.isCurrentTurn(gamePlayer)) {
                return PLAYER_CARD_TURN.replace("%player_name%", gamePlayer.bukkitPlayer.getName()).replace("%cards%", (gamePlayer.cards == null ? "0" : gamePlayer.cards.size()) + "");
            } else {
                return PLAYER_CARD_NOT_TURN.replace("%player_name%", gamePlayer.bukkitPlayer.getName()).replace("%cards%", (gamePlayer.cards == null ? "0" : gamePlayer.cards.size()) + "");
            }
        }
    }
}
