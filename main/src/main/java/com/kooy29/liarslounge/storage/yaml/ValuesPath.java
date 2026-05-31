package com.kooy29.liarslounge.storage.yaml;

public class ValuesPath {
    public static class Lobby {
        private static final String LOBBY = "lobby";
        public static final String LOCATION = LOBBY + ".location";
    }

    public static class Game {

        public static class Items {
            public static final String MATERIAL = ".material";
            public static final String TEXTURE = ".texture";
            public static final String DATA = ".data";
            private static final String BASE = "game.items";
            public static final String LEAVE_ITEM = BASE + ".leave-arena";
            public static final String GUIDE_ITEM = BASE + ".guide";
            public static final String CALL_LIAR = BASE + ".call-liar";
            public static final String CARD = BASE + ".cards.%card%";
            public static final String VERDICT_CARDS_RIGHT = BASE + ".verdict-cards.right.%card%";
            public static final String VERDICT_CARDS_WRONG = BASE + ".verdict-cards.wrong.%card%";
        }

        public static class Titles {
            public static final String FADE_IN = ".fade-in";
            public static final String STAY = ".stay";
            public static final String FADE_OUT = ".fade-out";
            private static final String BASE = "game.titles";
            public static final String WAITING = BASE + ".waiting";
            public static final String START_TIMER = BASE + ".start-timer";
            public static final String STARTED = BASE + ".started";
            public static final String YOUR_TURN = BASE + ".your-turn";
            public static final String SHOT = BASE + ".shot";
            public static final String GAMEOVER = BASE + ".game-over";
        }
    }

    public static class Gui {
        public static class ArenaSelector {
            private static final String BASE = "gui.arena-selector";

            public static final String SIZE = BASE + ".size";
            public static final String JOIN_RANDOM = BASE + ".join-random";
            public static final String NEXT_PAGE = BASE + ".next-page";
            public static final String PREVIOUS_PAGE = BASE + ".previous-page";

            public static class JoinItem {
                public static final String BASE = ArenaSelector.BASE + ".join-item.%status%";
                public static final String SLOTS = ArenaSelector.BASE + ".join-item.slots";
            }
        }
    }
}
