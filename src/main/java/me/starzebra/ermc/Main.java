package me.starzebra.ermc;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.starzebra.ermc.commands.GiveAxeCommand;
import me.starzebra.ermc.commands.GiveRelayCommand;
import me.starzebra.ermc.commands.StopProtectCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;

public final class Main extends JavaPlugin {

    private static BukkitScheduler scheduler;
    private static Main plugin;
    private static File configFile;

    public static JavaPlugin getInstance(){
        return plugin;
    }
    public static BukkitScheduler getScheduler() {
        return scheduler;
    }
    public static File getConfigFile(){
        return configFile;
    }

    @SuppressWarnings("UnstableApiUsage") // for lens to not tweak with commands
    @Override
    public void onEnable() {
        // Plugin startup logic i.e. register events and commands
        getLogger().info("ERMC plugin has been enabled!");
        plugin = this;
        scheduler = getServer().getScheduler();
        configFile = new File(this.getDataFolder(), "config.yml");

        saveResource("config.yml", false);

        //Register commands
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(StopProtectCommand.createCommand().build());
            commands.registrar().register(GiveAxeCommand.createCommand().build());
            commands.registrar().register(GiveRelayCommand.createCommand().build());
        });

        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new ArrowTrailListener(), this);
        pm.registerEvents(new AxeThrowListener(), this);
        pm.registerEvents(new ServerTickListener(), this);
        pm.registerEvents(new EntityDeathListener(), this);
        pm.registerEvents(new VoidRelayListener(), this);

        //pm.registerEvents(new BoneInteractListener(), this);
        //pm.registerEvents(new BlockBreakListener(), this);
        //pm.registerEvents(new SnowballTurretListener(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic i.e. saving data
        getLogger().info("ERMC plugin has been disabled!");
    }
}
