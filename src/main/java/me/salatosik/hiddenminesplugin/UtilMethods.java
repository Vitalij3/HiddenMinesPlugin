package me.salatosik.hiddenminesplugin;

import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.core.database.models.Mine;
import me.salatosik.hiddenminesplugin.core.database.models.MineType;
import me.salatosik.hiddenminesplugin.core.database.models.UnknownMine;
import me.salatosik.hiddenminesplugin.utils.BukkitRunnableWrapper;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
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

    public static Mine findMineByLocation(@NotNull Collection<Mine> mines, @NotNull Location location) {
        UnknownMine unknownMine = getUnknownMineByBlock(location);
        return findMineByUnknownMine(mines, unknownMine);
    }

    public static void addMineToDatabase(Mine mine, Database database, Logger logger) {
        try { database.addMine(mine); }
        catch(SQLException exception) {
            exception.printStackTrace();
            logger.warning("Failed add the mine to database!");
        }
    }

    public static void removeMineFromDatabase(Mine mine, Database database, Logger logger) {
        try { database.removeMine(mine); }
        catch(SQLException sqlException) {
            sqlException.printStackTrace();
            logger.warning("Failed to remove mine from database. Mine: " + mine);
        }
    }

    public static UnknownMine getUnknownMineByBlock(Block block) {
        return new UnknownMine(
                block.getX(),
                block.getY(),
                block.getZ(),
                block.getWorld().getEnvironment()
        );
    }

    public static UnknownMine getUnknownMineByBlock(Location blockLocation) {
        return new UnknownMine(
                (float) blockLocation.getX(),
                (float) blockLocation.getY(),
                (float) blockLocation.getZ(),
                blockLocation.getWorld().getEnvironment()
        );
    }

    public static Mine getMineByBlock(Block block, MineType mineType) {
        return new Mine(
                block.getX(),
                block.getY(),
                block.getZ(),
                mineType,
                block.getWorld().getEnvironment()
        );
    }

    public static Mine getMineByBlock(Location blockLocation, MineType mineType) {
        return new Mine(
                (float) blockLocation.getBlockX(),
                (float) blockLocation.getBlockY(),
                (float) blockLocation.getBlockZ(),
                mineType,
                blockLocation.getWorld().getEnvironment()
        );
    }

    public static void createBukkitThreadAndStart(JavaPlugin javaPlugin, Runnable runnable) {
        BukkitRunnableWrapper bukkitRunnableWrapper = new BukkitRunnableWrapper(runnable);
        bukkitRunnableWrapper.runTask(javaPlugin);
    }
}
