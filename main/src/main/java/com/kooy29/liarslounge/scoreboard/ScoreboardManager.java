package com.kooy29.liarslounge.scoreboard;

import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.utils.MsgUtil;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class ScoreboardManager extends Scoreboard {
    private static HashMap<Player, FastBoard> boards = new HashMap<>();

    public static void init() {
        SERVER_IP = MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.Scoreboard.SERVER_IP));
        WAITING_MSG = MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.Scoreboard.WAITING_MSG_WAITING));
        STARTING_IN_MSG = MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.Scoreboard.WAITING_MSG_STARTING));
        PLAYER_CARD_TURN = MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.Scoreboard.INGAME_PLAYER_CARD_TURN));
        PLAYER_CARD_NOT_TURN = MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.Scoreboard.INGAME_PLAYER_CARD_NOTTURN));
        waitingScoreboardTitle = MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.Scoreboard.WAITING_TITLE));
        List<String> waitingLines = MsgUtil.getConfigMessageList(MsgPath.Game.Scoreboard.WAITING_LINES);
        for (int i = 0; i < waitingLines.size(); i++) {
            String line = waitingLines.get(i);
            if (line.contains("%players%")) waitingPlayersLine = i;
            if (line.contains("%waiting_status%")) waitingStatusLine = i;
            waitingLines.set(i, MsgUtil.colorize(line));
        }
        waitingScoreboardLines = waitingLines;

        inGameScoreboardTitle = MsgUtil.colorize(MsgUtil.getConfigMessage(MsgPath.Game.Scoreboard.INGAME_TITLE));
        List<String> inGameLines = MsgUtil.getConfigMessageList(MsgPath.Game.Scoreboard.INGAME_LINES);
        for (int i = 0; i < inGameLines.size(); i++) {
            String line = inGameLines.get(i);
            if (line.contains("%round%")) gameRoundLine = i;
            if (line.contains("%table_card%")) gameTableCardLine = i;
            if (line.contains("%table_card%")) gameTableCardLine = i;
            if (line.contains("%players%")) gamePlayersLine = i;
            if (line.contains("%calls%")) gameCallsLine = i;
            if (line.contains("%eliminations%")) gameEliminationsLine = i;
            inGameLines.set(i, MsgUtil.colorize(line));
        }
        inGameScoreboardLines = inGameLines;
    }

    public static FastBoard createIfNotExist(Player player) {
        FastBoard sb = boards.get(player);
        if (sb == null) {
            sb = new FastBoard(player);
            boards.put(player, sb);
        }
        return sb;
    }

    public static FastBoard getBoard(Player player) {
        return boards.get(player);
    }

    public static FastBoard createBoard(Player player) {
        FastBoard sb = new FastBoard(player);
        boards.put(player, sb);
        return sb;
    }

    public static void removeBoard(Player player) {
        FastBoard sb = boards.get(player);
        sb.delete();
        boards.remove(player);
    }
}
