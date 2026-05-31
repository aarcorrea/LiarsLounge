package com.kooy29.liarslounge;


import com.kooy29.liarslounge.api.API;

public final class APIProvider {
    public static boolean isHigherVersion = false;
    private static API instance;

    public static API get() {
        if (instance == null) {
            throw new IllegalStateException("API is not initialized.");
        }
        return instance;
    }

    public static void setInstance(API api) {
        instance = api;
    }
}
