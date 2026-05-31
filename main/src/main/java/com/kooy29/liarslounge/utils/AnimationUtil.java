package com.kooy29.liarslounge.utils;

import java.lang.reflect.Constructor;

public class AnimationUtil {
    private static Constructor<?> cardReveal;
    private static Constructor<?> cardThrow;
    private static Constructor<?> liarCall;
    private static Constructor<?> tableCard;

    public static void setAnimationClass(Constructor<?> cardReveal, Constructor<?> cardThrow, Constructor<?> liarCall, Constructor<?> tableCard) {
        AnimationUtil.cardReveal = cardReveal;
        AnimationUtil.cardThrow = cardThrow;
        AnimationUtil.liarCall = liarCall;
        AnimationUtil.tableCard = tableCard;
    }

    public static Constructor<?> getCardReveal() {
        return cardReveal;
    }

    public static Constructor<?> getCardThrow() {
        return cardThrow;
    }

    public static Constructor<?> getLiarCall() {
        return liarCall;
    }

    public static Constructor<?> getTableCard() {
        return tableCard;
    }
}
