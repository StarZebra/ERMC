package me.starzebra.ermc;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.List;
import java.util.Map;

public class EntityDeathListener implements Listener {



    @EventHandler
    public void onBlazeAmalgamationDeath(EntityDeathEvent event){
        if(event.getEntityType() != EntityType.BLAZE) return;
        if(!event.getEntity().hasMetadata("amalgamation")) return;
        Entity entity = event.getEntity();
        List<Entity> passengers = entity.getPassengers();

        Entity[] killArray = new Entity[3];
        killArray[2] = entity;

        //top one gets killed
        if(passengers.isEmpty()){
            if(entity.getVehicle() == null) return;
            killArray[1] = entity.getVehicle();
            killArray[0] = killArray[1].getVehicle();

        } else {
            //middle one gets killed
            if(entity.getVehicle() != null){
                killArray[0] = entity.getVehicle();
                killArray[1] = passengers.getFirst();
            }else{ //bottom one gets killed
                killArray[0] = passengers.getFirst();
                killArray[1] = killArray[0].getPassengers().getFirst();
            }
        }

        Main.getInstance().getLogger().info("Death location "+entity.getLocation());

        for(Entity e : killArray){
            e.remove();
        }

        Map<Location, Boolean> spawnLocations = ServerTickListener.getSpawnLocations();

        for (Location location : spawnLocations.keySet()){
            if(entity.getLocation().getX() == location.getX() && entity.getLocation().getZ() == location.getZ()){
                spawnLocations.put(location, false);
                break;
            }
        }

    }

}
