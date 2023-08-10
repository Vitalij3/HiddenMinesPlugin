package me.salatosik.hiddenminesplugin.core.database;

import java.io.File;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.salatosik.hiddenminesplugin.core.database.interfaces.DatabaseListener;
import me.salatosik.hiddenminesplugin.core.database.models.Mine;
import me.salatosik.hiddenminesplugin.core.database.models.MineType;
import me.salatosik.hiddenminesplugin.core.database.models.UnknownMine;
import org.jetbrains.annotations.NotNull;

public class Database {
    private static final String JDBC_PREFIX = "jdbc:sqlite:{url}";
    private final HikariDataSource dataSource;
    private final LinkedList<DatabaseListener> databaseListeners = new LinkedList<>();

    public Database(@NotNull File databaseFile) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(JDBC_PREFIX.replace("{url}", databaseFile.getAbsolutePath()));
        hikariConfig.setAutoCommit(true);
        hikariConfig.setMaximumPoolSize(5);

        dataSource = new HikariDataSource(hikariConfig);

        try(Connection conn = dataSource.getConnection()) {
            try(Statement statement = conn.createStatement()) {
                String sql = "create table if not exists mines " +
                        "(x float not null, " +
                        "y float not null, " +
                        "z float not null, " +
                        "mineType text not null)";

                statement.execute(sql);
            } finally {
                conn.close();
            }
        }
    }

    public void addMine(@NotNull Mine mine) throws SQLException {
        String sql = "insert into mines (x, y, z, mineType) values(?, ?, ?, ?)";

        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setFloat(1, mine.x);
                preparedStatement.setFloat(2, mine.y);
                preparedStatement.setFloat(3, mine.z);
                preparedStatement.setString(4, mine.mineType.name());
                preparedStatement.execute();

                databaseListeners.forEach((listener) -> { listener.onMineAdd(mine); });
            } finally {
                connection.close();
            }
        }
    }

    public void removeMine(@NotNull Mine mine) throws SQLException {
        String sql = "delete from mines where x = ? and y = ? and z = ? and mineType = ?";

        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setFloat(1, mine.x);
                preparedStatement.setFloat(2, mine.y);
                preparedStatement.setFloat(3, mine.z);
                preparedStatement.setString(4, mine.mineType.name());
                preparedStatement.execute();

                databaseListeners.forEach((listener) -> { listener.onMineRemove(mine); });

            } finally {
                connection.close();
            }
        }
    }

    public Mine findMine(UnknownMine unknownMine) throws SQLException {
        String sql = "select * from mines where x = ? and y = ? and z = ?";

        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setFloat(1, unknownMine.x);
                preparedStatement.setFloat(2, unknownMine.y);
                preparedStatement.setFloat(3, unknownMine.z);

                try(ResultSet resultSet = preparedStatement.executeQuery()) {
                    if(resultSet.next()) {
                        float x = resultSet.getFloat("x");
                        float y = resultSet.getFloat("y");
                        float z = resultSet.getFloat("z");
                        MineType mineType = MineType.valueOf(resultSet.getString("mineType"));

                        return new Mine(x, y, z, mineType);

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
                        Mine mine = new Mine(rs.getFloat("x"), rs.getFloat("y"),
                                rs.getFloat("z"), MineType.valueOf(rs.getString("mineType")));

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
}
