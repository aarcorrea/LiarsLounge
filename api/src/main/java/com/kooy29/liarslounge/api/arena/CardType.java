package com.kooy29.liarslounge.api.arena;

public enum CardType {
    KING("king"),
    QUEEN("queen"),
    ACE("ace"),
    JOKER("joker");

    public final String name;

    CardType(String name) {
        this.name = name;
    }

    public static CardType fromValue(int value) {
        for (CardType type : CardType.values()) {
            if (type.ordinal() == value) {
                return type;
            }
        }
        return null;
    }

    public static CardType fromName(String name) {
        for (CardType type : CardType.values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}
