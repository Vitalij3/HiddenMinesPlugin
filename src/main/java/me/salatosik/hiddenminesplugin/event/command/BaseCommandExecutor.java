package me.salatosik.hiddenminesplugin.event.command;

import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.core.database.DatabaseListener;
import me.salatosik.hiddenminesplugin.core.database.models.mine.Mine;
import me.salatosik.hiddenminesplugin.utils.configuration.Configuration;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public abstract class BaseCommandExecutor implements DatabaseListener<Mine>, CommandExecutor {
    protected final LinkedList<Mine> minesFromDatabase = new LinkedList<>();
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

    @Override
    public void onItemAdd(Mine mine) {
        minesFromDatabase.add(mine);
    }

    @Override
    public void onItemRemove(Mine mine) {
        if(!minesFromDatabase.isEmpty()) minesFromDatabase.remove(mine);
    }

    @Override
    public void onListenerAdded(List<Mine> mines) {
        minesFromDatabase.addAll(mines);
    }

    @Override
    public void onItemRemoveList(List<Mine> removedMines) {
        minesFromDatabase.removeAll(removedMines);
    }
}
