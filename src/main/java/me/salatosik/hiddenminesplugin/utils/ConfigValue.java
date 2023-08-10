package me.salatosik.hiddenminesplugin.utils;

public enum ConfigValue {
    DATABASE("database.filename", "database.db");

    public final String key;
    public final String value;

    ConfigValue(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
