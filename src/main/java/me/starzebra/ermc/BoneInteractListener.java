package me.starzebra.ermc;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Random;

public class BoneInteractListener implements Listener {

    @EventHandler
    public void onRightClick(PlayerInteractEvent event){
        if(event.getHand() == EquipmentSlot.OFF_HAND || event.getAction().isLeftClick()) return;
        ItemStack heldItem = event.getItem();
        if(heldItem == null) return;
        if(heldItem.getType() != Material.BONE) return;

        Player player = event.getPlayer();
        World world = player.getWorld();
        Random rand = new Random();

        int cRadius = 1;
        Main.getScheduler().runTaskTimer(Main.getInstance(), (task) -> {
            if(player.getInventory().getItemInMainHand().getType() == Material.BONE){
                double angle = rand.nextDouble(0,Math.PI*2);
                double randX = Math.sin(angle) * rand.nextDouble(0,cRadius);
                double randZ = Math.cos(angle) * rand.nextDouble(0,cRadius);
                Vector offset = new Vector(randX,1,randZ);
                world.spawnEntity(player.getLocation().add(offset), EntityType.ARROW, CreatureSpawnEvent.SpawnReason.BUILD_WITHER);
            }else{
                task.cancel();
            }
        }, 0L, 40L);

    }

}
