package me.starzebra.ermc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;

import java.util.*;

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
    private final List<BlockFace> blockFaces = List.of(
            BlockFace.UP,
            BlockFace.DOWN,
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.EAST,
            BlockFace.WEST
    );

    @EventHandler
    public void onRightClick(PlayerInteractEvent event){
        if(event.getAction() != Action.RIGHT_CLICK_AIR) return;
        if(event.getItem() == null) return;
        if(!allowedItems.contains(event.getItem().getType())) return;
        int sweep = event.getItem().getPersistentDataContainer().getOrDefault(NamespacedKey.minecraft("sweep"), PersistentDataType.INTEGER, 0);
        if(sweep <= 0) return;

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
                        axeEntity.remove();
                        cancel();
                        Set<Block> connectedLogs = getConnectedLogsInclusive(hitBlock, sweep);
                        Iterator<Block> iterator = connectedLogs.stream().iterator();
                        plugin.getLogger().info("Sweeping with "+ sweep);
                        new BukkitRunnable(){
                            @Override
                            public void run() {
                                if(!connectedLogs.isEmpty()){
                                    if(!iterator.hasNext()) {
                                        cancel();
                                        return;
                                    }
                                    Block b = iterator.next();
                                    world.setBlockData(b.getLocation(), Material.AIR.createBlockData());
                                    Location loc = b.getLocation().add(new Vector(0.5,0.5,0.5));
                                    Particle.BLOCK.builder()
                                            .count(10)
                                            .data(b.getBlockData())
                                            .location(loc)
                                            .receivers(10).spawn();
                                    world.playSound(loc, Sound.BLOCK_WOOD_BREAK, 1, 1.5f);
                                }else{
                                    cancel();
                                }
                            }
                        }.runTaskTimer(plugin, 0L, 1L);

                    }

                }
            }.runTaskTimer(plugin, 0L, 1L);

        } else {
            Component feedbackMessage = Component.text()
                    .content("There are no blocks in range!")
                    .color(NamedTextColor.RED).build();
            player.sendMessage(feedbackMessage);
        }
    }

    private boolean isLog(Material inMat){
        return inMat.toString().endsWith("_LOG");
    }

    private Set<Block> getConnectedLogsInclusive(Block origin, int limit){
        Set<Block> logList = new HashSet<>();
        LinkedList<Block> toLoop = new LinkedList<>();

        logList.add(origin);
        toLoop.add(origin);

        while ((origin = toLoop.poll()) != null && logList.size() < limit){
            getConnectedBlocks(origin, logList, toLoop);
        }
        return logList;

    }

    private void getConnectedBlocks(Block block, Set<Block> results, List<Block> todo){
        for (BlockFace face : blockFaces){
            Block b = block.getRelative(face);
            if(isLog(b.getType())){
                if(results.add(b)){
                    todo.add(b);
                }
            }
        }
    }

}



