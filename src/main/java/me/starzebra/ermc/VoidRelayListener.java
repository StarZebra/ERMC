package me.starzebra.ermc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class VoidRelayListener implements Listener {

    Plugin plugin = Main.getInstance();

    Map<String, Location> relayLocations = new HashMap<>();

    NamespacedKey key = new NamespacedKey(plugin, "relay_id");

    @EventHandler
    public void onInteractRelay(PlayerInteractEvent event){
        if(event.getHand() == EquipmentSlot.OFF_HAND || event.getAction().isLeftClick()) return;
        ItemStack heldItem = event.getItem();
        if(heldItem == null) return;
        if(heldItem.getType() != Material.PURPLE_DYE) return;

        Player player = event.getPlayer();
        World world = player.getWorld();

        Predicate<Entity> displayEntity = (entity) -> (entity instanceof Display) && entity.getPersistentDataContainer().has(key);
        RayTraceResult result = world.rayTraceEntities(player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(0.1f)), player.getEyeLocation().getDirection(), 100, 0.5, displayEntity);
        if(result == null) return;
        PersistentDataContainer dataContainer = result.getHitEntity().getPersistentDataContainer();
        Location relayLocation = null;
        String relayID = dataContainer.get(key, PersistentDataType.STRING);
        if(relayID != null){
            relayLocation = relayLocations.get(relayID);
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
                spawnVoidRelay(landLocation, relayID);
                task.cancel();
            }
        },0L, 1L);
    }

    private void spawnVoidRelay(Location origin, String relayID){
        origin.setYaw(0);
        origin.setPitch(0);
        List<BlockDisplay> relayBlocks = createVoidRelay(origin, relayID);
        for (BlockDisplay bd : relayBlocks){
            bd.spawnAt(origin);
        }
    }

    private List<BlockDisplay> createVoidRelay(Location loc, String relayID){
        List<BlockDisplay> list = new ArrayList<>();
        World world = loc.getWorld();

        BlockDisplay middle = world.createEntity(loc, BlockDisplay.class);
        middle.setBlock(Material.PURPLE_CONCRETE.createBlockData());
        Transformation transformation = middle.getTransformation();
        Transformation scaledTransformation = new Transformation( // props to https://misode.github.io/transformation/
                transformation.getTranslation().add(-0.35f, 0, -0.35f),
                transformation.getLeftRotation(),
                new Vector3f(0.75f,0.75f,0.75f),
                transformation.getRightRotation()
        );
        middle.setRotation(0,0);
        middle.setTransformation(scaledTransformation);
        middle.getPersistentDataContainer().set(key, PersistentDataType.STRING, relayID);
        list.add(middle);

        BlockDisplay corners = world.createEntity(loc, BlockDisplay.class);
        corners.setBlock(Material.GRAY_CONCRETE.createBlockData());

        Transformation cornersTransformation = new Transformation(
                transformation.getTranslation().add(-0.25f, -0.1f, -0.25f),
                transformation.getLeftRotation(),
                new Vector3f(1.25f, 0.75f, 1.25f),
                transformation.getRightRotation()
        );
        corners.setTransformation(cornersTransformation);
        list.add(corners);
        return list;
    }
}
