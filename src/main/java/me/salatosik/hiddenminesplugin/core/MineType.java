package me.salatosik.hiddenminesplugin.core;

public enum MineType {
    HOOK("hook"), GROUND("ground"), EMPTY("none");

    public final String mineName;

    MineType(String mineName) {
        this.mineName = mineName;
    }
}
