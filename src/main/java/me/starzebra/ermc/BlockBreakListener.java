package me.starzebra.ermc;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class BlockBreakListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        if(player.getGameMode() == GameMode.CREATIVE) return;
        List<PotionEffect> activeEffects = player.getActivePotionEffects().stream().toList();
        if(activeEffects.isEmpty()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 2000000, 0));
        } else {
            activeEffects.listIterator().forEachRemaining((effect) -> {
                if(effect.getType() == PotionEffectType.MINING_FATIGUE){
                    player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 2000000, effect.getAmplifier()+1));
                }
            });
        }
    }
}
