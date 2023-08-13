package me.salatosik.hiddenminesplugin.core.database.models;

public enum MineType {
    HOOK("hook"), GROUND("ground");

    public final String mineName;

    MineType(String mineName) {
        this.mineName = mineName;
    }
}
