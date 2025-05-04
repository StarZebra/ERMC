package me.starzebra.ermc;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class VoidRelay {
    NamespacedKey key = new NamespacedKey(Main.getInstance(), "relay_id");

    Location center;
    String ID;
    List<BlockDisplay> parts = new ArrayList<>();

    public VoidRelay(Location c, String id){
        this.center = c;
        center.setPitch(0);
        center.setYaw(0);
        this.ID = id;
    }

    public void createAndSpawnRelay(){
        World world = center.getWorld();

        BlockDisplay middle = world.createEntity(center, BlockDisplay.class);
        middle.setBlock(Material.PURPLE_CONCRETE.createBlockData());
        Transformation transformation = middle.getTransformation();
        Transformation scaledTransformation = new Transformation(
                transformation.getTranslation().add(-0.35f, 0, -0.35f),
                transformation.getLeftRotation(),
                new Vector3f(0.75f,0.75f,0.75f),
                transformation.getRightRotation()
        );
        middle.setRotation(0,0);
        middle.setTransformation(scaledTransformation);
        middle.getPersistentDataContainer().set(key, PersistentDataType.STRING, ID);
        parts.add(middle);

        BlockDisplay corners = world.createEntity(center, BlockDisplay.class);
        corners.setBlock(Material.GRAY_CONCRETE.createBlockData());

        Transformation cornersTransformation = new Transformation(
                transformation.getTranslation().add(-0.25f, -0.1f, -0.25f),
                transformation.getLeftRotation(),
                new Vector3f(1.25f, 0.75f, 1.25f),
                transformation.getRightRotation()
        );
        corners.setTransformation(cornersTransformation);
        parts.add(corners);
        spawnRelay();
    }

    private void spawnRelay(){
        for (BlockDisplay bd : parts){
            bd.spawnAt(center);
        }
    }


}
