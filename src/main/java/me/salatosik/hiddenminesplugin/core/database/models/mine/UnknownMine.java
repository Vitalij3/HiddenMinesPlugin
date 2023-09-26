package me.salatosik.hiddenminesplugin.core.database.models.mine;

import com.google.common.base.Objects;
import me.salatosik.hiddenminesplugin.core.database.models.CoordinateObject;
import me.salatosik.hiddenminesplugin.core.database.models.DatabaseObject;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class UnknownMine extends CoordinateObject implements DatabaseObject {
    protected final int x;
    protected final int y;
    protected final int z;
    protected final World.Environment worldType;

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

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public World.Environment getWorldType() {
        return worldType;
    }
}
