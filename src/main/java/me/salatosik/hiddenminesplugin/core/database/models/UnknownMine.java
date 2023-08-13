package me.salatosik.hiddenminesplugin.core.database.models;

import com.google.common.base.Objects;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.function.Function;

public class UnknownMine {
    public final float x;
    public final float y;
    public final float z;
    public final World.Environment worldType;

    public UnknownMine(float x, float y, float z, World.Environment worldType) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldType = worldType;
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

    public Location toLocation(Function<World.Environment, World> function) {
        return new Location(function.apply(worldType), x, y, z);
    }

    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }
}
