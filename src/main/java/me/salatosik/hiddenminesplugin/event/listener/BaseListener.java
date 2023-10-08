package me.salatosik.hiddenminesplugin.event.listener;

import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.utils.configuration.Configuration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class BaseListener implements Listener {
    private final JavaPlugin plugin;
    private final Database database;
    private final Logger logger;
    private final Configuration configuration;

    public BaseListener(JavaPlugin plugin, Database database, Configuration configuration) {
        this.plugin = plugin;
        this.database = database;
        this.logger = plugin.getLogger();
        this.configuration = configuration;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public Logger getLogger() {
        return logger;
    }

    public Database getDatabase() {
        return database;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
