package me.starzebra.ermc.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.starzebra.ermc.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

@SuppressWarnings("UnstableApiUsage")
public class StopProtectCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand(){
        return Commands.literal("ermc")
                .then(Commands.literal("stopprotect")
                        .executes(StopProtectCommand::runStopProtect)
                );
    }

    private static int runStopProtect(CommandContext<CommandSourceStack> context){
        Main.getInstance().getLogger().info("HI FROM RUNSTOPPROTECT");
        //DISABLE PROTECTION SNOWBALLS

        CommandSender sender = context.getSource().getSender();
        if(sender instanceof Player player){
            System.out.println("is player");

            if(player.hasMetadata("protect")){
                System.out.println("removed meta protect");
                player.removeMetadata("protect", Main.getInstance());
            }else{
                System.out.println("added meta protect");
                player.setMetadata("protect", new FixedMetadataValue(Main.getInstance(), true));
            }


            return Command.SINGLE_SUCCESS;
        }


        return Command.SINGLE_SUCCESS;
    }

}
