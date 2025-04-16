package me.starzebra.ermc.commands;

import com.destroystokyo.paper.entity.Pathfinder;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;

@SuppressWarnings("UnstableApiUsage")
public class TestMobCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> createCommand(){
        return Commands.literal("testmob")
                .executes(TestMobCommand::runTestMob);
    }

    private static int runTestMob(CommandContext<CommandSourceStack> context){
        CommandSender sender = context.getSource().getSender();
        if(!(sender instanceof Player player)) return Command.SINGLE_SUCCESS;
        World world = player.getWorld();
        Mob mob = world.spawn(player.getLocation(), Zombie.class);

        Pathfinder pathFinder = mob.getPathfinder();
        pathFinder.stopPathfinding();
        pathFinder.moveTo(player.getLocation().add(5,0,0));

        return Command.SINGLE_SUCCESS;
    }
}
