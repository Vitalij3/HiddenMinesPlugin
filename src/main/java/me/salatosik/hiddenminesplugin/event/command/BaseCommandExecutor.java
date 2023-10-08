package me.salatosik.hiddenminesplugin.event.command;

import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.utils.configuration.Configuration;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public abstract class BaseCommandExecutor implements CommandExecutor {
    private final JavaPlugin plugin;
    private final Database database;
    private final Configuration configuration;
    private final Logger logger;

    protected BaseCommandExecutor(JavaPlugin plugin, Database database, Configuration configuration) {
        this.plugin = plugin;
        this.database = database;
        this.configuration = configuration;
        this.logger = plugin.getLogger();
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public Database getDatabase() {
        return database;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Logger getLogger() {
        return logger;
    }
}
