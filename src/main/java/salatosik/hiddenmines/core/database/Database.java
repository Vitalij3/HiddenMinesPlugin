package salatosik.hiddenmines.core.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.TableUtils;
import lombok.SneakyThrows;
import salatosik.hiddenmines.core.database.model.MineOfDatabase;
import salatosik.hiddenmines.core.mine.Mine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private final JdbcPooledConnectionSource jdbcPooledConnectionSource;
    private final Dao<MineOfDatabase, Long> mineDao;

    @SneakyThrows
    public Database(File databaseFile) {
        Class.forName("org.sqlite.JDBC");
        jdbcPooledConnectionSource = new JdbcPooledConnectionSource("jdbc:sqlite:%s".formatted(databaseFile.getAbsolutePath()));
        TableUtils.createTableIfNotExists(jdbcPooledConnectionSource, MineOfDatabase.class);

        mineDao = DaoManager.createDao(jdbcPooledConnectionSource, MineOfDatabase.class);
        previousSavedMines.addAll(mineDao.queryForAll().stream().map(MineOfDatabase::toMine).toList());

        TableUtils.clearTable(jdbcPooledConnectionSource, MineOfDatabase.class);
    }

    @SneakyThrows
    private static File createDatabaseFileIfNotExists(String databaseFilename) {
        File file = new File(databaseFilename);

        if(!file.exists()) {
            if(!file.createNewFile()) {
                throw new IOException("Failed to create the database file.");
            }
        }

        return file;
    }

    public Database(String databaseFilename) {
        this(createDatabaseFileIfNotExists(databaseFilename));
    }

    private final List<Mine> previousSavedMines = new ArrayList<>();

    public List<Mine> getPreviousSavedMines() {
        return previousSavedMines;
    }

    @SneakyThrows
    public void saveAll(List<Mine> mines) {
        if(connectionClosed) {
            throw new RuntimeException("Failed to save mines because database connection is closed!");
        }

        TableUtils.clearTable(jdbcPooledConnectionSource, MineOfDatabase.class);

        mineDao.create(mines.stream().map(Mine::toMineOfDatabase).toList());

        previousSavedMines.clear();
        previousSavedMines.addAll(mines);
    }

    private boolean connectionClosed = false;

    @SneakyThrows
    public void closeConnection() {
        jdbcPooledConnectionSource.close();
        connectionClosed = true;
    }
}
