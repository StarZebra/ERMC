package me.starzebra.ermc.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.starzebra.ermc.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("UnstableApiUsage")
public class StopProtectCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand(){
        return Commands.literal("ermc")
                .then(Commands.literal("stopprotect")
                        .executes(StopProtectCommand::runStopProtect)
                );
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
