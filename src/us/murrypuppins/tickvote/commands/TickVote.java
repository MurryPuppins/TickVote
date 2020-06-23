package us.murrypuppins.tickvote.commands;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.World;
import us.murrypuppins.tickvote.Main;

import java.util.ArrayList;

public class TickVote implements CommandExecutor {

    private Main plugin = Main.getInstance();
    private boolean voteStatus = false;
    private long start;
    private long end = plugin.getConfig().getInt("timer");
    private int voteCount;
    private int tickVote;
    private ArrayList<Player> pList = new ArrayList<Player>();
    private Economy economy = plugin.getEconomy();
    private World world;
    private String prefix = plugin.getConfig().getString("prefix");

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(!(sender instanceof Player) && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            Bukkit.getLogger().info(ChatColor.translateAlternateColorCodes('&', prefix + "Reload complete!"));
            return true;
        }

        if(!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "Must be a player to execute!"));
            return true;
        }
        Player player = (Player) sender;

        if(!player.hasPermission("tv.vote")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Invalid permissions!"));
            return true;
        }
        if(args.length == 0) {
            if(!voteStatus) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "No current votes found! Start one with /tv <tick>!"));
                return true;
            }
            else {
                if(System.currentTimeMillis() < (start+(end*60000))) {
                    if(!pList.contains(player)) {
                        voteCount++;
                        pList.add(player);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "Vote casted!"));
                        //TODO
                        // Add config variable for chance at when vote is executed
                        if(voteCount >= (getpOnline()/2)) {
                            double cost = plugin.getConfig().getDouble("cost");
                            for (Player value : pList) {
                                economy.withdrawPlayer(value, cost);
                            }
                            world.setTime(tickVote);
                            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',prefix + "Vote has passed, the time has been adjusted!"));
                            pList.clear();
                            setVoteStatus();
                        }
                        return true;
                    }
                    else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "You have already casted your vote!"));
                        return true;
                    }
                }
                else {
                    setVoteStatus();
                    voteCount = 0;
                    pList.clear();
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "The vote has expired! Please re-issue the command to start a new vote!"));
                }
            }
        }
        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("reload")) {
                if(!player.hasPermission("tv.reload")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "You do not have the reload permissions!"));
                    return true;
                }
                reloadConfig();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Reload complete!"));
                return true;
            }
            if(!voteStatus && (getpOnline() > 1)) {
                if(!isInt(args[0])) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Tick input is unknown, try day, night or a #!"));
                    return true;
                }
                setVoteStatus();
                tickVote = Integer.parseInt(args[0]);
                start = System.currentTimeMillis();
                world = player.getWorld();
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + "A tick vote for the tick time of " + args[0]) + " has been started! Vote with '/tv'");
                voteCount = 1;
                pList.add(player);
                return true;
            }
            if(!voteStatus && (getpOnline() == 1)) {
                if(!isInt(args[0])) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Tick input is unknown, try entering a number between 0 and 24000!"));
                    return true;
                }
                tickVote = Integer.parseInt(args[0]);
                start = System.currentTimeMillis();
                world = player.getWorld();
                if(player.isOnline()) {
                    economy.withdrawPlayer(player, plugin.getConfig().getDouble("cost"));
                }
                world.setTime(tickVote);
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + "The time has been modified!"));
                return true;
            }
            if(voteStatus) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "There is an ongoing vote already, please wait until the vote is over!"));
                return true;
            }
        }
        else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Invalid input!"));
            return true;
        }
        return true;
    }

    private void reloadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        prefix = plugin.getConfig().getString("prefix");
        end = plugin.getConfig().getInt("timer");
    }

    private void setVoteStatus() {
        voteStatus = !voteStatus;
    }

    private int getpOnline() {
        return plugin.getServer().getOnlinePlayers().size();
    }

    private boolean isInt(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}