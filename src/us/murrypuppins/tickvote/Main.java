package us.murrypuppins.tickvote;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import us.murrypuppins.tickvote.commands.TickVote;

import java.io.File;
import java.io.IOException;

/**
 * Hello! Looks like you decompiled my plugin TickVote!
 * Nothing too fancy here, it's my first plugin ever anyways <3
 * Hopefully you enjoy it and if you have any suggestions, please
 * let me know in DM's on SpigotMC :)
 */

public class Main extends JavaPlugin implements Listener {

    private Economy econ = null;
    private static Main instance;
    private File customMessagesFile;
    private FileConfiguration customMessagesConfig;

    @Override
    public void onEnable(){
        instance = this;
        Bukkit.getServer().getLogger().info("Enabling TickVote " + this.getDescription().getVersion());
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        if(!setupEconomy()) {
            getLogger().severe("Disabled due to lack of Vault plugin!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        this.getCommand("tv").setExecutor(new TickVote());
        this.saveDefaultConfig();
        this.customConfiguration();

        // Enabling bStats metrics
        int pluginId = 11729;
        Metrics metrics = new Metrics(this, pluginId);
    }

    @Override
    public void onDisable(){
        this.saveDefaultConfig();
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if(rsp == null) {
            return false;
        }
        this.econ = rsp.getProvider();
        return true;
    }

    public Economy getEconomy() {
        return econ;
    }

    public static Main getInstance() {
        return instance;
    }

    public FileConfiguration getCustomConfig() {
        if(customMessagesConfig == null) {
            customConfiguration();
        }
        return this.customMessagesConfig;
    }

    public void reloadCustomConfig() {
        customMessagesConfig = new YamlConfiguration();
        try {
            customMessagesConfig.load(customMessagesFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void customConfiguration() {
        customMessagesFile = new File(getDataFolder(), "messages.yml");
        if (!customMessagesFile.exists()) {
            customMessagesFile.getParentFile().mkdirs();
            saveResource("messages.yml", false);
        }

        customMessagesConfig = new YamlConfiguration();
        try {
            customMessagesConfig.load(customMessagesFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}