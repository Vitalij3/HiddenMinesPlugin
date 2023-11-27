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
    private static void createDatabaseFileIfNotExists(String databaseFilename) {
        File file = new File(databaseFilename);

        if(!file.exists()) {
            if(!file.createNewFile()) {
                throw new IOException("Failed to create the database file.");
            }
        }
    }

    @SneakyThrows
    public Database(File databaseFile) {
        createDatabaseFileIfNotExists(databaseFile.getAbsolutePath());

        Class.forName("org.sqlite.JDBC");
        jdbcPooledConnectionSource = new JdbcPooledConnectionSource("jdbc:sqlite:%s".formatted(databaseFile.getAbsolutePath()));
        TableUtils.createTableIfNotExists(jdbcPooledConnectionSource, MineOfDatabase.class);

        mineDao = DaoManager.createDao(jdbcPooledConnectionSource, MineOfDatabase.class);
        oldSavedMines.addAll(mineDao.queryForAll().stream().map(MineOfDatabase::toMine).toList());

        TableUtils.clearTable(jdbcPooledConnectionSource, MineOfDatabase.class);
    }

    public Database(String databaseFilename) {
        this(new File(databaseFilename));
    }

    private final List<Mine> oldSavedMines = new ArrayList<>();

    public List<Mine> getOldSavedMines() {
        return new ArrayList<>(oldSavedMines);
    }

    @SneakyThrows
    public void saveAll(List<Mine> mines) {
        if(connectionClosed) {
            throw new RuntimeException("Failed to save mines because database connection is closed!");
        }

        TableUtils.clearTable(jdbcPooledConnectionSource, MineOfDatabase.class);
        mineDao.create(mines.stream().map(Mine::toMineOfDatabase).toList());

        oldSavedMines.clear();
        oldSavedMines.addAll(mines);
    }

    private boolean connectionClosed = false;

    @SneakyThrows
    public void closeConnection() {
        jdbcPooledConnectionSource.close();
        connectionClosed = true;
    }
}
