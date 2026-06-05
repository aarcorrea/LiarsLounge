package com.kooy29.liarslounge;

import com.kooy29.liarslounge.api.API;
import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.api.arena.IArenaManager;
import com.kooy29.liarslounge.api.gui.IBookGUI;
import com.kooy29.liarslounge.api.hologram.IPlayerHologram;
import com.kooy29.liarslounge.api.nms.CustomConnectionWrapper;
import com.kooy29.liarslounge.api.nms.IVersionWrapper;
import com.kooy29.liarslounge.api.party.IParty;
import com.kooy29.liarslounge.api.storage.IConfiguration;
import com.kooy29.liarslounge.api.storage.database.IDatabase;
import com.kooy29.liarslounge.arena.ArenaManager;
import com.kooy29.liarslounge.arena.ArenaSetupSession;
import com.kooy29.liarslounge.commands.MainCommand;
import com.kooy29.liarslounge.commands.sub.arena.LeaveCommand;
import com.kooy29.liarslounge.gui.BookGUI;
import com.kooy29.liarslounge.hooks.PAFHook;
import com.kooy29.liarslounge.hooks.PAPIHook;
import com.kooy29.liarslounge.listeners.*;
import com.kooy29.liarslounge.listeners.gui.ArenaSelectorListener;
import com.kooy29.liarslounge.nms.WrapperMethods;
import com.kooy29.liarslounge.scoreboard.ScoreboardManager;
import com.kooy29.liarslounge.storage.database.MySQL;
import com.kooy29.liarslounge.storage.database.SQLite;
import com.kooy29.liarslounge.storage.yaml.ArenaConfig;
import com.kooy29.liarslounge.storage.yaml.ConfigPath;
import com.kooy29.liarslounge.storage.yaml.Configuration;
import com.kooy29.liarslounge.storage.yaml.ValuesConfig;
import com.kooy29.liarslounge.utils.AnimationUtil;
import com.kooy29.liarslounge.utils.ExtraUtil;
import com.kooy29.liarslounge.utils.MsgUtil;
import com.kooy29.liarslounge.utils.SoundUtil;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;

import static com.kooy29.liarslounge.utils.MsgUtil.sendConsoleMessage;

public final class LiarsLounge extends JavaPlugin implements API {

    private static LiarsLounge instance;
    private IConfiguration mainConfig;
    private IConfiguration valuesConfig;
    private ArenaManager arenaManager;
    private IVersionWrapper versionWrapper;
    private CustomConnectionWrapper customConnectionWrapper = null;

    private IPlayerHologram playerHolo;
    private IBookGUI bookGUI;
    private IDatabase remoteDatabase;
    private IParty partyHook = null;

    public static LiarsLounge getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        int pluginId = 27116;
        new Metrics(this, pluginId);
        loadLibs();

        String version = Bukkit.getServer().getClass().getName().split("\\.")[3];
        String mcVersion = Bukkit.getServer().getBukkitVersion().split("-")[0];

        // In-case we add support for other versions besides paper
//        switch (mcVersion) {
//            case "1.21.11":
//                version = "v1_21_R7";
//                APIProvider.isHigherVersion = true;
//                break;
//            default:
//                break;
//        }

        if (isModernPaper(mcVersion)) {
            version = "paper";
            APIProvider.isHigherVersion = true;
        }

        mainConfig = new Configuration("config.yml", true);
        MsgUtil.setMsgConfig(new Configuration("messages.yml", true));
        SoundUtil.setupSoundsConfig(new Configuration("sounds.yml", false));
        valuesConfig = ValuesConfig.setupValuesConfig(new Configuration("values.yml", false));

        loadDb();

        try {
            versionWrapper = (IVersionWrapper) Class.forName("com.kooy29.liarslounge.nms." + version + ".VersionWrapper").getConstructors()[0].newInstance(this);
            playerHolo = (IPlayerHologram) Class.forName("com.kooy29.liarslounge.nms." + version + ".hologram.PlayerHologram").getConstructors()[0].newInstance();
            Constructor<?> cardReveal = Class.forName("com.kooy29.liarslounge.nms." + version + ".animation.CardReveal").getConstructors()[0];
            Constructor<?> cardThrow = Class.forName("com.kooy29.liarslounge.nms." + version + ".animation.CardThrow").getConstructors()[0];
            Constructor<?> liarCall = Class.forName("com.kooy29.liarslounge.nms." + version + ".animation.LiarCall").getConstructors()[0];
            Constructor<?> tableCard = Class.forName("com.kooy29.liarslounge.nms." + version + ".animation.TableCard").getConstructors()[0];
            AnimationUtil.setAnimationClass(cardReveal, cardThrow, liarCall, tableCard);
            Object eventListener = Class.forName("com.kooy29.liarslounge.nms." + version + ".Listeners").getConstructors()[0].newInstance(new WrapperMethods(), this);
            getServer().getPluginManager().registerEvents((Listener) eventListener, this);
            if (APIProvider.isHigherVersion)
                bookGUI = (IBookGUI) Class.forName("com.kooy29.liarslounge.nms." + version + ".BookGUI").getConstructors()[0].newInstance();
            else bookGUI = new BookGUI();
            customConnectionWrapper = (CustomConnectionWrapper) Class.forName("com.kooy29.liarslounge.nms." + version + ".CustomConnection")
                    .getConstructors()[0].newInstance(this, new WrapperMethods());
            sendConsoleMessage("&7Found Supported Version: &e" + version + " (" + mcVersion + ")");
        } catch (ClassNotFoundException e) {
            sendConsoleMessage("&cUnsupported server version: " + version + " (" + mcVersion + ")");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } catch (Exception e) {
            sendConsoleMessage("&cError initializing nms");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        APIProvider.setInstance(this);
        arenaManager = new ArenaManager(this, versionWrapper);
        ScoreboardManager.init();
        ArenaConfig.loadArenas();
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PAPIHook().register();
            MsgUtil.sendConsoleMessage("&eHooked into PlaceholderAPI");
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PartyAndFriends")) {
            partyHook = new PAFHook();
            MsgUtil.sendConsoleMessage("&eHooked into PartyAndFriends");
        }

        getCommand("liarslounge").setExecutor(new MainCommand());
        getCommand("leave").setExecutor(new LeaveCommand());
        getServer().getPluginManager().registerEvents(new SeatListener(playerHolo), this);
        getServer().getPluginManager().registerEvents(new ChatListener(arenaManager), this);
        getServer().getPluginManager().registerEvents(new BlocksListener(arenaManager), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(arenaManager), this);
        getServer().getPluginManager().registerEvents(new WeatherSpawnListener(arenaManager), this);
        getServer().getPluginManager().registerEvents(new ArenaSelectorListener(versionWrapper), this);
        getServer().getPluginManager().registerEvents(new ItemsListener(versionWrapper, arenaManager, mainConfig.getConfig()), this);
        getServer().getPluginManager().registerEvents(new CmdListener(arenaManager, mainConfig), this);
        if (valuesConfig.getConfig().getBoolean(ConfigPath.LOBBY_PROT) && ExtraUtil.isLobbyLocation()) ExtraUtil.setGameRules(ExtraUtil.getLobbyLocation().getWorld());
        sendConsoleMessage("&aLiarsLounge has been enabled");
    }

    @Override
    public void onDisable() {
        IArena.gamePlayers.clear();
        if (arenaManager != null)
            arenaManager.clearArenas();
        ArenaSetupSession.endSession();
        Bukkit.getPluginManager().disablePlugin(this);
    }

    @Override
    public IConfiguration getConfiguration() {
        return mainConfig;
    }

    @Override
    public IConfiguration getValuesConfig() {
        return valuesConfig;
    }

    @Override
    public IDatabase getDb() {
        return remoteDatabase;
    }

    @Override
    public IArenaManager getArenaManager() {
        return arenaManager;
    }

    @Override
    public IVersionWrapper getVersionWrapper() {
        return versionWrapper;
    }

    public CustomConnectionWrapper getCustomConnectionWrapper() {
        return customConnectionWrapper;
    }

    public IPlayerHologram getPlayerHolo() {
        return playerHolo;
    }

    private void loadLibs() {
        getLogger().info("Loading libraries...");
        BukkitLibraryManager libraryManager = new BukkitLibraryManager(this);

        Library mysql = new Library.Builder().groupId("com{}mysql").artifactId("mysql-connector-j").version("8.2.0").build();
        Library hikariCP = new Library.Builder().groupId("com{}zaxxer").artifactId("HikariCP").version("5.1.0").build();
        Library slf4j = new Library.Builder().groupId("org{}slf4j").artifactId("slf4j-api").version("2.0.7").build();
        Library slf4j_nop = new Library.Builder().groupId("org{}slf4j").artifactId("slf4j-nop").version("2.0.7").build();

        libraryManager.addMavenCentral();

        libraryManager.loadLibrary(mysql);
        libraryManager.loadLibrary(hikariCP);
        libraryManager.loadLibrary(slf4j);
        libraryManager.loadLibrary(slf4j_nop);
    }

    private void loadDb() {
        if (mainConfig.getConfig().getString(ConfigPath.DATABASE + ".type").equalsIgnoreCase("mysql")) {
            MySQL mySQL = new MySQL(this);
            long time = System.currentTimeMillis();
            if (!mySQL.connect()) {
                this.getLogger().severe("Couldn't connect to MySQL database! Falling back to SQLite (Local DB).");
                remoteDatabase = new SQLite();
            } else {
                remoteDatabase = mySQL;
                MsgUtil.sendConsoleMessage("&6Connected to MySQL database!");
            }
            if (System.currentTimeMillis() - time >= 5000) {
                this.getLogger().severe("Database connection established in "
                        + (System.currentTimeMillis() - time) + " ms. "
                        + "Remote connections may cause significant delays and are not recommended!");
            }
            remoteDatabase.init();
        } else if (mainConfig.getConfig().getString(ConfigPath.DATABASE + ".type").equalsIgnoreCase("sqlite")) {
            remoteDatabase = new SQLite();
            remoteDatabase.init();
        }
    }

    public IParty getPartyHook() {
        return partyHook;
    }

    public boolean isPartyHook() {
        return partyHook != null;
    }

    public IBookGUI getBookGUI() {
        return bookGUI;
    }

    private boolean isModernPaper(String version) {
        String[] split = version.split("\\.");

        int major = Integer.parseInt(split[0]);
        int minor = Integer.parseInt(split[1]);
        int patch = split.length > 2 ? Integer.parseInt(split[2]) : 0;

        // new versioning
        if (major >= 26) {
            return true;
        }

        return minor > 20 || (minor == 20 && patch >= 5);
    }
}
