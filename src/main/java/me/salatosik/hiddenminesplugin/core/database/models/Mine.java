package me.salatosik.hiddenminesplugin.core.database.models;

import org.bukkit.Location;
import org.bukkit.World;

public class Mine extends UnknownMine {
    public final MineType mineType;

    public Mine(float x, float y, float z, MineType mineType, World.Environment worldType) {
        super(x, y, z, worldType);
        this.mineType = mineType;
    }

    public Mine(UnknownMine unknownMine, MineType mineType) {
        super(unknownMine.x, unknownMine.y, unknownMine.z, unknownMine.worldType);
        this.mineType = mineType;
    }

    @Override
    public String toString() {
        return "Mine{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", mineType=" + mineType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Mine) {
            Mine mine = (Mine) o;
            return mine.x == x && mine.y == y && mine.z == z && mine.mineType == mineType && mine.worldType == worldType;
        } else if(o instanceof UnknownMine) {
            UnknownMine unknownMine = (UnknownMine) o;
            return unknownMine.x == x && unknownMine.y == y && unknownMine.z == z && unknownMine.worldType == worldType;
        } else return false;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + mineType.hashCode();
        return result;
    }
}
