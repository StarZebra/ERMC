package me.starzebra.ermc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;

import java.util.List;

public class AxeThrowListener implements Listener {

    private final int AXE_THROW_LENGTH = 10;
    private final Plugin plugin = Main.getInstance();
    private final List<Material> allowedItems = List.of(
            Material.WOODEN_AXE,
            Material.STONE_AXE,
            Material.IRON_AXE,
            Material.GOLDEN_AXE,
            Material.DIAMOND_AXE,
            Material.NETHERITE_AXE
    );

    @EventHandler
    public void onRightClick(PlayerInteractEvent event){
        if(event.getAction() != Action.RIGHT_CLICK_AIR) return;
        if(event.getItem() == null) return;
        if(!allowedItems.contains(event.getItem().getType())) return;

        Player player = event.getPlayer();
        World world = player.getWorld();
        RayTraceResult result = player.rayTraceBlocks(AXE_THROW_LENGTH);
        if(result != null) {
            Block hitBlock = result.getHitBlock();
            if(hitBlock == null) return;
            Material hitMaterial = hitBlock.getType();
            if(!isLog(hitMaterial)) return;

            Vector dir = player.getLocation().getDirection();
            Location eyeLoc = player.getEyeLocation();

            ItemDisplay axeEntity = (ItemDisplay) world.spawnEntity(player.getEyeLocation(), EntityType.ITEM_DISPLAY);
            axeEntity.setItemStack(event.getItem());

            new BukkitRunnable(){
                private int angle = 0;
                private boolean hasHitBlock = false;
                @Override
                public void run() {
                    if(!hasHitBlock){ // AAAAAAAAAHHHHHHHHHHHHHHHH DISPLAY ENTIITES I HATE!!!
                        Matrix4f rotationMatrix = new Matrix4f(
                                0,0,1,0,
                                0,1,0,0,
                                -1,0,0,0,
                                0,0,0,1)
                                .rotate(new AxisAngle4f((float) -Math.toRadians(angle), 0, 0, 1) );
                        axeEntity.setTransformationMatrix(rotationMatrix);
                        axeEntity.teleport(eyeLoc.add(dir.clone().multiply(0.7f)));
                        if(axeEntity.getLocation().distanceSquared(hitBlock.getLocation().add(new Vector(0.5, 0.5, 0.5))) < 1f) hasHitBlock = true;
                        angle += 36;
                    }else{
                        cancel();
                    }

                }
            }.runTaskTimer(plugin, 0L, 1L);

            new BukkitRunnable(){
                @Override
                public void run() {
                     axeEntity.remove();
                }
            }.runTaskLater(plugin, 60L);

        } else {
            Component feedbackMessage = Component.text()
                    .content("There are no blocks in range!")
                    .color(NamedTextColor.RED).build();
            player.sendMessage(feedbackMessage);
        }
    }

    private boolean isLog(Material inMat){
        return inMat.toString().contains("_LOG");
    }
}

