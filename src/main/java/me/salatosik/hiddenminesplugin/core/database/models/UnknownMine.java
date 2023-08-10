package me.salatosik.hiddenminesplugin.core.database.models;

import com.google.common.base.Objects;

public class UnknownMine {
    public final float x;
    public final float y;
    public final float z;

    public UnknownMine(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof UnknownMine) {
            UnknownMine unknownMine = (UnknownMine) o;
            return unknownMine.x == x && unknownMine.y == y && unknownMine.z == z;
        } else return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(x, y, z);
    }
}
