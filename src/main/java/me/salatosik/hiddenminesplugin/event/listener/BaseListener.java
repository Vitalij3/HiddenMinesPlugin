package me.salatosik.hiddenminesplugin.event.listener;

import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.utils.configuration.Configuration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public abstract class BaseListener implements Listener {
    protected final JavaPlugin plugin;
    protected final Database database;
    protected final Logger logger;
    protected final Configuration configuration;

    public BaseListener(JavaPlugin plugin, Database database, Logger logger, Configuration configuration) {
        this.plugin = plugin;
        this.database = database;
        this.logger = logger;
        this.configuration = configuration;
    }
}
