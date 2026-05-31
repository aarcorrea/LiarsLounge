package com.kooy29.liarslounge.commands.sub;

import com.kooy29.liarslounge.commands.SubCommand;
import com.kooy29.liarslounge.utils.ExtraUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestCommand implements SubCommand {
    @Override
    public String getName() {
        return "test";
    }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {

        if (ExtraUtil.notAPlayer(sender)) return;

        if (!ExtraUtil.hasPermission(sender, "liarslounge.start", "liarslounge.admin")) return;

        Player p = (Player) sender;

//        Location spawnLoc = p.getEyeLocation().add(p.getLocation().getDirection().normalize().multiply(2));
//        spawnLoc.setYaw(p.getLocation().getYaw() + 180); // face player
//        playAxeSwingAnimation(p, spawnLoc);
//        LiarCall.playAxeSwing(p, args.length != 1, Collections.singletonList(p), () -> {
//
//        });
//        IArena arena = arenaManager.getArenaByPlayer(p);
//        LiarCall lc = new LiarCall(arena);
//        lc.test(p, Collections.singletonList(p));

//        IArena arena = arenaManager.getArenaByPlayer(p);
//        if (arena == null) {
//            MsgUtil.sendConfigMessage(sender, MsgPath.Error.NOT_IN_ARENA);
//            return;
//        }
//
//        for (Player player : arena.getPlayers()) {
//            IArena.GamePlayer gp = IArena.gamePlayers.get(player);
//            p.sendMessage(player.getName() + " - " + gp.shots + " remaing shots");
//        }

//        if (args.length != 1) {
//            p.setExp(0.7f);
//            p.setLevel(3);
//            p.sendMessage(ChatColor.RED + "Not enough args");
//            return;
//        }
//
//        p.setExp(0);
//        p.setLevel(0);
//
////        CardReveal cardReveal = new CardReveal(ExtraUtil.getLobbyLocation());
////        ItemStack[] cards = new ItemStack[]{ArenaManager.getGameItem("king"), ArenaManager.getGameItem("queen")};
////        cardReveal.revealTo(p, cards);

//        Location playerLoc = p.getLocation();
//
//        // === SAME yaw logic as test() ===
//        Vector dir = playerLoc.getDirection();
//        dir.setY(0).normalize().multiply(1.3);
//        Location newLoc = playerLoc.clone().add(dir);
//
//        p.getWorld().spigot().playEffect(p.getEyeLocation(), Effect.SMOKE, 0, 64, 0, 0, 0, 0, 1, 64);

//        ArenaSelectorGUI.openGui(p, 0);
//
//        switch (args[0]) {
//            case "1": ((Arena) arenaManager.getArenaByPlayer(p)).liarCall.moveToPlayer(p, () -> {}); break;
//            case "2": ((Arena) arenaManager.getArenaByPlayer(p)).liarCall.playAxeSwing(p, false, () -> {}); break;
//            case "3": ((Arena) arenaManager.getArenaByPlayer(p)).liarCall.playAxeSwing(p, true, () -> {}); break;
//            case "4": ((Arena) arenaManager.getArenaByPlayer(p)).liarCall.moveBackToLoc(p); break;
////            case "5": ((Arena) arenaManager.getArenaByPlayer(p)).liarCall.destroyAxe(p, () -> {});
////            case "6": ((Arena) arenaManager.getArenaByPlayer(p)).liarCall.setupAxe(p, , () -> {});
//        }
    }
}
