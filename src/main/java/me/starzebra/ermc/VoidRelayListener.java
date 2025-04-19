package me.starzebra.ermc;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class VoidRelayListener implements Listener {

    Plugin plugin = Main.getInstance();

    @EventHandler
    public void on(PlayerInteractEvent event){
        if(event.getHand() == EquipmentSlot.OFF_HAND || event.getAction().isLeftClick()) return;
        ItemStack heldItem = event.getItem();
        if(heldItem == null) return;
        if(heldItem.getType() != Material.PAPER) return;
        if(!heldItem.getPersistentDataContainer().has(new NamespacedKey(plugin, "relay_id"))) return;

        plugin.getLogger().info("Interact with void relay...");

        Player player = event.getPlayer();
        World world = player.getWorld();

        ItemDisplay relayProjectile = world.spawn(player.getEyeLocation(), ItemDisplay.class, (relay) -> {
            relay.setItemStack(ItemStack.of(Material.PAPER));
            relay.setTeleportDuration(2);
        });

        Location eyeLoc = player.getEyeLocation();

        Main.getScheduler().runTaskTimer(plugin, (task) -> {
            if(relayProjectile.getLocation().distanceSquared(player.getEyeLocation()) > 100){
                task.cancel();
                relayProjectile.remove();
            }
            relayProjectile.teleport(eyeLoc.add(new Vector(0.2,0.5,0.2)));
        }, 0L, 1L);

    }
}
