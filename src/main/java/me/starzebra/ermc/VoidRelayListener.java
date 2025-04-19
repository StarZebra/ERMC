package me.starzebra.ermc;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

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
        });
        //TODO: make this an invis armor stand instead of just anything that has gravity and is invisible unlike displays :(
        Item item = (Item) world.spawnEntity(player.getEyeLocation(), EntityType.ITEM);
        item.setItemStack(ItemStack.of(Material.END_ROD));
        item.setCanPlayerPickup(false);
        item.setCanMobPickup(false);
        item.setVelocity(player.getLocation().getDirection());
        item.addPassenger(relayProjectile);

        Main.getScheduler().runTaskTimer(plugin, (task) -> {
            if(item.isOnGround()){
                Location landLocation = item.getLocation();
                plugin.getLogger().info("Landed at "+item.getLocation());
                item.remove();
                //relayProjectile.remove();
                spawnVoidRelay(landLocation);
                task.cancel();
            }
        },0L, 1L);

    }

    private void spawnVoidRelay(Location origin){
        origin.setYaw(0);
        origin.setPitch(0);
        List<BlockDisplay> relayBlocks = createVoidRelay(origin);
        for (BlockDisplay bd : relayBlocks){
            bd.spawnAt(origin);
        }
    }

    private List<BlockDisplay> createVoidRelay(Location loc){
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
        list.add(middle);

        BlockDisplay corner1 = world.createEntity(loc, BlockDisplay.class);
        corner1.setBlock(Material.GRAY_CONCRETE.createBlockData());
        Transformation cornerTrans1 = middle.getTransformation();
        Transformation scaledCornerTrans1 = new Transformation(
                cornerTrans1.getTranslation().add(0.35f,-0.2f,0.35f),
                cornerTrans1.getLeftRotation(),
                new Vector3f(0.75f,0.75f,0.75f),
                cornerTrans1.getRightRotation()
        );
        corner1.setRotation(0,0);
        corner1.setTransformation(scaledCornerTrans1);
        list.add(corner1);

        BlockDisplay corner2 = world.createEntity(loc, BlockDisplay.class);
        corner2.setBlock(Material.GRAY_CONCRETE.createBlockData());
        Transformation cornerTrans2 = middle.getTransformation();
        Transformation scaledCornerTrans2 = new Transformation(
                transformation.getTranslation().add(-0.35f,-0.2f,0.35f),
                cornerTrans2.getLeftRotation(),
                new Vector3f(0.75f,0.75f,0.75f),
                cornerTrans2.getRightRotation()
        );
        corner2.setRotation(0,0);
        corner2.setTransformation(scaledCornerTrans2);
        list.add(corner2);

        BlockDisplay corner3 = world.createEntity(loc, BlockDisplay.class);
        corner3.setBlock(Material.GRAY_CONCRETE.createBlockData());
        Transformation cornerTrans3 = middle.getTransformation();
        Transformation scaledCornerTrans3 = new Transformation(
                transformation.getTranslation().add(0,0,-0.75f),
                cornerTrans3.getLeftRotation(),
                new Vector3f(0.75f,0.75f,0.75f),
                cornerTrans3.getRightRotation()
        );
        corner3.setRotation(0,0);
        corner3.setTransformation(scaledCornerTrans3);
        list.add(corner3);

        BlockDisplay corner4 = world.createEntity(loc, BlockDisplay.class);
        corner4.setBlock(Material.GRAY_CONCRETE.createBlockData());
        Transformation cornerTrans4 = middle.getTransformation();
        Transformation scaledCornerTrans4 = new Transformation(
                transformation.getTranslation().add(0.7f,0,0),
                cornerTrans4.getLeftRotation(),
                new Vector3f(0.75f,0.75f,0.75f),
                cornerTrans4.getRightRotation()
        );
        corner4.setRotation(0,0);
        corner4.setTransformation(scaledCornerTrans4);
        list.add(corner4);
        return list;
    }
}
