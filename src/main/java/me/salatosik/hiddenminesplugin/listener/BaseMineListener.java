package me.salatosik.hiddenminesplugin.listener;

import me.salatosik.hiddenminesplugin.UtilMethods;
import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.core.database.interfaces.DatabaseListener;
import me.salatosik.hiddenminesplugin.core.database.models.Mine;
import me.salatosik.hiddenminesplugin.core.database.models.UnknownMine;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public abstract class BaseMineListener implements Listener, DatabaseListener {
    protected final JavaPlugin plugin;
    protected final Database database;
    protected final Logger logger;

    protected BaseMineListener(JavaPlugin plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
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

    protected LinkedList<Mine> minesFromDatabase;

    @Override
    public void onListenerAdded(List<Mine> mines) {
        minesFromDatabase = new LinkedList<>(mines);
    }

    @Override
    public void onMineAdd(Mine mine) {
        minesFromDatabase.add(mine);
    }

    @Override
    public void onMineRemove(Mine mine) {
        minesFromDatabase.remove(mine);
    }

    abstract String getChildClassName();

    // TODO Вивести це у файл конфігурації
    public final float EXPLOSION_POWER = 5.5f;

    // TODO вивести це у файл конфігурації
    public final List<Material> SHOVELS = List.of(
            Material.WOODEN_SHOVEL,
            Material.STONE_SHOVEL,
            Material.IRON_SHOVEL,
            Material.GOLDEN_SHOVEL,
            Material.DIAMOND_SHOVEL,
            Material.NETHERITE_SHOVEL
    );

    // TODO вивести це у файл конфігурації
    public final List<Material> ALLOWED_GROUND = List.of(
            Material.DIRT,
            Material.COARSE_DIRT,
            Material.GRASS,
            Material.GRASS_BLOCK
    );

    public boolean itIsMine(Block block) {
        Material blockType = block.getType();
        return blockType.equals(Material.TNT) || blockType.equals(Material.TRIPWIRE_HOOK);
    }

    public boolean itIsPossibleMine(Block block) {
        Material blockType = block.getType();
        return ALLOWED_GROUND.contains(blockType) || blockType == Material.TRIPWIRE_HOOK;
    }

    public boolean itIsPossibleGroundMine(Block block) {
        Material material = block.getType();
        return ALLOWED_GROUND.contains(material);
    }

    public boolean itIsPossibleHookMine(Block block) {
        Material material = block.getType();
        return material == Material.TRIPWIRE_HOOK;
    }

    public void detonateMine(Location blockLocation) {
        UnknownMine unknownMine = new UnknownMine(blockLocation.getBlockX(), blockLocation.getBlockY(), blockLocation.getBlockZ());
        Mine mine = UtilMethods.findMineByUnknownMine(minesFromDatabase, unknownMine);

        if(mine != null) {
            UtilMethods.removeMineFromDatabase(mine, database, logger);
            blockLocation.getWorld().createExplosion(blockLocation, EXPLOSION_POWER);
        }
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
