package com.kooy29.liarslounge.storage.yaml;

public final class SoundsPath {

    public static final class ArenaStart {
        private static final String BASE = "arena-start";

        public static final String TEN_MULTIPLE = BASE + ".10-multiple";
        public static final String ONE_TO_FIVE = BASE + ".1-to-5";

        public static final String CANCEL_START_1 = BASE + ".cancel-start-1";
        public static final String CANCEL_START_2 = BASE + ".cancel-start-2";
    }

    public static final class Card {
        private static final String BASE = "card";

        public static final String SELECT = BASE + ".select";
        public static final String DESELECT = BASE + ".deselect";
        public static final String THROW = BASE + ".throw";
    }

    public static final class CardReveal {
        private static final String BASE = "card-reveal";

        public static final String RISE = BASE + ".rise";
        public static final String ACCUSER_RIGHT = BASE + ".accuser-right";
        public static final String ACCUSER_WRONG = BASE + ".accuser-wrong";
    }

    public static final class TableCard {
        private static final String BASE = "table-card";

        public static final String RISE = BASE + ".rise";
    }

    public static final class CallLiar {
        private static final String BASE = "call-liar";

        public static final String CALL = BASE + ".call";

        public static final String TRUTH = BASE + ".truth";
        public static final String LIE = BASE + ".lie";

        public static final String AXE_RELOAD = BASE + ".axe-reload";
        public static final String AXE_HALF_SWING = BASE + ".axe-halfswing";
        public static final String AXE_FULL_SWING = BASE + ".axe-fullswing";
    }

    public static final class GameEnd {
        private static final String BASE = "game-end";

        public static final String WINNER = BASE + ".winner";
        public static final String OTHERS = BASE + ".others";
    }
}