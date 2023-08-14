package me.salatosik.hiddenminesplugin.core.database;

import java.io.File;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.salatosik.hiddenminesplugin.core.database.interfaces.DatabaseListener;
import me.salatosik.hiddenminesplugin.core.database.models.Mine;
import me.salatosik.hiddenminesplugin.core.database.models.MineType;
import me.salatosik.hiddenminesplugin.core.database.models.UnknownMine;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class Database {
    private static final String JDBC_PREFIX = "jdbc:sqlite:{url}";
    private final HikariDataSource dataSource;
    private final LinkedList<DatabaseListener> databaseListeners = new LinkedList<>();

    public Database(@NotNull File databaseFile, @NotNull JavaPlugin plugin) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(JDBC_PREFIX.replace("{url}", databaseFile.getAbsolutePath()));
        hikariConfig.setAutoCommit(true);
        hikariConfig.setMaximumPoolSize(10);

        dataSource = new HikariDataSource(hikariConfig);

        try(Connection conn = dataSource.getConnection()) {
            try(Statement statement = conn.createStatement()) {
                String sql = "create table if not exists mines " +
                        "(x float not null, " +
                        "y float not null, " +
                        "z float not null, " +
                        "mineType text not null," +
                        "worldType text not null)";

                statement.execute(sql);
            } finally {
                conn.close();
            }
        }

        Timer timer = new Timer(DatabaseCleaner.TIMER_NAME);
        DatabaseCleaner databaseCleaner = new DatabaseCleaner(plugin);
        timer.scheduleAtFixedRate(databaseCleaner, 1000, DatabaseCleaner.UPDATE_RATE);
    }

    public void addMine(@NotNull Mine mine) throws SQLException {
        String sql = "insert into mines (x, y, z, mineType, worldType) values(?, ?, ?, ?, ?)";

        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setFloat(1, mine.x);
                preparedStatement.setFloat(2, mine.y);
                preparedStatement.setFloat(3, mine.z);
                preparedStatement.setString(4, mine.mineType.name());
                preparedStatement.setString(5, mine.worldType.name());
                preparedStatement.execute();

                databaseListeners.forEach((listener) -> { listener.onMineAdd(mine); });
            } finally {
                connection.close();
            }
        }
    }

    public void removeMine(@NotNull Mine mine) throws SQLException {
        String sql = "delete from mines where x = ? and y = ? and z = ? and mineType = ? and worldType = ?";

        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setFloat(1, mine.x);
                preparedStatement.setFloat(2, mine.y);
                preparedStatement.setFloat(3, mine.z);
                preparedStatement.setString(4, mine.mineType.name());
                preparedStatement.setString(5, mine.worldType.name());
                preparedStatement.execute();

                databaseListeners.forEach((listener) -> { listener.onMineRemove(mine); });

            } finally {
                connection.close();
            }
        }
    }

    public Mine findMine(UnknownMine unknownMine) throws SQLException {
        String sql = "select * from mines where x = ? and y = ? and z = ? and worldType = ?";

        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setFloat(1, unknownMine.x);
                preparedStatement.setFloat(2, unknownMine.y);
                preparedStatement.setFloat(3, unknownMine.z);
                preparedStatement.setString(4, unknownMine.worldType.name());

                try(ResultSet resultSet = preparedStatement.executeQuery()) {
                    if(resultSet.next()) {
                        float x = resultSet.getFloat("x");
                        float y = resultSet.getFloat("y");
                        float z = resultSet.getFloat("z");
                        MineType mineType = MineType.valueOf(resultSet.getString("mineType"));
                        World.Environment worldType = World.Environment.valueOf(resultSet.getString("worldType"));

                        return new Mine(x, y, z, mineType, worldType);

                    } else return null;
                }
            } finally {
                connection.close();
            }
        }
    }

    public List<Mine> getAllMines() throws SQLException {
        String sql = "select * from mines";

        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                try(ResultSet rs = preparedStatement.executeQuery()) {
                    LinkedList<Mine> linkedList = new LinkedList<>();

                    while(rs.next()) {
                        Mine mine = new Mine(
                                rs.getFloat("x"),
                                rs.getFloat("y"),
                                rs.getFloat("z"),
                                MineType.valueOf(rs.getString("mineType")),
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

    public void subscribeListener(DatabaseListener databaseListener) throws SQLException {
        databaseListener.onListenerAdded(getAllMines());
        databaseListeners.add(databaseListener);
    }

    private class DatabaseCleaner extends TimerTask {
        private final JavaPlugin plugin;
        private final Logger logger;

        public static final String LOG_PREFIX = "- [Database Cleaner]: ";
        public static final String TIMER_NAME = "Database cleaner";
        public static final long UPDATE_RATE = 1000L;

        public DatabaseCleaner(JavaPlugin plugin) {
            this.plugin = plugin;
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

            for(World world: plugin.getServer().getWorlds()) {
                for(Mine mine: mines) {
                    if(mine.worldType != world.getEnvironment()) continue;

                    Block block = world.getBlockAt((int) mine.x, (int) mine.y, (int) mine.z);
                    if(block.getType() == Material.AIR || block.getType() == Material.CAVE_AIR || block.getType() == Material.VOID_AIR) {
                        Thread thread = new Thread(() -> {
                            try { removeMine(mine); }
                            catch(SQLException sqlException) {
                                databaseLog("Failed to remove excess block!", Level.WARNING);
                            }
                        });

                        thread.start();
                    }
                }
            }
        }
    }
}
