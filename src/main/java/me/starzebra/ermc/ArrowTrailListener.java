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

    long shootDelay = 100;
    long lastShot = 0;

    boolean abilityReady = false;

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
    public void onRightClickBow(PlayerInteractEvent event){
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
            Arrow special_arrow = player.launchProjectile(Arrow.class, vec.multiply(2));
            special_arrow.setMetadata("special_arrow", new FixedMetadataValue(plugin, true));
            special_arrow.setMetadata("infect_block", new FixedMetadataValue(plugin, true));
        }

    }

    @EventHandler
    public void onLeftClickBow(PlayerInteractEvent event){
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR || event.getItem() == null) return;
        if(event.getItem().getType() != Material.BOW) return;
        if(abilityReady) {
            event.setCancelled(true);
            shootAbilityBeam(event.getPlayer(), 8);
            abilityReady = false;
            event.getPlayer().setMetadata("remaining_hits", new FixedMetadataValue(plugin, 10));
        }
    }

    private void shootAbilityBeam(Player player, int range){
        if(player == null) return;
        Location eyeLoc = player.getEyeLocation();
        Vector dir = eyeLoc.getDirection();

        for (int i = 0; i < range * 2; i++) {
            Particle.DUST.builder()
                    .count(2)
                    .data(new Particle.DustOptions(Color.RED, 1.5f))
                    .location(eyeLoc.clone().add(dir.clone().multiply((i+1)/2f)))
                    .receivers(10).spawn();
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
        Entity hitEntity = event.getHitEntity();
        Block hitBlock = event.getHitBlock();
        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        arrow.setMetadata("inactive", new FixedMetadataValue(plugin, true));
        if(hitEntity != null && arrow.hasMetadata("special_arrow")){
            if(!(hitEntity instanceof LivingEntity entity)) return;
            entity.setNoDamageTicks(2);

            Player shooter = (Player) event.getEntity().getShooter();
            if(shooter == null) return;
            if(shooter.hasMetadata("remaining_hits")){
                int remaining_hits = shooter.getMetadata("remaining_hits").getFirst().asInt();

                if (remaining_hits - 1 <= 0) {
                    abilityReady = true;
                    plugin.getLogger().info("Abiliy charged!");
                } else {
                    shooter.setMetadata("remaining_hits", new FixedMetadataValue(plugin, remaining_hits-1));
                }

            }else{
                shooter.setMetadata("remaining_hits", new FixedMetadataValue(plugin, 10));
            }
        }

        if(hitBlock != null && arrow.hasMetadata("infect_block")){
            arrow.remove();

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
