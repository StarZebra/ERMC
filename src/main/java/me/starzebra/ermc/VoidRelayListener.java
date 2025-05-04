package me.starzebra.ermc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class VoidRelayListener implements Listener {

    Plugin plugin = Main.getInstance();

    public static  Map<String, Location> relayLocations = new HashMap<>();

    NamespacedKey key = new NamespacedKey(plugin, "relay_id");

    public static Map<String, Location> getRelayLocations(){
        return relayLocations;
    }

    @EventHandler
    public void onInteractRelay(PlayerInteractEvent event){
        if(event.getHand() == EquipmentSlot.OFF_HAND || event.getAction().isLeftClick()) return;
        ItemStack heldItem = event.getItem();
        if(heldItem == null) return;
        if(heldItem.getType() != Material.PURPLE_DYE) return;

        Player player = event.getPlayer();
        World world = player.getWorld();

        Predicate<Entity> displayEntity = (entity) -> (entity instanceof Display) && entity.getPersistentDataContainer().has(key);
        RayTraceResult result = world.rayTraceEntities(player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(0.3f)), player.getEyeLocation().getDirection(), 1000, 0.5, displayEntity);
        if(result == null || result.getHitEntity() == null) return;
        PersistentDataContainer dataContainer = result.getHitEntity().getPersistentDataContainer();
        Location relayLocation = null;
        String relayID = dataContainer.get(key, PersistentDataType.STRING);
        if(relayID != null){
            relayLocation = relayLocations.get(relayID);
            relayLocation.setYaw(player.getYaw());
            relayLocation.setPitch(player.getPitch());
        }
        if(relayLocation != null){
            player.teleport(relayLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
            player.sendMessage(Component.text().content("Teleported to a void ").color(NamedTextColor.LIGHT_PURPLE)
                    .append(Component.text().decoration(TextDecoration.BOLD, true).content("RELAY!").color(NamedTextColor.DARK_PURPLE)
                            .hoverEvent(HoverEvent.showText(Component.text().content(relayID)))));
        }
    }

    //TODO: Make cool animations and sounds for the relay and teleport

    @EventHandler
    public void on(PlayerInteractEvent event){
        if(event.getHand() == EquipmentSlot.OFF_HAND || event.getAction().isLeftClick()) return;
        ItemStack heldItem = event.getItem();
        if(heldItem == null) return;
        if(heldItem.getType() != Material.PAPER) return;
        if(!heldItem.getPersistentDataContainer().has(key)) return;

        Player player = event.getPlayer();
        World world = player.getWorld();

        player.getInventory().remove(heldItem);

        ItemDisplay relayProjectile = world.spawn(player.getEyeLocation(), ItemDisplay.class, (relay) -> {
            relay.setItemStack(ItemStack.of(Material.PAPER));
        });
        //TODO: make this an invis armor stand instead of just anything that has gravity and is invisible unlike displays :(
        Item item = (Item) world.spawnEntity(player.getEyeLocation(), EntityType.ITEM);
        item.setItemStack(ItemStack.of(Material.END_ROD));
        item.setCanPlayerPickup(false);
        item.setCanMobPickup(false);
        item.setVelocity(player.getLocation().getDirection());
        item.addPassenger(relayProjectile);

        String relayID = heldItem.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if(relayID == null) return;

        ItemStack teleportItem = new ItemStack(Material.PURPLE_DYE);
        ItemMeta meta = teleportItem.getItemMeta();
        meta.displayName(Component.text().decoration(TextDecoration.ITALIC, false).content("Relay Teleporter").color(NamedTextColor.DARK_PURPLE).build());
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, relayID);
        teleportItem.setItemMeta(meta);
        player.getInventory().addItem(teleportItem);

        Main.getScheduler().runTaskTimer(plugin, (task) -> {
            if(item.isOnGround()){
                Location landLocation = item.getLocation();
                plugin.getLogger().info("Landed at "+item.getLocation());
                item.remove();
                relayProjectile.remove();
                relayLocations.put(relayID, landLocation);
                VoidRelay relay = new VoidRelay(landLocation, relayID);
                relay.createAndSpawnRelay();
                task.cancel();
            }
        },0L, 1L);
    }

    @EventHandler
    public void on(PlayerItemHeldEvent event){
        if(event.getPlayer().getInventory().getItem(event.getNewSlot()) == null) return;
        if(event.getPlayer().getInventory().getItem(event.getNewSlot()).getType() != Material.PURPLE_DYE) return;

        ItemStack heldItem = event.getPlayer().getInventory().getItem(event.getNewSlot());
        if(heldItem == null) return;
        String id = heldItem.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if(id == null) return;

        World world = event.getPlayer().getWorld();
        Location relayLocation = relayLocations.get(id);

        Main.getScheduler().runTaskTimer(Main.getInstance(), (task) -> {

            if(Objects.equals(event.getPlayer().getInventory().getItemInMainHand().getPersistentDataContainer().get(key, PersistentDataType.STRING), heldItem.getPersistentDataContainer().get(key, PersistentDataType.STRING))) {
                Vector targetVec = relayLocation.toVector().subtract(event.getPlayer().getEyeLocation().toVector());
                Vector dir = event.getPlayer().getEyeLocation().getDirection().multiply(2);

                Location finalLoc = event.getPlayer().getEyeLocation().add(dir.add(targetVec.normalize()).toLocation(world));

                world.spawnParticle(Particle.WITCH, finalLoc,0);
                return;
            }

            task.cancel();

        },0, 1L);
    }
}
