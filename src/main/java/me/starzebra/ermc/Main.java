package me.starzebra.ermc;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public final class Main extends JavaPlugin {

    private static BukkitScheduler scheduler;
    private static Main plugin;

    public static JavaPlugin getInstance(){
        return plugin;
    }

    public static BukkitScheduler getScheduler(){
        return scheduler;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic i.e. register events and commands
        getLogger().info("ERMC plugin has been enabled!");

        PluginManager pm = getServer().getPluginManager();
        //pm.registerEvents(new BlockBreakListener(), this);
        pm.registerEvents(new SnowballTurretListener(), this);
        pm.registerEvents(new BoneInteractListener(), this);
        pm.registerEvents(new ArrowTrailListener(), this);
        plugin = this;
        scheduler = getServer().getScheduler();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic i.e. saving data
        getLogger().info("ERMC plugin has been disabled!");
    }
}
