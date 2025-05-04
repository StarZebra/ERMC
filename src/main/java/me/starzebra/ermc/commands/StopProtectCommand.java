package me.starzebra.ermc.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.starzebra.ermc.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("UnstableApiUsage")
public class StopProtectCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand(){
        return Commands.literal("ermc")
                .then(Commands.literal("stopprotect")
                        .executes(StopProtectCommand::runStopProtect)
                )
                .then(Commands.literal("toggle")
                        .executes(StopProtectCommand::runNexusToggle));
    }

    private static int runNexusToggle(CommandContext<CommandSourceStack> context){
        CommandSender sender = context.getSource().getSender();
        if(sender instanceof Player player){
            World world = player.getWorld();
            TextDisplay textDisplay = world.spawn(player.getLocation(), TextDisplay.class, (display) -> {
                display.text(Component.text().content("X").color(NamedTextColor.DARK_PURPLE).build());
                display.setBillboard(Display.Billboard.CENTER);
                display.setBackgroundColor(Color.AQUA);
                display.setSeeThrough(true);
                display.setTeleportDuration(1);
            });

            //Location fakeRelay = new Location(world, 13.5,71.5,-67.5);

            player.addPassenger(textDisplay);

            return Command.SINGLE_SUCCESS;
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int runStopProtect(CommandContext<CommandSourceStack> context){
        CommandSender sender = context.getSource().getSender();
        if(sender instanceof Player player){

            File file = Main.getConfigFile();
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

            if(player.hasMetadata("protect")){
                config.set("player.snowball_protection", false);
                player.removeMetadata("protect", Main.getInstance());
            }else{
                config.set("player.snowball_protection", true);
                player.setMetadata("protect", new FixedMetadataValue(Main.getInstance(), true));
            }

            try {
                config.save(file);
            }catch (IOException e){
                e.printStackTrace();
            }

            return Command.SINGLE_SUCCESS;
        }


        return Command.SINGLE_SUCCESS;
    }

}
