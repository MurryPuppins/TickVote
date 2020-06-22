package us.murrypuppins.tickvote;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import us.murrypuppins.tickvote.commands.TickVote;

public class Main extends JavaPlugin implements Listener {

    private Economy econ = null;
    private static Main instance;

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
}