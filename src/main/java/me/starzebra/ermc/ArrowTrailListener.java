package me.starzebra.ermc;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class ArrowTrailListener implements Listener {

    @EventHandler
    public void onRightClick(PlayerInteractEvent event){
        if(event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR || event.getItem() == null) return;
        if(event.getItem().getType() != Material.BOW) return;
        event.setCancelled(true);

        Player player = event.getPlayer();

        Vector[] shootVectors = new Vector[3];
        shootVectors[0] = player.getEyeLocation().getDirection();
        shootVectors[1] = ERMC$getDirWithYawOffset(player, 5);
        shootVectors[2] = ERMC$getDirWithYawOffset(player, -5);

        player.playSound(player.getEyeLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1.2f);
        for (Vector vec : shootVectors) {
            Arrow specialArrow = player.launchProjectile(Arrow.class, vec.multiply(2));
            specialArrow.setMetadata("specialArrow", new FixedMetadataValue(Main.getInstance(), true));
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

        Main.getScheduler().runTaskTimer(Main.getInstance(), (task) -> {
            if(!arrow.hasMetadata("inactive")){     //vv this bullshit for offsetX is the color of the note?? (n)/24 = color where 0<=n<=24
                Particle.NOTE.builder().offset((arrow.getTicksLived() % 24)/24f, 0, 0).location(arrow.getLocation()).count(0).receivers(32).spawn();
                return;
            }
            task.cancel();
        }, 1L, 2L);

    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event){
        if(event.getEntity().getType() != EntityType.ARROW) return;
        if(!(event.getEntity().getShooter() instanceof Player)) return;
        Arrow arrow = (Arrow) event.getEntity();
        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        arrow.setMetadata("inactive", new FixedMetadataValue(Main.getInstance(), true));
        if(event.getHitEntity() != null && arrow.hasMetadata("specialArrow")){
            LivingEntity entity = (LivingEntity) event.getHitEntity();
            entity.setNoDamageTicks(2);
        }

        Main.getScheduler().runTaskLater(Main.getInstance(), arrow::remove, 100L);
    }

}
