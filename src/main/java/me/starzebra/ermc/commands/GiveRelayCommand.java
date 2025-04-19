package me.starzebra.ermc.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.starzebra.ermc.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class GiveRelayCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand(){
        return Commands.literal("giverelay")
                .executes(GiveRelayCommand::runGiveRelay);
    }

    private static int runGiveRelay(CommandContext<CommandSourceStack> context){
        CommandSender sender = context.getSource().getSender();
        if(!(sender instanceof Player player)) return Command.SINGLE_SUCCESS;
        ItemStack relay = createRelayItem();
        player.getInventory().addItem(relay);
        return Command.SINGLE_SUCCESS;
    }

    private static ItemStack createRelayItem(){
        ItemStack relay = ItemStack.of(Material.PAPER);
        ItemMeta meta = relay.getItemMeta();
        meta.displayName(Component.text()
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .content("Void Relay").color(NamedTextColor.LIGHT_PURPLE).build());
        relay.setItemMeta(meta);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text()
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .content("Throws a void relay in the direction").color(NamedTextColor.GRAY)
                .append(Component.text().content(" you're looking.")).build());
        relay.lore(lore);

        NamespacedKey key = new NamespacedKey(Main.getInstance(), "relay_id");

        String relayID = UUID.randomUUID().toString().substring(0, 6);

        relay.editPersistentDataContainer((c) -> c.set(key, PersistentDataType.STRING, relayID));
        return relay;
    }

}
