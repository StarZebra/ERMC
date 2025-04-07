package me.starzebra.ermc;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class SnowballTurretListener implements Listener {

    boolean keepAlive = true;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        Main.getScheduler().runTaskTimer(Main.getInstance(), (task) -> {
            if(keepAlive){

                List<Vector> vectorList = new ArrayList<>();

                List<Entity> nearbyEntities = player.getNearbyEntities(5,5,5);

                nearbyEntities.forEach((entity -> {
                    if(!(entity instanceof LivingEntity)) return;
                    vectorList.add(entity.getLocation().subtract(player.getLocation()).toVector().normalize());
                }));

                vectorList.forEach((pos) -> {
                    Snowball protBall = player.launchProjectile(Snowball.class, pos);
                    protBall.setMetadata("protBall", new FixedMetadataValue(Main.getInstance(), true));
                });
                return;
            }
            task.cancel();
        }, 0L, 1L);
    }

    @EventHandler
    public void onSnowballHit(ProjectileHitEvent event){
        if(!(event.getEntity() instanceof Snowball ball)) return;
        if(!ball.hasMetadata("protBall")) return;
        LivingEntity hitEntity = (LivingEntity) event.getHitEntity();
        if(hitEntity != null){
            hitEntity.setNoDamageTicks(0);
            hitEntity.setVelocity(event.getEntity().getVelocity().multiply(3).add(new Vector(0,1,0)));
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        keepAlive = false;
    }

}
