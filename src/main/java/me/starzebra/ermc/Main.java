package me.starzebra.ermc;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.starzebra.ermc.commands.StopProtectCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public final class Main extends JavaPlugin {

    private static BukkitScheduler scheduler;
    private static Main plugin;

    public static JavaPlugin getInstance(){
        return plugin;
    }

    public static BukkitScheduler getScheduler() { return scheduler; }




    @SuppressWarnings("UnstableApiUsage") // for lens to not tweak with commands
    @Override
    public void onEnable() {
        // Plugin startup logic i.e. register events and commands
        getLogger().info("ERMC plugin has been enabled!");
        plugin = this;
        scheduler = getServer().getScheduler();

        //Register commands
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(StopProtectCommand.createCommand().build());
        });

        PluginManager pm = getServer().getPluginManager();
        //pm.registerEvents(new BlockBreakListener(), this);
        pm.registerEvents(new SnowballTurretListener(), this);
        pm.registerEvents(new BoneInteractListener(), this);
        pm.registerEvents(new ArrowTrailListener(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic i.e. saving data
        getLogger().info("ERMC plugin has been disabled!");
    }
}
