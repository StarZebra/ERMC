package me.starzebra.ermc;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ArrowTrailListener implements Listener {

    long shootDelay = 250;
    long lastShot = 0;

    ArrayList<Location> infectedBlocks = new ArrayList<>();
    List<Material> cycleBlocks = Arrays.asList(
            Material.MAGMA_BLOCK,
            Material.DIAMOND_BLOCK,
            Material.SPRUCE_LOG,
            Material.GLOWSTONE,
            Material.GRAY_TERRACOTTA,
            Material.YELLOW_GLAZED_TERRACOTTA,
            Material.BONE_BLOCK,
            Material.CAULDRON,
            Material.BEACON,
            Material.IRON_BLOCK,
            Material.GOLD_BLOCK);

    BukkitScheduler scheduler = Main.getScheduler();
    Plugin plugin = Main.getInstance();

    @EventHandler
    public void onRightClick(PlayerInteractEvent event){
        if(event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR || event.getItem() == null) return;
        if(event.getItem().getType() != Material.BOW) return;

        event.setCancelled(true);

        long now = System.currentTimeMillis();
        if(now - lastShot < shootDelay) return;
        lastShot = now;

        Player player = event.getPlayer();

        Vector[] shootVectors = new Vector[3];
        shootVectors[0] = player.getEyeLocation().getDirection();
        shootVectors[1] = ERMC$getDirWithYawOffset(player, 5);
        shootVectors[2] = ERMC$getDirWithYawOffset(player, -5);

        player.playSound(player.getEyeLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1.2f);
        for (Vector vec : shootVectors) {
            Arrow specialArrow = player.launchProjectile(Arrow.class, vec.multiply(2));
            specialArrow.setMetadata("specialArrow", new FixedMetadataValue(plugin, true));
            specialArrow.setMetadata("infect_block", new FixedMetadataValue(plugin, true));
        }

    }

    private Vector ERMC$getDirWithYawOffset(Player player, double offsetYaw){
        Vector vector = new Vector();
        double rotX = player.getYaw() + offsetYaw;
        double rotY = player.getPitch() + 2;
        vector.setY(-Math.sin(Math.toRadians(rotY)));
        double xz = Math.cos(Math.toRadians(rotY));
        vector.setX(-xz * Math.sin(Math.toRadians(rotX)));
        vector.setZ(xz * Math.cos(Math.toRadians(rotX)));
        return vector;
    }

    @EventHandler
    public void onProjectileFire(ProjectileLaunchEvent event){
        if(event.getEntity().getType() != EntityType.ARROW) return;
        if(!(event.getEntity().getShooter() instanceof Player)) return;

        Projectile arrow = event.getEntity();

        scheduler.runTaskTimer(plugin, (task) -> {
            if(!arrow.hasMetadata("inactive")){     //vv this bullshit for offsetX is the color of the note?? (n)/24 = color where 0<=n<=24
                Particle.NOTE.builder().offset((arrow.getTicksLived() % 24)/24f, 0, 0).location(arrow.getLocation()).count(0).receivers(32).spawn();
                return;
            }
            task.cancel();
        }, 2L, 2L);

    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event){
        if(event.getEntity().getType() != EntityType.ARROW) return;
        if(!(event.getEntity().getShooter() instanceof Player)) return;
        Arrow arrow = (Arrow) event.getEntity();
        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        arrow.setMetadata("inactive", new FixedMetadataValue(plugin, true));
        if(event.getHitEntity() != null && arrow.hasMetadata("specialArrow")){
            LivingEntity entity = (LivingEntity) event.getHitEntity();
            entity.setNoDamageTicks(2);
        }

        if(event.getHitBlock() != null && arrow.hasMetadata("infect_block")){
            arrow.remove();

            Block hitBlock = event.getHitBlock();
            World world = hitBlock.getWorld();
            Location loc = hitBlock.getLocation();
            BlockData hitBlockData = hitBlock.getBlockData();

            if(infectedBlocks.contains(loc)) return;
            infectedBlocks.add(loc);

            world.setBlockData(loc, Material.BONE_BLOCK.createBlockData());

            scheduler.runTaskLater(plugin, () -> {
                infectedBlocks.remove(loc);
                world.setBlockData(loc, hitBlockData);
            }, 100L);

            scheduler.runTaskTimer(plugin, (task) -> {
                if(infectedBlocks.contains(loc)){
                    world.setBlockData(loc, getRandomBlockData());
                }else {
                    task.cancel();
                }
            }, 0L, 10L);
        }
    }

    private BlockData getRandomBlockData(){
        Random rand = new Random();
        Material randMat = cycleBlocks.get(rand.nextInt(cycleBlocks.size()));
        return randMat.createBlockData();
    }

}
