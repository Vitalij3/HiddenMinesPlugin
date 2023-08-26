package me.salatosik.hiddenminesplugin.core.database.models;

import com.google.common.base.Objects;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class UnknownMine {
    public final int x;
    public final int y;
    public final int z;
    public final World.Environment worldType;

    public UnknownMine(int x, int y, int z, World.Environment worldType) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldType = worldType;
    }

    public UnknownMine(Block block) {
        x = block.getX();
        y = block.getY();
        z = block.getZ();
        worldType = block.getWorld().getEnvironment();
    }

    public UnknownMine(Location location) {
        x = location.getBlockX();
        y = location.getBlockY();
        z = location.getBlockZ();
        worldType = location.getWorld().getEnvironment();
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof UnknownMine) {
            UnknownMine unknownMine = (UnknownMine) o;
            return unknownMine.x == x && unknownMine.y == y && unknownMine.z == z && unknownMine.worldType == worldType;
        } else return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(x, y, z);
    }

    public Location toLocation(JavaPlugin plugin) {
        List<World> worlds = plugin.getServer().getWorlds();
        for(World world: worlds) {
            if(world.getEnvironment() == worldType) {
                return new Location(world, x, y, z);
            }
        }

        return null;
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }
}
