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
    private long end = plugin.getConfig().getLong("timer") * 1200;
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
        }

        if(!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.AQUA + prefix + "Must be a player to execute!");
            return true;
        }
        Player player = (Player) sender;

        if(!player.hasPermission("tv.vote")) {
            player.sendMessage(ChatColor.AQUA + prefix + "Invalid permissions!");
            return true;
        }
        if(args.length == 0) {
            if(!voteStatus) {
                player.sendMessage(ChatColor.AQUA + prefix + "No current votes found! Start one with /tv <tick>!");
            }
            else {
                if(System.currentTimeMillis() < start+end) {
                    if(!pList.contains(player)) {
                        voteCount++;
                        pList.add(player);
                        player.sendMessage(ChatColor.AQUA + prefix + "Vote casted!");
                        if(voteCount >= (getpOnline()/2)) {
                            double cost = plugin.getConfig().getDouble("cost");
                            for (Player value : pList) {
                                economy.withdrawPlayer(value, cost);
                            }
                            world.setTime(tickVote);
                            Bukkit.broadcastMessage(ChatColor.AQUA + prefix + "Vote has passed, the time has been adjusted!");
                        }
                        return true;
                    }
                    else {
                        player.sendMessage(ChatColor.AQUA + prefix + "You have already casted your vote!");
                    }
                }
                else {
                    setVoteStatus();
                    voteCount = 0;
                    pList.clear();
                    player.sendMessage(ChatColor.AQUA + prefix + "The vote has expired! Please re-issue the command to start a new vote!");
                }
            }
        }
        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("reload")) {
                if(!player.hasPermission("tv.reload")) {
                    player.sendMessage(ChatColor.AQUA + prefix + "You do not have the reload permissions!");
                    return true;
                }
                reloadConfig();
                return true;
            }
            if(!voteStatus && (getpOnline() > 1)) {
                if(args[0].equalsIgnoreCase("day")) {
                    tickVote = 1000;
                }
                if(args[0].equalsIgnoreCase("night")) {
                    tickVote = 13000;
                }
                else {
                    if(!isInt(args[0])) {
                        player.sendMessage(ChatColor.AQUA + prefix + "Tick input is unknown, try day, night or a #!");
                        return true;
                    }
                    tickVote = Integer.parseInt(args[0]);
                }
                start = System.currentTimeMillis();
                world = player.getWorld();
                Bukkit.broadcastMessage(ChatColor.AQUA + prefix + "A day vote has been started! /tv day to vote!");
                Bukkit.broadcastMessage(ChatColor.AQUA + prefix + "The current vote is for the tick time of " + args[0]);
                setVoteStatus();
                voteCount = 1;
                pList.add(player);
            }
            if(!voteStatus && (getpOnline() == 1)) {
                if(args[0].equalsIgnoreCase("day")) {
                    tickVote = 1000;
                }
                if(args[0].equalsIgnoreCase("night")) {
                    tickVote = 13000;
                }
                else {
                    tickVote = Integer.parseInt(args[0]);
                }
                start = System.currentTimeMillis();
                world = player.getWorld();
                if(player.isOnline()) {
                    economy.withdrawPlayer(player, plugin.getConfig().getDouble("cost"));
                }
                world.setTime(tickVote);
                Bukkit.broadcastMessage(ChatColor.AQUA + prefix + "The time has been modified!");
                return true;
            }
            if(voteStatus) {
                player.sendMessage(ChatColor.AQUA + prefix + "There is an ongoing vote already, please wait until the vote is over!");
            }
        }
        else {
            player.sendMessage(ChatColor.AQUA + prefix + "Invalid input!");
            return true;
        }
        return true;
    }

    private void reloadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        prefix = plugin.getConfig().getString("prefix");
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