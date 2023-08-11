package me.salatosik.hiddenminesplugin.core.database.models;

public class Mine extends UnknownMine {
    public final MineType mineType;

    public Mine(float x, float y, float z, MineType mineType) {
        super(x, y, z);
        this.mineType = mineType;
    }

    public Mine(UnknownMine unknownMine, MineType mineType) {
        super(unknownMine.x, unknownMine.y, unknownMine.z);
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
            return mine.x == x && mine.y == y && mine.z == z && mine.mineType == mineType;
        } else if(o instanceof UnknownMine) {
            UnknownMine unknownMine = (UnknownMine) o;
            return unknownMine.x == x && unknownMine.y == y && unknownMine.z == z;
        } else return false;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + mineType.hashCode();
        return result;
    }
}
