package me.starzebra.ermc;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;

public class ServerTickListener implements Listener {

    private static final World world = Main.getInstance().getServer().getWorlds().getFirst();

    private static Map<Location, Boolean> spawnLocations = new HashMap<>();

    private void initSpawnLocations(){
        spawnLocations.put(new Location(world, 1.5, 74, -48.5), false);
        spawnLocations.put(new Location(world, 2.5, 74, -48.5), false);
        spawnLocations.put(new Location(world, 3.5, 74, -48.5), false);
        spawnLocations.put(new Location(world, 4.5, 74, -48.5), false);
        spawnLocations.put(new Location(world, 5.5, 74, -48.5), false);
    }

    public static Map<Location, Boolean> getSpawnLocations() {
        return spawnLocations;
    }

    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent event){
        if(event.getTickNumber() % 20 == 0){
            Main.getInstance().getLogger().info("Server ticked 1 second at tick#"+event.getTickNumber());
            if(spawnLocations.isEmpty()){
                initSpawnLocations();
            }
        }

        if(event.getTickNumber() % 200 == 0){
            for (Location loc : spawnLocations.keySet()){
                if(loc.getNearbyPlayers(50, 20).isEmpty()) return;
                if(!trySpawnBlazeAmalgamation(loc)){
                    Main.getInstance().getLogger().info("Failed to spawn blaze amalgamation at "+loc);
                }
            }
        }
    }

    private boolean trySpawnBlazeAmalgamation(Location spawnLocation){
        if(spawnLocations.get(spawnLocation)) return false;

        Entity[] entities = new Entity[3];

        for (int i = 0; i < 3; i++) {
            LivingEntity blaze = world.createEntity(spawnLocation, Blaze.class);
            blaze.setMetadata("amalgamation", new FixedMetadataValue(Main.getInstance(), true));
            blaze.setAI(false);
            blaze.setPersistent(true);
            entities[i] = blaze;
        }

        entities[2].customName(Component.text().content("Dinnerbone").build());
        entities[2].setCustomNameVisible(false);


        entities[1].addPassenger(entities[2]);
        entities[0].addPassenger(entities[1]);

        world.addEntity(entities[0]);

        spawnLocations.put(spawnLocation, true);
        return true;

    }
}
