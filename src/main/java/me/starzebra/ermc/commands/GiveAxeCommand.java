package me.starzebra.ermc.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class GiveAxeCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand(){
        return Commands.literal("giveaxe")
                .then(Commands.argument("sweep", IntegerArgumentType.integer(1))
                        .executes(context -> {
                            int sweep = context.getArgument("sweep", Integer.class);
                            return runGiveAxe(context, sweep);
                        })
                );
    }

    private static int runGiveAxe(CommandContext<CommandSourceStack> context, int sweep){
        CommandSender sender = context.getSource().getSender();
        if(!(sender instanceof Player player)) return Command.SINGLE_SUCCESS;
        ItemStack axe = createAxeItem(sweep);
        player.getInventory().addItem(axe);
        return Command.SINGLE_SUCCESS;
    }

    private static ItemStack createAxeItem(int sweep){
        ItemStack axe = new ItemStack(Material.IRON_AXE);
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text()
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .content("Sweep: ").color(NamedTextColor.GRAY)
                .append(Component.text().content("+"+sweep).color(NamedTextColor.GREEN)).build());
        axe.lore(lore);
        axe.editPersistentDataContainer((c) -> c.set(NamespacedKey.minecraft("sweep"), PersistentDataType.INTEGER, sweep));
        return axe;
    }

}
