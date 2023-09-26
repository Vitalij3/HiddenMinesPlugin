package me.salatosik.hiddenminesplugin;

import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.core.database.models.DatabaseObject;
import me.salatosik.hiddenminesplugin.core.database.models.mine.Mine;
import me.salatosik.hiddenminesplugin.core.database.models.mine.UnknownMine;
import me.salatosik.hiddenminesplugin.utils.BukkitRunnableWrapper;
import me.salatosik.hiddenminesplugin.utils.CommonFunctionThrowsException;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

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

    private static void templateDatabaseInteraction(Consumer<Object> onComplete, Consumer<Exception> onException, CommonFunctionThrowsException method) {
        try {
            method.invoke();
            if(onComplete != null) onComplete.accept(new Object());
        } catch(Exception exception) {
            exception.printStackTrace();
            if(onException != null) onException.accept(exception);
        }
    }

    // Updated every time a new table is added
    private static void addItemToDatabase(DatabaseObject item, Database database, Consumer<Exception> onException, Consumer<Object> onComplete) {
        templateDatabaseInteraction(onComplete, onException, () -> {
            if(item instanceof Mine) database.addMine((Mine) item);
        });
    }


    // Updated every time a new table is added
    private static void removeItemFromDatabase(DatabaseObject item, Database database, Consumer<Exception> onException, Consumer<Object> onComplete) {
        templateDatabaseInteraction(onComplete, onException, () -> {
            if(item instanceof Mine) database.removeMine((Mine) item);
        });
    }

    public static void addMineToDatabase(Mine mine, Database database, Consumer<Exception> onException, Consumer<Object> onComplete) {
        addItemToDatabase(mine, database, onException, onComplete);
    }

    public static void addMineToDatabase(Mine mine, Database database, Consumer<Object> onComplete) {
        addMineToDatabase(mine, database, null, onComplete);
    }

    public static void removeMineFromDatabase(Mine mine, Database database, Consumer<Exception> onException, Consumer<Object> onComplete) {
        removeItemFromDatabase(mine, database, onException, onComplete);
    }

    public static void removeMineFromDatabase(Mine mine, Database database) {
        removeMineFromDatabase(mine, database, null, null);
    }

    public static void removeMineFromDatabase(Mine mine, Database database, Consumer<Object> onComplete) {
        removeMineFromDatabase(mine, database, null, onComplete);
    }

    public static void removeMinesFromDatabase(List<Mine> mines, Database database, Consumer<Exception> onException, Consumer<Object> onComplete, Consumer<Mine> onEach) {
        templateDatabaseInteraction(onComplete, onException, () -> {
            database.removeMines(mines, onEach);
        });
    }

    public static void removeMinesFromDatabase(List<Mine> mines, Database database, Consumer<Mine> onEach) {
        removeMinesFromDatabase(mines, database, null, null, onEach);
    }

    public static void removeMinesFromDatabase(List<Mine> mines, Database database) {
        removeMinesFromDatabase(mines, database, null);
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
