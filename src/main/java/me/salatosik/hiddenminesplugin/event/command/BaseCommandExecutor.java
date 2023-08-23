package me.salatosik.hiddenminesplugin.event.command;

import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.core.database.interfaces.DatabaseListener;
import me.salatosik.hiddenminesplugin.core.database.models.Mine;
import me.salatosik.hiddenminesplugin.utils.configuration.Configuration;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public abstract class BaseCommandExecutor implements DatabaseListener, CommandExecutor {
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

        try { database.subscribeListener(this); }
        catch(SQLException sqlException) {
            sqlException.printStackTrace();
            plugin.getPluginLoader().disablePlugin(plugin);
        }
    }

    @Override
    public void onMineAdd(Mine mine) {
        minesFromDatabase.add(mine);
    }

    @Override
    public void onMineRemove(Mine mine) {
        if(!minesFromDatabase.isEmpty()) minesFromDatabase.remove(mine);
    }

    @Override
    public void onListenerAdded(List<Mine> mines) {
        minesFromDatabase.addAll(mines);
    }

    @Override
    public void onMineRemoveList(List<Mine> removedMines) {
        minesFromDatabase.removeAll(removedMines);
    }
}
