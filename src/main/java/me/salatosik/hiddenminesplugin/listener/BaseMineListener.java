package me.salatosik.hiddenminesplugin.listener;

import me.salatosik.hiddenminesplugin.UtilMethods;
import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.core.database.interfaces.DatabaseListener;
import me.salatosik.hiddenminesplugin.core.database.models.Mine;
import me.salatosik.hiddenminesplugin.core.database.models.UnknownMine;
import me.salatosik.hiddenminesplugin.utils.configuration.Configuration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Logger;

public abstract class BaseMineListener implements DatabaseListener, Listener {
    protected final JavaPlugin plugin;
    protected final Database database;
    protected final Logger logger;
    protected final Configuration configuration;

    public static final List<Material> ALLOWED_GROUND_MINE_GROUNDS = List.of(
            Material.GRASS_BLOCK,
            Material.DIRT,
            Material.COARSE_DIRT,
            Material.PODZOL,
            Material.SAND,
            Material.GRAVEL,
            Material.RED_SAND,
            Material.SOUL_SAND,
            Material.SOUL_SOIL,
            Material.MYCELIUM,
            Material.GRASS_PATH
    );

    public static final List<Material> SHOVELS = List.of(
            Material.WOODEN_SHOVEL,
            Material.STONE_SHOVEL,
            Material.IRON_SHOVEL,
            Material.GOLDEN_SHOVEL,
            Material.DIAMOND_SHOVEL,
            Material.NETHERITE_SHOVEL
    );

    protected BaseMineListener(JavaPlugin plugin, Database database, Configuration configuration) {
        this.plugin = plugin;
        this.database = database;
        this.configuration = configuration;
        logger = plugin.getLogger();

        try {
            database.subscribeListener(this);
            logger.info("Database listener in " + getChildClassName() + " is initialized!");
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
            logger.warning("Database listener in " + getChildClassName() + " is not initialized!");
            plugin.getPluginLoader().disablePlugin(plugin);
        }
    }

    protected LinkedBlockingDeque<Mine> minesFromDatabase;

    @Override
    public void onListenerAdded(List<Mine> mines) {
        minesFromDatabase = new LinkedBlockingDeque<>(mines);
    }

    @Override
    public void onMineAdd(Mine mine) {
        minesFromDatabase.add(mine);
    }

    @Override
    public void onMineRemove(Mine mine) {
        if(!minesFromDatabase.isEmpty()) {
            minesFromDatabase.remove(mine);
        }
    }

    abstract String getChildClassName();

    public boolean itIsPossibleMine(Block block) {
        Material blockType = block.getType();
        return ALLOWED_GROUND_MINE_GROUNDS.contains(blockType) || blockType == Material.TRIPWIRE_HOOK;
    }

    public boolean itIsPossibleGroundMine(Block block) {
        Material material = block.getType();
        return ALLOWED_GROUND_MINE_GROUNDS.contains(material);
    }

    public boolean itIsPossibleHookMine(Block block) {
        Material material = block.getType();
        return material == Material.TRIPWIRE_HOOK;
    }

    public boolean itIsMine(UnknownMine unknownMine) {
        for(Mine mine: minesFromDatabase) if(mine.equals(unknownMine)) return true;
        return false;
    }

    public boolean itIsMine(Block block) {
        UnknownMine unknownMine = UtilMethods.getUnknownMineByBlock(block);
        return itIsMine(unknownMine);
    }

    private void detonateMine(Mine mine, World world) {
        Location location = new Location(world, mine.x, mine.y, mine.z);
        switch(mine.mineType) {
            case GROUND:
                world.createExplosion(
                        location,
                        (float) configuration.getMineConfiguration().getGround().getExplosionPower(),
                        configuration.getMineConfiguration().getGround().getFireBlocks(),
                        configuration.getMineConfiguration().getGround().getBreakBlocks()
                );
                break;

            case HOOK:
                world.createExplosion(
                        location,
                        (float) configuration.getMineConfiguration().getHook().getExplosionPower(),
                        configuration.getMineConfiguration().getHook().getFireBlocks(),
                        configuration.getMineConfiguration().getHook().getBreakBlocks()
                );
                break;
        }
    }

    public void detonateMineAndRemoveFromDatabase(Location blockLocation) {
        detonateMineAndRemoveFromDatabase(blockLocation, false);
    }

    public void detonateMineAndRemoveFromDatabase(Location blockLocation, boolean withoutDetonation) {
        Mine mine = UtilMethods.findMineByLocation(minesFromDatabase, blockLocation);
        if(mine == null) return;

        UtilMethods.createBukkitAsyncThreadAndStart(plugin,
                () -> UtilMethods.removeMineFromDatabase(mine, database, logger,
                        (v) -> UtilMethods.createBukkitThreadAndStart(plugin, () -> {
                            if(!withoutDetonation) detonateMine(mine, blockLocation.getWorld());
        })));
    }

    public void removeItemFromInventory(ItemStack itemStack, int amount, Inventory inventory) {
        int itemAmount = itemStack.getAmount();
        if(itemAmount == 1) inventory.remove(itemStack);
        else if(itemAmount > 1) {
            itemStack.setAmount(amount);
            inventory.remove(itemStack);
        }
    }
}
