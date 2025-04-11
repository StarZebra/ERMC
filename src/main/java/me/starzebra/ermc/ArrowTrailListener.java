package me.starzebra.ermc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public class ArrowTrailListener implements Listener {

    final long SHOOT_DELAY = 100;
    final long INFECT_DURATION = 100L;
    final int BEAM_RANGE = 8;
    final int REQ_HITS = 10;
    final String SPECIAL_ARROW_META = "special_arrow";
    final String INFECT_BLOCK_META = "infect_block";
    final String REMAINING_HITS_META = "remaining_hits";
    final List<Material> cycleBlocks = List.of(
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
    final BukkitScheduler scheduler = Main.getScheduler();
    final Plugin plugin = Main.getInstance();
    private final Map<UUID, Boolean> abilityReadyMap = new HashMap<>();

    long lastShot = 0;
    Set<Location> infectedBlocks = new HashSet<>();

    @EventHandler
    public void onRightClickBow(PlayerInteractEvent event){
        if(event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_AIR || event.getItem() == null) return;
        if(event.getItem().getType() != Material.BOW) return;

        event.setCancelled(true);

        long now = System.currentTimeMillis();
        if(now - lastShot < SHOOT_DELAY) return;
        lastShot = now;

        Player player = event.getPlayer();

        Vector[] shootVectors = new Vector[3];
        shootVectors[0] = player.getEyeLocation().getDirection();
        shootVectors[1] = getDirectionWithOffsetYaw(player, 5);
        shootVectors[2] = getDirectionWithOffsetYaw(player, -5);

        player.playSound(player.getEyeLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1.2f);
        for (Vector vec : shootVectors) {
            Arrow special_arrow = player.launchProjectile(Arrow.class, vec.multiply(2));
            special_arrow.setMetadata(SPECIAL_ARROW_META, new FixedMetadataValue(plugin, true));
            special_arrow.setMetadata(INFECT_BLOCK_META, new FixedMetadataValue(plugin, true));
        }

    }

    @EventHandler
    public void onLeftClickBow(PlayerInteractEvent event){
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR || event.getItem() == null) return;
        if(isNotBow(event.getItem())) return;
        if(abilityReadyMap.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            shootAbilityBeam(event.getPlayer(), BEAM_RANGE);
        }
    }

    @EventHandler
    public void onAttackWithBow(EntityDamageByEntityEvent event){
        if(!(event.getDamager() instanceof Player player)) return;
        if(!(event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK)) return;
        if(isNotBow(player.getInventory().getItemInMainHand())) return;

        if(abilityReadyMap.containsKey(player.getUniqueId())){
            shootAbilityBeam(player, BEAM_RANGE);
        }

    }

    @SuppressWarnings("SameParameterValue")
    private void shootAbilityBeam(Player player, int range){
        if(player == null) return;
        Location eyeLoc = player.getEyeLocation();
        Vector dir = eyeLoc.getDirection();
        World world = player.getWorld();

        player.setMetadata(REMAINING_HITS_META, new FixedMetadataValue(plugin, REQ_HITS));
        abilityReadyMap.remove(player.getUniqueId());

        for (int i = 0; i < range * 2; i++) {
            Particle.DUST.builder()
                    .count(2)
                    .data(new Particle.DustOptions(Color.RED, 1.5f))
                    .location(eyeLoc.clone().add(dir.clone().multiply((i+1)/2f)))
                    .receivers(10).spawn();
        }

        List<Entity> piercedEntities = new ArrayList<>();
        Predicate<Entity> nonPiercedEntity = (entity) -> !piercedEntities.contains(entity) && !(entity instanceof Player);

        for (int i = 0; i < 5; i++) {
            RayTraceResult blockCheck = world.rayTraceBlocks(eyeLoc.clone(), dir, range);
            if(blockCheck != null) return;
            RayTraceResult result = world.rayTraceEntities(eyeLoc.clone(), dir, range, 0.3f, nonPiercedEntity);

            if(result == null) return;
            Entity hitEntity = result.getHitEntity();

            if(!(hitEntity instanceof LivingEntity)) return;
            ((LivingEntity) hitEntity).damage(5);
            ((LivingEntity) hitEntity).setNoDamageTicks(2);
            piercedEntities.add(hitEntity);

        }

    }

    private @NotNull Vector getDirectionWithOffsetYaw(@NotNull Player player, double offsetYaw){
        Vector vector = new Vector();
        double rotX = player.getYaw() + offsetYaw;
        double rotY = player.getPitch() + 2;
        vector.setY(-Math.sin(Math.toRadians(rotY)));
        double xz = Math.cos(Math.toRadians(rotY));
        vector.setX(-xz * Math.sin(Math.toRadians(rotX)));
        vector.setZ(xz * Math.cos(Math.toRadians(rotX)));
        return vector;
    }

    private boolean isNotBow(ItemStack item){
        return item == null || item.getType() != Material.BOW;
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

        if(hitEntity != null && arrow.hasMetadata(SPECIAL_ARROW_META)){
            if(!(hitEntity instanceof LivingEntity entity)) return;
            entity.setNoDamageTicks(2);

            Player shooter = (Player) event.getEntity().getShooter();
            if(shooter == null) return;
            if(shooter.hasMetadata(REMAINING_HITS_META)){
                int remaining_hits = shooter.getMetadata(REMAINING_HITS_META).getFirst().asInt();

                if (remaining_hits - 1 <= 0) {
                    if(abilityReadyMap.put(shooter.getUniqueId(), true) == null){
                        Component component = Component.text()
                                .content("Left-click ability charged!").color(TextColor.color(255,255,0))
                                .build();
                        shooter.sendMessage(component);
                        shooter.playSound(shooter.getLocation(), Sound.BLOCK_CONDUIT_ACTIVATE, 1f, 1.5f);
                    }

                } else {
                    shooter.setMetadata(REMAINING_HITS_META, new FixedMetadataValue(plugin, remaining_hits-1));
                }

            }else{
                shooter.setMetadata(REMAINING_HITS_META, new FixedMetadataValue(plugin, REQ_HITS));
            }
        }

        if(hitBlock != null && arrow.hasMetadata(INFECT_BLOCK_META)){
            arrow.remove();

            World world = hitBlock.getWorld();
            Location loc = hitBlock.getLocation();
            BlockData hitBlockData = hitBlock.getBlockData();

            if(infectedBlocks.contains(loc)) return;
            infectedBlocks.add(loc);

            scheduler.runTaskLater(plugin, () -> {
                infectedBlocks.remove(loc);
                world.setBlockData(loc, hitBlockData);
            }, INFECT_DURATION);

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
