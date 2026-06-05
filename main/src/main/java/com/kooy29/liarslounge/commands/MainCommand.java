package com.kooy29.liarslounge.commands;

import com.kooy29.liarslounge.LiarsLounge;
import com.kooy29.liarslounge.api.arena.GameState;
import com.kooy29.liarslounge.api.arena.IArena;
import com.kooy29.liarslounge.arena.ArenaSetupSession;
import com.kooy29.liarslounge.commands.sub.GuiCommand;
import com.kooy29.liarslounge.commands.sub.HelpCommand;
import com.kooy29.liarslounge.commands.sub.ReloadCommand;
import com.kooy29.liarslounge.commands.sub.arena.*;
import com.kooy29.liarslounge.commands.sub.setup.SetupGroup;
import com.kooy29.liarslounge.storage.yaml.MsgPath;
import com.kooy29.liarslounge.utils.ExtraUtil;
import com.kooy29.liarslounge.utils.MsgUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainCommand implements CommandExecutor, TabCompleter {
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public MainCommand() {
        // Ungrouped commands
        register(new HelpCommand());
        register(new ReloadCommand());
        register(new JoinCommand());
        register(new LeaveCommand());
        register(new StartCommand());
        register(new SpectateCommand());
        register(new GuiCommand());
//        register(new TestCommand());

        // Grouped commands
        register(new SetupGroup());
        register(new ArenaGroup());
    }

    private void register(SubCommand cmd) {
        subCommands.put(cmd.getName(), cmd);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            subCommands.get("help").execute(sender, label, args);
            return true;
        }

        SubCommand subCmd = subCommands.get(args[0].toLowerCase());

        if (subCmd != null) subCmd.execute(sender, label, Arrays.copyOfRange(args, 1, args.length));
        else MsgUtil.sendConfigMessage(sender, MsgPath.Error.UNKNOWN_SUBCOMMAND);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Main command completions
            completions.addAll(Arrays.asList("join", "gui", "leave", "spectate", "arena"));
            if (ExtraUtil.hasPermissionNP(sender, "liarslounge.admin", "liarslounge.setup"))
                completions.add("setup");
            if (ExtraUtil.hasPermissionNP(sender, "liarslounge.start"))
                completions.add("start");
        } else if (args.length == 2) {
            // Second argument completions based on first argument
            switch (args[0].toLowerCase()) {
                case "join":
                    completions.addAll(LiarsLounge.getInstance().getArenaManager().getArenasSorted().stream().filter(a -> a.getGameState() != GameState.PLAYING).map(IArena::getName).collect(Collectors.toList()));
                    break;
                case "spectate":
                    completions.addAll(LiarsLounge.getInstance().getArenaManager().getArenasSorted().stream().filter(a -> a.getGameState() == GameState.PLAYING).map(IArena::getName).collect(Collectors.toList()));
                    break;
                case "arena":
                    if (ExtraUtil.hasPermissionNP(sender, "liarslounge.admin"))
                        completions.addAll(Arrays.asList("enable", "disable", "list", "join", "leave", "spectate"));
                    else
                        completions.addAll(Arrays.asList("join", "leave", "spectate"));
                    break;
                case "setup":
                    if (ExtraUtil.hasPermissionNP(sender, "liarslounge.admin", "liarslounge.setup")) {
                        if (sender instanceof Player && ArenaSetupSession.getPlayerSession(((Player) sender)) != null) {
                            completions.addAll(Arrays.asList("waiting", "table", "chair", "action_item", "save", "end"));
                        } else {
                            completions.add("<arena_name>");
                            completions.add("lobby");
                        }
                    }
                    break;
            }
        } else if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "arena":
                    if (args[1].equalsIgnoreCase("join")) {
                        completions.addAll(LiarsLounge.getInstance().getArenaManager().getArenasSorted().stream().filter(a -> a.getGameState() != GameState.PLAYING).map(IArena::getName).collect(Collectors.toList()));
                    } else if (args[1].equalsIgnoreCase("spectate")) {
                        completions.addAll(LiarsLounge.getInstance().getArenaManager().getArenasSorted().stream().filter(a -> a.getGameState() == GameState.PLAYING).map(IArena::getName).collect(Collectors.toList()));
                    } else {
                        if (ExtraUtil.hasPermissionNP(sender, "liarslounge.admin")) {
                            if (args[1].equalsIgnoreCase("enable") || args[1].equalsIgnoreCase("disable")) {
                                completions.addAll(LiarsLounge.getInstance().getArenaManager().getArenasSorted().stream().map(IArena::getName).collect(Collectors.toList()));
                            }
                        }
                    }
                    break;
                case "setup":
                    if (ExtraUtil.hasPermissionNP(sender, "liarslounge.admin", "liarslounge.setup")) {
                        if (sender instanceof Player && ArenaSetupSession.getPlayerSession(((Player) sender)) != null) {
                            if (args[1].equalsIgnoreCase("chair") || args[1].equalsIgnoreCase("action_item")) {
                                completions.addAll(Arrays.asList("add", "remove"));
                            }
                        } else {
                            completions.add("<world_name>");
                        }
                    }
                    break;
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("setup") || ExtraUtil.hasPermissionNP(sender, "liarslounge.admin", "liarslounge.setup")) {
                if ((args[1].equalsIgnoreCase("chair") || args[1].equalsIgnoreCase("action_item"))
                        && args[2].equalsIgnoreCase("remove")) {
                    ArenaSetupSession ss = ArenaSetupSession.getPlayerSession((Player) sender);
                    if (ss != null) {
                        if (args[1].equalsIgnoreCase("chair"))
                            completions.addAll(IntStream.range(0, ss.getChairLocations().size()).mapToObj(String::valueOf).collect(Collectors.toList()));
                        else if (args[1].equalsIgnoreCase("action_item"))
                            completions.addAll(IntStream.range(0, ss.getActionItemsLocation().size()).mapToObj(String::valueOf).collect(Collectors.toList()));
                    }
                }
            }
        }

        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
