package com.kooy29.liarslounge.storage.yaml;

public class MsgPath {

    public static class HelpCommands {
        public static final String BASE = "help-commands";

        public static class Ll {
            public static final String DEFAULT = BASE + ".ll.default";
            public static final String ADMIN = BASE + ".ll.admin";
        }

        public static class Arena {
            public static final String DEFAULT = BASE + ".arena.default";
            public static final String ADMIN = BASE + ".arena.admin";
        }

        public static class Setup {
            public static final String NO_SESSION = BASE + ".setup.no-session";
            public static final String IN_SESSION = BASE + ".setup.in-session";
        }
    }

    public static class Usage {
        public static final String JOIN = "usage.join";
        public static final String SPECTATE = "usage.spectate";
        public static final String DISABLE_ARENA = "usage.disable-arena";
        public static final String ENABLE_ARENA = "usage.enable-arena";
        public static final String SETUP = "usage.setup";
        public static final String CHAIR = "usage.chair";
        public static final String CHAIR_REMOVE = "usage.chair-remove";
        public static final String ACTION_ITEM = "usage.action-item";
        public static final String ACTION_ITEM_REMOVE = "usage.action-item-remove";
    }

    public static class Success {

        public static class Arena {
            public static final String JOIN = "success.arena.join";
            public static final String LEAVE = "success.arena.leave";
            public static final String ENABLE = "success.arena.enable";
            public static final String DISABLE = "success.arena.disable";
            public static final String LIST_HEADER = "success.arena.list-header";
            public static final String LIST_ROW = "success.arena.list-row";
            public static final String LIST_JOIN = "success.arena.list-join";
            public static final String LIST_SPECTATE = "success.arena.list-spectate";
        }

        public static class Setup {
            public static final String STARTED = "success.setup.started";
            public static final String WAITING = "success.setup.waiting";
            public static final String TABLE = "success.setup.table";
            public static final String CHAIRS = "success.setup.chairs";
            public static final String CHAIR_ADD = "success.setup.chair-add";
            public static final String CHAIR_REMOVE = "success.setup.chair-remove";
            public static final String ACTION_ITEMS = "success.setup.action-items";
            public static final String ACTION_ITEM_ADD = "success.setup.action-item-add";
            public static final String ACTION_ITEM_REMOVE = "success.setup.action-item-remove";
            public static final String SAVE = "success.setup.save";
            public static final String CLICK_TO_ENABLE = "success.setup.click-to-enable";
            public static final String END = "success.setup.end";
            public static final String LOBBY = "success.setup.lobby";
        }
    }

    public static class Error {
        public static final String INVALID_NUM = "error.invalid-num";
        public static final String NOT_IN_ARENA = "error.not-in-arena";
        public static final String IN_AN_ARENA = "error.in-an-arena";
        public static final String ARENA_NOT_FOUND = "error.arena-not-found";
        public static final String ARENA_IS_FULL = "error.arena-is-full";
        public static final String ARENA_ALREADY_ENABLED = "error.arena-already-enabled";
        public static final String ARENA_NAME_EMPTY = "error.arena-name-empty";
        public static final String ARENA_IN_GAME = "error.arena-in-game";
        public static final String ARENA_NOT_IN_GAME = "error.arena-not-in-game";
        public static final String PLAYER_LEFT_RESTART = "error.player-left-restart";
        public static final String ARENA_NOT_IN_WAITING = "error.arena-not-in-waiting";
        public static final String LEAST_PLAYERS = "error.least-players";
        public static final String LOC_NOT_SET = "error.location-not-set";
        public static final String CHAIR_ALREADY_SET = "error.chair-already-set";
        public static final String ADMIN_DISABLED_ARENA = "error.admin-disabled-arena";
        public static final String NO_PERMISSION = "error.no-permission";
        public static final String NO_SESSION = "error.no-session";
        public static final String SETUP_ERROR = "error.setup-error";
        public static final String ADD_EXCEED = "error.add-exceed";
        public static final String WORLD_NOT_FOUND = "error.world-not-found";
        public static final String CMD_NOT_ALLOWED = "error.cmd-not-allowed";
        public static final String IN_SETUP_SESSION = "error.in-setup-session";
        public static final String REQUIRES_ARENA_DISABLED = "error.requires-arena-disabled";
        public static final String FOUND_SAVED_ARENA = "error.found-saved-arena";
        public static final String SETUP_JOIN = "error.setup-session-join";
        public static final String UNKNOWN_SUBCOMMAND = "error.unknown-subcommand";
    }

    public static class Game {
        public static final String JOIN = "game.join";
        public static final String LEFT = "game.left";
        public static final String SPECTATING = "game.spectating";
        public static final String TABLE_CARD_MSG = "game.table-card-msg";
        public static final String TABLE_CARD_ANIMATION = "game.table-card-animation";
        public static final String NOT_YOUR_TURN = "game.not-your-turn";
        public static final String CARD_NOT_SELECTED = "game.card-not-selected";
        public static final String PROTIP = "game.protip";
        public static final String CARD_DROPPED = "game.card-dropped";
        public static final String CARDS_DROPPED = "game.cards-dropped";
        public static final String CANNOT_CALL_LIAR = "game.cannot-call-liar";
        public static final String CARD_SELECT = "game.card-select";
        public static final String CARD_SELECT_LIMIT = "game.card-select-limit";
        public static final String CARD_DESELECT = "game.card-deselect";
        public static final String SHOT_PASS = "game.shot-pass";

        public static class START_TIMER {
            public static final String MSG = "game.start-timer.msg";
            public static final String TITLE_WAITING = "game.start-timer.title-waiting";
            public static final String TITLE = "game.start-timer.title-%seconds%";
        }

        public static class Scoreboard {
            public static final String SERVER_IP = "game.scoreboard.server-ip";
            public static final String WAITING_TITLE = "game.scoreboard.waiting.title";
            public static final String WAITING_LINES = "game.scoreboard.waiting.lines";
            public static final String WAITING_MSG_WAITING = "game.scoreboard.waiting.waiting";
            public static final String WAITING_MSG_STARTING = "game.scoreboard.waiting.starting-in";
            public static final String INGAME_TITLE = "game.scoreboard.in-game.title";
            public static final String INGAME_LINES = "game.scoreboard.in-game.lines";
            public static final String INGAME_PLAYER_CARD_TURN = "game.scoreboard.in-game.player-card.turn";
            public static final String INGAME_PLAYER_CARD_NOTTURN = "game.scoreboard.in-game.player-card.not-turn";
        }

        public static class Items {
            public static final String NAME = ".name";
            public static final String LORE = ".lore";
            public static final String NOT_SELECTED_NAME = ".not-selected" + NAME;
            public static final String NOT_SELECTED_LORE = ".not-selected" + LORE;
            public static final String SELECTED_NAME = ".selected" + NAME;
            public static final String SELECTED_LORE = ".selected" + LORE;
            private static final String GAME_ITEMS = "game.items";
            public static final String LEAVE_ITEM = GAME_ITEMS + ".leave-arena";
            public static final String GUIDE_ITEM = GAME_ITEMS + ".guide";
            public static final String CALL_LIAR = GAME_ITEMS + ".call-liar";
            public static final String CARD = GAME_ITEMS + ".cards.%card%";

        }

        public static class Started {
            public static final String MSG = "game.game-started.msg";
            public static final String TITLE = "game.game-started.title";
            public static final String SUBTITLE = "game.game-started.subtitle";
        }

        public static class NotifyTurn {
            public static final String WAITING = "game.notify-turn.waiting";
            private static final String YOUR = "game.notify-turn.your";
            public static final String YOUR_MSG = YOUR + ".msg";
            public static final String YOUR_TITLE = YOUR + ".title";
            public static final String YOUR_SUBTITLE = YOUR + ".subtitle";
        }

        public static class CallLiar {
            public static final String INITIAL = "game.call-liar.initial";
            public static final String NOT_LIAR = "game.call-liar.not-liar";
            public static final String IS_LIAR = "game.call-liar.is-liar";
        }

        public static class ShotHit {
            public static final String MSG = "game.shot-hit.msg";
            public static final String TITLE = "game.shot-hit.title";
            public static final String SUBTITLE = "game.shot-hit.subtitle";
        }

        public static class Cards {
            public static final String CARD = "game.cards.%card%";
        }

        public static class GAME_OVER {
            public static final String MSG = "game.game-over.msg";
            public static final String NO_PLAYERS_LEFT = "game.game-over.no-players-left";
            public static final String TITLE = "game.game-over.title";
            public static final String SUBTITLE = "game.game-over.subtitle";
        }
    }

    public static final class Gui {
        private static final String BASE = "gui";
        public static String BOOK_GUIDE = Gui.BASE + ".book_guide";

        public static final class ArenaSelector {
            public static final String BASE = Gui.BASE + ".arena-selector";

            public static final String TITLE = BASE + ".title";

            public static final String JoinItem = ArenaSelector.BASE + ".join-item.%status%";
            public static final String JoinRandom = ArenaSelector.BASE + ".join-random";
            public static final String NextPage = ArenaSelector.BASE + ".next-page";
            public static final String PreviousPage = ArenaSelector.BASE + ".previous-page";
        }
    }
}
