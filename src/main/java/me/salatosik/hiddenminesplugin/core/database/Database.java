package me.salatosik.hiddenminesplugin.core.database;

import java.io.File;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.TableUtils;
import me.salatosik.hiddenminesplugin.core.database.models.mine.Mine;
import me.salatosik.hiddenminesplugin.core.database.orm.table.TableMine;
import me.salatosik.hiddenminesplugin.utils.BukkitRunnableWrapper;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Database {
    private static final String JDBC_PREFIX = "jdbc:sqlite:{url}";
    private final JavaPlugin plugin;

    private final JdbcPooledConnectionSource connectionSource;
    private final Dao<TableMine, Object> minesDao;

    private final LinkedList<DatabaseListener<Mine>> databaseMineListeners = new LinkedList<>();

    public Database(@NotNull File databaseFile, @NotNull JavaPlugin plugin) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        connectionSource = new JdbcPooledConnectionSource(JDBC_PREFIX.replace("{url}", databaseFile.getAbsolutePath()));

        TableUtils.createTableIfNotExists(connectionSource, TableMine.class);
        minesDao = DaoManager.createDao(connectionSource, TableMine.class);

        this.plugin = plugin;

        DatabaseCleaner databaseCleaner = new DatabaseCleaner();
        databaseCleaner.runTaskTimerAsynchronously(plugin, 20, DatabaseCleaner.UPDATE_RATE);
    }

    private <T> void notifyListeners(Consumer<DatabaseListener<T>> consumer, List<DatabaseListener<T>> databaseListeners) {
        BukkitRunnableWrapper bukkitRunnable = new BukkitRunnableWrapper(() -> databaseListeners.forEach(consumer));
        bukkitRunnable.runTask(plugin);
    }

    public void subscribeMineListener(DatabaseListener<Mine> databaseMineListener) throws SQLException {
        databaseMineListener.onListenerAdded(getAllMines());
        databaseMineListeners.add(databaseMineListener);
    }

    public void closePooledConnection() {
        try {
            connectionSource.close();
        } catch(Exception exception) {
            exception.printStackTrace();
        }
    }

    public void addMine(@NotNull Mine mine) throws SQLException {
        TableMine tableMine = new TableMine(mine.getX(), mine.getY(), mine.getZ(), mine.getMineType(), mine.getWorldType());
        minesDao.create(tableMine);
        notifyListeners((listener) -> listener.onItemAdd(mine), databaseMineListeners);
    }

    public void removeMine(@NotNull Mine mine) throws SQLException {
        for(TableMine tableMine: minesDao.queryForAll()) {
            if(tableMine.equalsMine(mine)) {
                minesDao.deleteById(tableMine.getId());
                notifyListeners((listener) -> listener.onItemRemove(mine), databaseMineListeners);
                break;
            }
        }
    }

    public void removeMines(@NotNull List<Mine> mines, @Nullable Consumer<Mine> onEach) throws SQLException {
        LinkedList<Mine> removedMines = new LinkedList<>();

        for(TableMine tableMine: minesDao.queryForAll()) {
            for(Mine mine: mines) {
                if(tableMine.equalsMine(mine)) {
                    minesDao.deleteById(tableMine.getId());
                    removedMines.add(mine);
                    if(onEach != null) onEach.accept(mine);
                }
            }
        }

        notifyListeners((listener) -> listener.onItemRemoveList(removedMines), databaseMineListeners);
    }

    public void removeMines(@NotNull List<Mine> mines) throws SQLException {
        removeMines(mines, null);
    }

    public List<Mine> getAllMines() throws SQLException {
        List<Mine> mines = new LinkedList<>();
        minesDao.queryForAll().forEach((tableMine) -> mines.add(tableMine.toMine()));
        return mines;
    }

    private class DatabaseCleaner extends BukkitRunnable {
        private final Logger logger;

        public static final String LOG_PREFIX = "- [Database Cleaner]: ";
        public static final long UPDATE_RATE = 20L;

        public DatabaseCleaner() {
            logger = plugin.getLogger();
            databaseLog("Database cleaner initialized!", Level.INFO);
        }

        private void databaseLog(String str, Level level) {
            logger.log(level, LOG_PREFIX + str);
        }

        @Override
        public synchronized void run() {
            List<Mine> mines;
            try { mines = getAllMines(); }
            catch(SQLException sqlException) {
                databaseLog("Failed to get all mines!", Level.WARNING);
                return;
            }

            final List<Mine> clearMines = new LinkedList<>();

            for(World world: plugin.getServer().getWorlds()) {
                for(Mine mine: mines) {
                    if(mine.getWorldType() != world.getEnvironment()) continue;

                    Block block = world.getBlockAt(mine.getX(), mine.getY(), mine.getZ());
                    if(block.getType() == Material.AIR || block.getType() == Material.CAVE_AIR ||
                            block.getType() == Material.VOID_AIR) {

                        clearMines.add(mine);
                    }
                }
            }

            if(!clearMines.isEmpty()) {
                BukkitRunnableWrapper bukkitRunnableWrapper = new BukkitRunnableWrapper(() -> {
                    try { removeMines(clearMines); }
                    catch(SQLException sqlException) {
                        sqlException.printStackTrace();
                        databaseLog("Failed to remove the mines list!", Level.WARNING);
                    }
                });

                bukkitRunnableWrapper.runTaskAsynchronously(plugin);
            }
        }
    }
}
