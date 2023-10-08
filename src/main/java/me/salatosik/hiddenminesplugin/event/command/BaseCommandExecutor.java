package me.salatosik.hiddenminesplugin.event.command;

import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.event.listener.DatabaseListenerWrapper;
import me.salatosik.hiddenminesplugin.utils.configuration.Configuration;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Logger;

public abstract class BaseCommandExecutor extends DatabaseListenerWrapper.MineDatabaseListener implements CommandExecutor {
    protected final JavaPlugin plugin;
    protected final Database database;
    protected final Configuration configuration;
    protected final Logger logger;

    protected BaseCommandExecutor(JavaPlugin plugin, Database database, Configuration configuration) {
        this.plugin = plugin;
        this.database = database;
        this.configuration = configuration;
        this.logger = plugin.getLogger();

        try { database.subscribeMineListener(this); }
        catch(SQLException sqlException) {
            sqlException.printStackTrace();
            plugin.getPluginLoader().disablePlugin(plugin);
        }
    }
}
