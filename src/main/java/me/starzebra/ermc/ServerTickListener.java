package me.starzebra.ermc;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ServerTickListener implements Listener {

    @EventHandler
    public void onServerTickEnd(ServerTickEndEvent event){
        if(event.getTickNumber() % 20 == 0){
            Main.getInstance().getLogger().info("Server ticked 1 second at tick#"+event.getTickNumber());
        }

    }
}
