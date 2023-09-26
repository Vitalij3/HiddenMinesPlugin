package me.salatosik.hiddenminesplugin.core.database;

import java.io.File;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.salatosik.hiddenminesplugin.core.data.MineData;
import me.salatosik.hiddenminesplugin.core.database.models.mine.Mine;
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
    private final HikariDataSource dataSource;
    private final JavaPlugin plugin;

    private final LinkedList<DatabaseListener<Mine>> databaseMineListeners = new LinkedList<>();

    public Database(@NotNull File databaseFile, @NotNull JavaPlugin plugin) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(JDBC_PREFIX.replace("{url}", databaseFile.getAbsolutePath()));
        hikariConfig.setAutoCommit(true);
        hikariConfig.setMaximumPoolSize(10);

        dataSource = new HikariDataSource(hikariConfig);

        try(Connection conn = dataSource.getConnection()) {
            try(Statement statement = conn.createStatement()) {
                statement.execute("create table if not exists mines " +
                        "(x int not null, " +
                        "y int not null, " +
                        "z int not null, " +
                        "mineType text not null," +
                        "worldType text not null)");
            } finally {
                conn.close();
            }
        }

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

    public void addMine(@NotNull Mine mine) throws SQLException {
        String sql = "insert into mines (x, y, z, mineType, worldType) values(?, ?, ?, ?, ?)";

        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, mine.getX());
                preparedStatement.setInt(2, mine.getY());
                preparedStatement.setInt(3, mine.getZ());
                preparedStatement.setString(4, mine.getMineType().name());
                preparedStatement.setString(5, mine.getWorldType().name());
                preparedStatement.executeUpdate();

                notifyListeners((listener) -> listener.onItemAdd(mine), databaseMineListeners);
            } finally {
                connection.close();
            }
        }
    }

    public void removeMine(@NotNull Mine mine) throws SQLException {
        String sql = "delete from mines where x = ? and y = ? and z = ? and mineType = ? and worldType = ?";

        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, mine.getX());
                preparedStatement.setInt(2, mine.getY());
                preparedStatement.setInt(3, mine.getZ());
                preparedStatement.setString(4, mine.getMineType().name());
                preparedStatement.setString(5, mine.getWorldType().name());
                preparedStatement.executeUpdate();

                notifyListeners((listener) -> listener.onItemRemove(mine), databaseMineListeners);

            } finally {
                connection.close();
            }
        }
    }

    public void removeMines(@NotNull List<Mine> mines, @Nullable Consumer<Mine> onEach) throws SQLException {
        String sql = "delete from mines where x = ? and y = ? and z = ? and mineType = ? and worldType = ?";

        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                for(Mine mine: mines) {
                    preparedStatement.setInt(1, mine.getX());
                    preparedStatement.setInt(2, mine.getY());
                    preparedStatement.setInt(3, mine.getZ());
                    preparedStatement.setString(4, mine.getMineType().name());
                    preparedStatement.setString(5, mine.getWorldType().name());
                    preparedStatement.executeUpdate();

                    if(onEach != null) onEach.accept(mine);
                }

            } finally {
                notifyListeners((listener) -> listener.onItemRemoveList(mines), databaseMineListeners);
                connection.close();
            }
        }
    }

    public void removeMines(@NotNull List<Mine> mines) throws SQLException {
        removeMines(mines, null);
    }

    public List<Mine> getAllMines() throws SQLException {
        String sql = "select * from mines";

        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try(ResultSet rs = preparedStatement.executeQuery()) {
                    LinkedList<Mine> linkedList = new LinkedList<>();

                    while(rs.next()) {
                        Mine mine = new Mine(
                                rs.getInt("x"),
                                rs.getInt("y"),
                                rs.getInt("z"),
                                MineData.valueOf(rs.getString("mineType")),
                                World.Environment.valueOf(rs.getString("worldType"))
                        );

                        linkedList.add(mine);
                    }

                    return linkedList;
                }
            } finally {
                connection.close();
            }
        }
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