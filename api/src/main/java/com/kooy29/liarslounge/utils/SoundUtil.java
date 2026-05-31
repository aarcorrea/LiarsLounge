package com.kooy29.liarslounge.utils;

import com.kooy29.liarslounge.APIProvider;
import com.kooy29.liarslounge.api.storage.IConfiguration;
import com.kooy29.liarslounge.storage.yaml.SoundsPath;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SoundUtil {
    private static IConfiguration soundsConfig;

    public static void setupSoundsConfig(IConfiguration iConfig) {
        soundsConfig = iConfig;
        FileConfiguration config = soundsConfig.getConfig();

        config.addDefault(SoundsPath.ArenaStart.TEN_MULTIPLE,
                sound("BLOCK_NOTE_BLOCK_HAT", "NOTE_STICKS") + ";1.0;0.7");

        config.addDefault(SoundsPath.ArenaStart.ONE_TO_FIVE,
                sound("BLOCK_NOTE_BLOCK_HAT", "NOTE_STICKS") + ";1.0;1.0");

        config.addDefault(SoundsPath.ArenaStart.CANCEL_START_1,
                sound("BLOCK_NOTE_BLOCK_PLING", "NOTE_PLING") + ";1.0;2.0");

        config.addDefault(SoundsPath.ArenaStart.CANCEL_START_2,
                sound("BLOCK_NOTE_BLOCK_PLING", "NOTE_PLING") + ";1.0;1.5");

        config.addDefault(SoundsPath.Card.SELECT,
                sound("ENTITY_CHICKEN_EGG", "CHICKEN_EGG_POP") + ";1.0;0.5");

        config.addDefault(SoundsPath.Card.DESELECT,
                sound("ENTITY_CHICKEN_EGG", "CHICKEN_EGG_POP") + ";1.0;0.5");

        config.addDefault(SoundsPath.Card.THROW,
                sound("ENTITY_BAT_TAKEOFF", "BAT_TAKEOFF") + ";1.0;0.5");

        config.addDefault(SoundsPath.CardReveal.RISE,
                sound("ENTITY_LIGHTNING_BOLT_THUNDER", "AMBIENCE_THUNDER") + ";1.0;1.0");

        config.addDefault(SoundsPath.CardReveal.ACCUSER_RIGHT,
                sound("ENTITY_ITEM_BREAK", "ITEM_BREAK") + ";1.0;1.0");

        config.addDefault(SoundsPath.CardReveal.ACCUSER_WRONG,
                sound("ENTITY_VILLAGER_YES", "VILLAGER_YES") + ";1.0;0.8");

        config.addDefault(SoundsPath.TableCard.RISE,
                sound("ENTITY_BLAZE_AMBIENT", "BLAZE_BREATH") + ";1.0;0.5");

        config.addDefault(SoundsPath.CallLiar.CALL,
                sound("ENTITY_BLAZE_DEATH", "BLAZE_DEATH") + ";1.0;0.5");

        config.addDefault(SoundsPath.CallLiar.TRUTH,
                sound("BLOCK_NOTE_BLOCK_PLING", "NOTE_PLING") + ";1.0;1.5");

        config.addDefault(SoundsPath.CallLiar.LIE,
                sound("BLOCK_NOTE_BLOCK_PLING", "NOTE_PLING") + ";1.0;1.5");

        config.addDefault(SoundsPath.CallLiar.AXE_RELOAD,
                sound("ENTITY_ZOMBIE_VILLAGER_CONVERTED", "ZOMBIE_UNFECT") + ";1.0;1.0");

        config.addDefault(SoundsPath.CallLiar.AXE_HALF_SWING,
                sound("ENTITY_ITEM_BREAK", "ITEM_BREAK") + ";1.0;0.5");

        config.addDefault(SoundsPath.CallLiar.AXE_FULL_SWING,
                sound("ENTITY_PLAYER_HURT", "HURT_FLESH") + ";1.0;1.0");

        config.addDefault(SoundsPath.GameEnd.WINNER,
                sound("ENTITY_PLAYER_LEVELUP", "LEVEL_UP") + ";1.0;1.0");

        config.addDefault(SoundsPath.GameEnd.OTHERS,
                sound("ENTITY_PLAYER_LEVELUP", "LEVEL_UP") + ";1.0;1.0");

        config.options().copyDefaults(true);
        iConfig.saveConfig();
    }

    public static void playSound(Player player, String path) {
        try {
            String[] sound = soundsConfig.getConfig().getString(path).split(";");
            player.playSound(player.getLocation(), Sound.valueOf(sound[0]), Float.parseFloat(sound[1]), Float.parseFloat(sound[2]));
        } catch (Exception e) {
            Bukkit.getLogger().severe("Either could not find sound for " + path + " or invalid configuration.");
        }
    }

    private static String sound(String modern, String legacy) {
        return APIProvider.isHigherVersion ? modern : legacy;
    }

    public static void reload() {
        soundsConfig.reloadConfig(false);
    }
}