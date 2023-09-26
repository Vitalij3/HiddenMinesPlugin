package me.salatosik.hiddenminesplugin.core.database.models;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public abstract class CoordinateObject {
    public abstract int getX();
    public abstract int getY();
    public abstract int getZ();
    public abstract World.Environment getWorldType();

    public Location toLocation(JavaPlugin javaPlugin) {
        for(World world: javaPlugin.getServer().getWorlds()) {
            if(world.getEnvironment() == getWorldType()) {
                return new Location(world, getX(), getY(), getZ());
            }
        }

        return null;
    }

    public Location toLocation(World world) {
        return new Location(world, getX(), getY(), getZ());
    }
}
