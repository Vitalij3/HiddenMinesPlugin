package me.salatosik.hiddenminesplugin.utils.configuration.database;

public class DatabaseConfiguration {
    private final String filename;

    public DatabaseConfiguration(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }
}
