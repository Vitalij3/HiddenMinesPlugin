package me.salatosik.hiddenminesplugin.utils.configuration;

import me.salatosik.hiddenminesplugin.utils.configuration.database.DatabaseConfiguration;
import me.salatosik.hiddenminesplugin.utils.configuration.mine.MineConfiguration;

public class Configuration {
    public final DatabaseConfiguration databaseConfiguration;
    public final MineConfiguration mineConfiguration;

    public Configuration(DatabaseConfiguration databaseConfiguration, MineConfiguration mineConfiguration) {
        this.databaseConfiguration = databaseConfiguration;
        this.mineConfiguration = mineConfiguration;
    }
}
