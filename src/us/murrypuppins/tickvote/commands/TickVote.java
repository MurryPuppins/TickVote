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
    private Double percentage = plugin.getConfig().getDouble("percentage");

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        // If console issues reload command
        if(!(sender instanceof Player) && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            Bukkit.getLogger().info(ChatColor.translateAlternateColorCodes('&', prefix + "Reload complete!"));
            return true;
        }

        // If console attempts player-level commands
        if(!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + "Must be a player to execute!"));
            return true;
        }
        Player player = (Player) sender;

        // Checks for player permission
        if(!player.hasPermission("tv.vote")) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + plugin.getCustomConfig().getString("invalid-permissions")));
            return true;
        }

        // If player only issues /tv (to vote, no arguments)
        if(args.length == 0) {
            if(!voteStatus) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + plugin.getCustomConfig().getString("invalid-vote")));
                return true;
            }
            else {
                if(System.currentTimeMillis() < (start+(end*60000))) {
                    if(!pList.contains(player)) {
                        voteCount++;
                        pList.add(player);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + plugin.getCustomConfig().getString("vote-casted")));
                        if(((double)voteCount/getpOnline()) >= (percentage * .01)) {
                            double cost = plugin.getConfig().getDouble("cost");
                            for (Player value : pList) {
                                economy.withdrawPlayer(value, cost);
                            }
                            world.setTime(tickVote);
                            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',prefix + plugin.getCustomConfig().getString("proposed-tick-passed")));
                            pList.clear();
                            setVoteStatus();
                        }
                        return true;
                    }
                    else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&',prefix + plugin.getCustomConfig().getString("vote-already-casted")));
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
            /**
             * Command in progress for help command
             if(args[0].equalsIgnoreCase(("help"))) {
             player.sendMessage()
             }**/
            if(args[0].equalsIgnoreCase("reload")) {
                if(!player.hasPermission("tv.reload")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + plugin.getCustomConfig().getString("invalid-permissions")));
                    return true;
                }
                reloadConfig();
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "Reload complete!"));
                return true;
            }
            if(!voteStatus) {
                if(!isInt(args[0])) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + plugin.getCustomConfig().getString("invalid-tick-time")));
                    return true;
                }
                setVoteStatus();
                tickVote = Integer.parseInt(args[0]);
                start = System.currentTimeMillis();
                world = player.getWorld();
                pList.add(player);
                voteCount = 1;
                if(((double)voteCount/getpOnline()) >= (percentage * .01)) {
                    double cost = plugin.getConfig().getDouble("cost");
                    for (Player value : pList) {
                        economy.withdrawPlayer(value, cost);
                    }
                    world.setTime(tickVote);
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + plugin.getCustomConfig().getString("proposed-tick-passed")));
                    pList.clear();
                    setVoteStatus();
                    return true;
                }
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix
                        + "Vote has started for the tick time of (" + Integer.parseInt(args[0]) + ")!"));
                return true;
            }
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + plugin.getCustomConfig().getString("ongoing-proposed-vote")));
            return true;
        }
        else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + plugin.getCustomConfig().getString("invalid-input")));
            return true;
        }
    }

    private void reloadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        prefix = plugin.getConfig().getString("prefix");
        end = plugin.getConfig().getInt("timer");
        percentage = plugin.getConfig().getDouble("percentage");
        plugin.reloadCustomConfig();
    }

    private void setVoteStatus() {
        this.voteStatus = !this.voteStatus;
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