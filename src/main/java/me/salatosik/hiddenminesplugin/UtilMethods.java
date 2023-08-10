package me.salatosik.hiddenminesplugin;

import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.core.database.models.Mine;
import me.salatosik.hiddenminesplugin.core.database.models.UnknownMine;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Logger;

public class UtilMethods {
    public static Mine findMineByUnknownMine(@NotNull Collection<Mine> mines, @NotNull UnknownMine unknownMine) {
        for(Mine mine: mines) {
            if(unknownMine.equals(mine)) {
                return mine;
            }
        }
        return null;
    }

    private static void createThreadAndStart(Runnable runnable) {
        Thread databaseThread = new Thread(runnable);
        databaseThread.start();
    }

    public static void addMineToDatabase(Mine mine, Database database, Logger logger) {
        createThreadAndStart(() -> {
            try { database.addMine(mine); }
            catch(SQLException exception) {
                exception.printStackTrace();
                logger.warning("Failed add the mine to database!");
            }
        });
    }

    public static void removeMineFromDatabase(Mine mine, Database database, Logger logger) {
        try { database.removeMine(mine); }
        catch(SQLException sqlException) {
            sqlException.printStackTrace();
            logger.warning("Failed to remove mine from database. Mine: " + mine);
        }
    }
}
