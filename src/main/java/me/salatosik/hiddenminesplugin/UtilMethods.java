package me.salatosik.hiddenminesplugin;

import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.core.database.models.Mine;
import me.salatosik.hiddenminesplugin.core.database.models.UnknownMine;
import me.salatosik.hiddenminesplugin.utils.BukkitRunnableWrapper;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
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

    public static Mine findMineByLocation(@NotNull Collection<Mine> mines, @NotNull Location location) {
        UnknownMine unknownMine = new UnknownMine(location);
        return findMineByUnknownMine(mines, unknownMine);
    }

    public static void addMineToDatabase(Mine mine, Database database, Logger logger, Consumer<SQLException> onException, Consumer<Object> onComplete) {
        try {
            database.addMine(mine);
            if(onComplete != null) onComplete.accept(new Object());
        } catch(SQLException exception) {
            exception.printStackTrace();
            if(onException != null) onException.accept(exception);
            logger.warning("Failed add the mine to database!");
        }
    }

    public static void addMineToDatabase(Mine mine, Database database, Logger logger, Consumer<Object> onComplete) {
        addMineToDatabase(mine, database, logger, null, onComplete);
    }

    public static void removeMineFromDatabase(Mine mine, Database database, Logger logger, Consumer<SQLException> onException, Consumer<Object> onComplete) {
        try {
            database.removeMine(mine);
            if(onComplete != null) onComplete.accept(new Object());
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
            if(onException != null) onException.accept(sqlException);
            logger.warning("Failed to remove mine from database. Mine: " + mine);
        }
    }

    public static void removeMineFromDatabase(Mine mine, Database database, Logger logger) {
        removeMineFromDatabase(mine, database, logger, null, null);
    }

    public static void removeMineFromDatabase(Mine mine, Database database, Logger logger, Consumer<Object> onComplete) {
        removeMineFromDatabase(mine, database, logger, null, onComplete);
    }

    public static void removeMinesFromDatabase(List<Mine> mines, Database database, Logger logger, Consumer<SQLException> onException, Consumer<Object> onComplete, Consumer<Mine> onEach) {
        try {
            database.removeMines(mines, onEach);
            if(onComplete != null) onComplete.accept(new Object());
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
            if(onException != null) onException.accept(sqlException);
            logger.warning("An array was deleted from the database.");
        }
    }

    public static void removeMinesFromDatabase(List<Mine> mines, Database database, Logger logger, Consumer<Mine> onEach) {
        removeMinesFromDatabase(mines, database, logger, null, null, onEach);
    }

    public static void removeMinesFromDatabase(List<Mine> mines, Database database, Logger logger) {
        removeMinesFromDatabase(mines, database, logger, null);
    }

    public static void createBukkitThreadAndStart(JavaPlugin javaPlugin, Runnable runnable) {
        BukkitRunnableWrapper bukkitRunnableWrapper = new BukkitRunnableWrapper(runnable);
        bukkitRunnableWrapper.runTask(javaPlugin);
    }

    public static void createBukkitAsyncThreadAndStart(JavaPlugin javaPlugin, Runnable runnable) {
        BukkitRunnableWrapper bukkitRunnableWrapper = new BukkitRunnableWrapper(runnable);
        bukkitRunnableWrapper.runTaskAsynchronously(javaPlugin);
    }
}
