package me.starzebra.ermc;

import org.bukkit.Material;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public final class Main extends JavaPlugin {

    private static BukkitScheduler scheduler;
    private static Main plugin;

    private static List<Material> nonLegacyBlockMaterials;

    public static JavaPlugin getInstance(){
        return plugin;
    }

    public static BukkitScheduler getScheduler(){
        return scheduler;
    }

    public static List<Material> getNonLegacyBlockMaterials(){
        return nonLegacyBlockMaterials;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic i.e. register events and commands
        getLogger().info("ERMC plugin has been enabled!");
        plugin = this;
        scheduler = getServer().getScheduler();

        PluginManager pm = getServer().getPluginManager();
        //pm.registerEvents(new BlockBreakListener(), this);
        pm.registerEvents(new SnowballTurretListener(), this);
        pm.registerEvents(new BoneInteractListener(), this);
        pm.registerEvents(new ArrowTrailListener(), this);

        Predicate<Material> isNonLegacySolidBlock = (block) -> block.isBlock() && !block.isLegacy() && block.isSolid();
        nonLegacyBlockMaterials = Arrays.stream(Material.values()).filter(isNonLegacySolidBlock).toList();


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic i.e. saving data
        getLogger().info("ERMC plugin has been disabled!");
    }
}
