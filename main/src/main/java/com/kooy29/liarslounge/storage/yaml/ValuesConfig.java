package com.kooy29.liarslounge.storage.yaml;

import com.kooy29.liarslounge.APIProvider;
import com.kooy29.liarslounge.api.storage.IConfiguration;
import org.bukkit.configuration.file.FileConfiguration;

public class ValuesConfig {
    public static IConfiguration setupValuesConfig(IConfiguration iConfig) {
        FileConfiguration config = iConfig.getConfig();

        config.addDefault(ValuesPath.Lobby.LOCATION, "");

        setItem(config, ValuesPath.Game.Items.LEAVE_ITEM,
                mat("RED_BED", "BED"), "", 0, 8);

        setItem(config, ValuesPath.Game.Items.GUIDE_ITEM,
                mat("WRITTEN_BOOK", "WRITTEN_BOOK"), "", 0, 0);

        setItem(config, ValuesPath.Game.Items.CALL_LIAR,
                mat("BLAZE_ROD", "BLAZE_ROD"), "", 0, -1);

        createCard(config, "king", "ewogICJ0aW1lc3RhbXAiIDogMTc1MDkzODg5ODg5NCwKICAicHJvZmlsZUlkIiA6ICI0NmNhODkyZTY4ODA0YThmYjFkYzkwYjg0ZTY5ZjVmZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJPbG8xNjA2IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg1YzBjNWYyZGMwYjA2NDc5YWEzNThiMGZhMjkzNGNhMWM4MTNiNmMxMjEyYzk2MTQ4YmQzNWE4ZjBiNGUxZmEiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==");
        createCard(config, "queen", "ewogICJ0aW1lc3RhbXAiIDogMTc1MDkzODkwMDQwOCwKICAicHJvZmlsZUlkIiA6ICJhNzdkNmQ2YmFjOWE0NzY3YTFhNzU1NjYxOTllYmY5MiIsCiAgInByb2ZpbGVOYW1lIiA6ICIwOEJFRDUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZThmNWY2Y2UzY2I2YmRjYzdhM2U0NTgyNWE1YWRmNDBlM2YxYTNiZjUwZDAxZWVmNzMxMTAxMTFmY2IxYzZlNiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9");
        createCard(config, "ace", "ewogICJ0aW1lc3RhbXAiIDogMTc1MDkzODkwMTg5NywKICAicHJvZmlsZUlkIiA6ICJmMzNlZGMyNTRmNDk0NWY2YTg5ZjFjM2JhZmNkZjIwNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJGVV9CYWJ5IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzYyNjk5NjhhMWVkMzdhNzFmMDJkYjIyMzU0ODI4NTVlNjBlNTg1OWFlYTFjNzczZDZmMWQyZmJkZmNlZTI4YzEiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==");
        createCard(config, "joker", "ewogICJ0aW1lc3RhbXAiIDogMTc1MDkzODg5NzI1NiwKICAicHJvZmlsZUlkIiA6ICJkNGUwNTFjZGRhNDU0NTUxYjk1ZWRiZjdkNTM1ZTA3ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJFbGVtZW50YWxfTWF4IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzI2ZjU2ODA2OTk2YWQ2YWQ1MjA2ZWVjNzRjYWJiNzczMGFmNTcwMzQ2ODVjNTAwYzJkMGU4NjczYzJhNjM5NzAiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==");
        createCard(config, "hidden", "ewogICJ0aW1lc3RhbXAiIDogMTc1MjQxNTYyMTQ3MiwKICAicHJvZmlsZUlkIiA6ICIzMzU3MWJiY2UyMDE0MTRiYmNkMDYyMjEyZTI4MjBlMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGFkb21JbmF0b3I0NzgiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTY4NGU1YTQ5MTgyYmM5ODBhZGJjNWYwZjZkMzE3NDg0NDM5YWY0YjU3ZDJhN2U4N2E3MjMzYTMyZDdlYmZlZiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9");

        createVerdictCard(config, true, "king", "ewogICJ0aW1lc3RhbXAiIDogMTc1NTgwODU2Mjc0NSwKICAicHJvZmlsZUlkIiA6ICJhYjNkNTgwMjVkOWM0NTcyODNkNTFlYTcwYTY4N2U1NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJsdWN5X2ludGhlc2t5XyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9mYTQ4ZGIxMDdlODBkMWVjNmYxNjdmNWVlMDhkMGEwMmIwM2ZiMmNlOGQ4NTc1NGYzNGFhNjA2MTA3Yzk1ZTAyIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=");
        createVerdictCard(config, true, "queen", "ewogICJ0aW1lc3RhbXAiIDogMTc1NTgwODU2Njc3NywKICAicHJvZmlsZUlkIiA6ICJhZWNkODIxZTQyYzE0ZDJlOThmNTA1OTg1MWI5OWMzNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJSb2RyaVgyMDc1IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2EyZjY5MmRiYjg3YWJjNzdmYjVlYzU0MWVkNTNhMGZhYTZlMmJjYmE2Mzk1MTE0ZWFhZjY1ZDEwOGUyZDM3NmYiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==");
        createVerdictCard(config, true, "ace", "ewogICJ0aW1lc3RhbXAiIDogMTc1NTgwODU1ODIxNSwKICAicHJvZmlsZUlkIiA6ICJmNThkZWJkNTlmNTA0MjIyOGY2MDIyMjExZDRjMTQwYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ1bnZlbnRpdmV0YWxlbnQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWEyZDFhZWM2NzBhMjE4OGE0YjhmNTJjZDkxMDMzMjA3ZGU5YmU1ZmEyMzRiNWQxZjU5YmIzMDc4MjEyMGFhMSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9");
        createVerdictCard(config, true, "joker", "ewogICJ0aW1lc3RhbXAiIDogMTc1NTgwODU2MTQ2MiwKICAicHJvZmlsZUlkIiA6ICJhZDEwM2FhODMzNWM0ZDA4YjNlZWI2ZDI2NjNkMzA0MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJJY2hpaW5uIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Y4NDEzZDAzMzUxOTRmOWM0ZmRkNWM5MTAwYjNmYjQzZTY3Y2JjMDkyN2UzZDBmYmQwOTU2ZTIzY2E0Mjk2NmYiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==");

        createVerdictCard(config, false, "king", "ewogICJ0aW1lc3RhbXAiIDogMTc1NTgwODU2NDg2MiwKICAicHJvZmlsZUlkIiA6ICIwNTljODIxYzhhODU0NGJiOWJiODVhOGMxNjVhYTc5YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJoZWxsc3RydWNrZWR6IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzkyZmRjNmI3NjllNGZiNDYxMThiZTI1ZjVhNGQ2ZGU4ZjM1NjdlOTQ3YWM2ZmZkMGE5ZjhhZmMwMzI5NGMyMTciLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==");
        createVerdictCard(config, false, "queen", "ewogICJ0aW1lc3RhbXAiIDogMTc1NTgwODU2OTA2MywKICAicHJvZmlsZUlkIiA6ICJlYTdiMmRmZDZjNGU0NDU0YWQ2YWMwZDFjZWNjYjNhMSIsCiAgInByb2ZpbGVOYW1lIiA6ICJFbGV2ZW5DaGFycyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iNDk0OWRjY2VjMWI5MjdlMjFmMjU4ZTY2N2Q3ZWJlMGNiN2M5OThlOThlNmY5MjgzYzU1MDIyYzgwNGJlM2M2IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=");
        createVerdictCard(config, false, "ace", "ewogICJ0aW1lc3RhbXAiIDogMTc1NTgwODU1OTcyNiwKICAicHJvZmlsZUlkIiA6ICIxN2I5ZDBmOWYxYzE0OTE5ODRkY2Y5ZGM2YzczZDYzYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJuYWJlNDk3NSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xZjBkMGRjMTc3MGI4OGEwY2RkYWIyOTJmMGU3MmQ2MjAzNzU2N2NlMDhjODk3OTg4NDAyZjI1NWFjODQwMjUxIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=");

        title(config, ValuesPath.Game.Titles.WAITING, 10, 20, 10);
        title(config, ValuesPath.Game.Titles.START_TIMER, 10, 20, 10);
        title(config, ValuesPath.Game.Titles.STARTED, 5, 5, 5);
        title(config, ValuesPath.Game.Titles.YOUR_TURN, 10, 30, 10);
        title(config, ValuesPath.Game.Titles.SHOT, 10, 30, 10);
        title(config, ValuesPath.Game.Titles.GAMEOVER, 10, 30, 10);

        config.addDefault(ValuesPath.Gui.ArenaSelector.SIZE, 45);

        setItem(config, ValuesPath.Gui.ArenaSelector.JoinItem.BASE.replace("%status%", "waiting"), "PAPER", "", 0, -1);
        setItem(config, ValuesPath.Gui.ArenaSelector.JoinItem.BASE.replace("%status%", "starting"), "MAP", "", 0, -1);
        setItem(config, ValuesPath.Gui.ArenaSelector.JoinItem.BASE.replace("%status%", "playing"), "FILLED_MAP", "", 0, -1);
        config.addDefault(
                ValuesPath.Gui.ArenaSelector.JoinItem.SLOTS,
                "10,11,12,13,14,15,16,19,20,21,22,23,24,25"
        );

        setItem(config, ValuesPath.Gui.ArenaSelector.JOIN_RANDOM,
                mat("FIREWORK_ROCKET", "FIREWORK"), "", 0, 40);

        setItem(config, ValuesPath.Gui.ArenaSelector.NEXT_PAGE,
                "CUSTOM_SKULL", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjYxNmUwNjQ5MjJiYzk1MDI3NDE3NWVjMWEyNzUyYmM0M2M1YzllNDVhOTllMTdiZjM1YzY4MGMwMjgwM2U0In19fQ==", 1, 41);

        setItem(config, ValuesPath.Gui.ArenaSelector.PREVIOUS_PAGE,
                "CUSTOM_SKULL", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWRiZWIwZmJjNWU2NjAxZmU1MDQ3MDJjYWZmZGFhYzBlOGVhMTllMTFiY2FkZmJlZTBkMThjODlmNDZiYzFmZCJ9fX0=", 1, 39);

        config.options().copyDefaults(true);
        iConfig.saveConfig();
        return iConfig;
    }

    private static void setItem(FileConfiguration config,
                                String base,
                                String material,
                                String texture,
                                int data,
                                int slot) {

        config.addDefault(base + ValuesPath.Game.Items.MATERIAL, material);
        config.addDefault(base + ValuesPath.Game.Items.TEXTURE, texture);

        config.addDefault(base + ValuesPath.Game.Items.DATA, data);
        if (slot != -1)
            config.addDefault(base + ".slot", slot);
    }

    private static void createCard(FileConfiguration config,
                                   String card,
                                   String texture) {

        String path = ValuesPath.Game.Items.CARD.replace("%card%", card);

        config.addDefault(path + ValuesPath.Game.Items.MATERIAL,
                mat("CUSTOM_SKULL", "CUSTOM_SKULL"));
        config.addDefault(path + ValuesPath.Game.Items.TEXTURE, texture);
        config.addDefault(path + ValuesPath.Game.Items.DATA, 1);
    }

    private static void createVerdictCard(FileConfiguration config,
                                          boolean right,
                                          String card,
                                          String texture) {

        String base = right
                ? ValuesPath.Game.Items.VERDICT_CARDS_RIGHT
                : ValuesPath.Game.Items.VERDICT_CARDS_WRONG;

        String path = base.replace("%card%", card);

        config.addDefault(path + ValuesPath.Game.Items.MATERIAL,
                mat("CUSTOM_SKULL", "CUSTOM_SKULL"));
        config.addDefault(path + ValuesPath.Game.Items.TEXTURE, texture);
        config.addDefault(path + ValuesPath.Game.Items.DATA, 1);
    }

    private static void title(FileConfiguration config,
                              String base,
                              int in, int stay, int out) {

        config.addDefault(base + ValuesPath.Game.Titles.FADE_IN, in);
        config.addDefault(base + ValuesPath.Game.Titles.STAY, stay);
        config.addDefault(base + ValuesPath.Game.Titles.FADE_OUT, out);
    }


    private static String mat(String modern, String legacy) {
        return APIProvider.isHigherVersion ? modern : legacy;
    }
}
