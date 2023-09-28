package me.salatosik.hiddenminesplugin.event.listener.mine;

import me.salatosik.hiddenminesplugin.UtilMethods;
import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.core.database.DatabaseListener;
import me.salatosik.hiddenminesplugin.core.database.models.mine.Mine;
import me.salatosik.hiddenminesplugin.core.database.models.mine.UnknownMine;
import me.salatosik.hiddenminesplugin.event.listener.BaseListener;
import me.salatosik.hiddenminesplugin.utils.configuration.Configuration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

public class BaseMineListener extends BaseListener implements DatabaseListener<Mine> {
    protected BaseMineListener(JavaPlugin plugin, Database database, Configuration configuration) {
        super(plugin, database, plugin.getLogger(), configuration);

        try {
            database.subscribeMineListener(this);
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
            plugin.getPluginLoader().disablePlugin(plugin);
        }
    }

    public static final List<Material> ALLOWED_MINE_GROUNDS = List.of(
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

    protected LinkedBlockingDeque<Mine> minesFromDatabase;

    @Override
    public void onListenerAdded(List<Mine> mines) {
        minesFromDatabase = new LinkedBlockingDeque<>(mines);
    }

    @Override
    public void onItemAdd(Mine mine) {
        minesFromDatabase.add(mine);
    }

    @Override
    public void onItemRemove(Mine mine) {
        if(!minesFromDatabase.isEmpty()) {
            minesFromDatabase.remove(mine);
        }
    }

    @Override
    public void onItemRemoveList(List<Mine> removedMines) {
        minesFromDatabase.removeAll(removedMines);
    }

    protected boolean itIsPossibleMine(Block block) {
        Material blockType = block.getType();
        return ALLOWED_MINE_GROUNDS.contains(blockType) || blockType == Material.TRIPWIRE_HOOK;
    }

    protected boolean itIsPossibleGroundMine(Block block) {
        Material material = block.getType();
        return ALLOWED_MINE_GROUNDS.contains(material);
    }

    protected boolean itIsPossibleHookMine(Block block) {
        Material material = block.getType();
        return material == Material.TRIPWIRE_HOOK;
    }

    protected boolean itIsMine(UnknownMine unknownMine) {
        for(Mine mine: minesFromDatabase) if(mine.equals(unknownMine)) return true;
        return false;
    }

    protected boolean itIsMine(Block block) {
        UnknownMine unknownMine = new UnknownMine(block);
        return itIsMine(unknownMine);
    }

    protected void detonateMine(Mine mine) {
        Location mineLocation = mine.toLocation(plugin);
        if(mineLocation == null) return;

        switch(mine.getMineType()) {
            case GROUND:
                mineLocation.setY(mineLocation.getBlockY() + 1);
                mineLocation.getWorld().createExplosion(
                        mineLocation,
                        (float) configuration.getMineConfiguration().getGround().getExplosionPower(),
                        configuration.getMineConfiguration().getGround().getFireBlocks(),
                        configuration.getMineConfiguration().getGround().getBreakBlocks()
                );
                break;

            case HOOK:
                mineLocation.getWorld().createExplosion(
                        mineLocation,
                        (float) configuration.getMineConfiguration().getHook().getExplosionPower(),
                        configuration.getMineConfiguration().getHook().getFireBlocks(),
                        configuration.getMineConfiguration().getHook().getBreakBlocks()
                );
                break;
        }
    }

    protected void detonateMineAndRemoveFromDatabase(Location blockLocation, boolean withoutDetonation) {
        Mine mine = UtilMethods.findMineByLocation(minesFromDatabase, blockLocation);
        if(mine == null) return;

        UtilMethods.createBukkitAsyncThreadAndStart(plugin,
                () -> UtilMethods.removeMineFromDatabase(mine, database,
                        (v) -> UtilMethods.createBukkitThreadAndStart(plugin, () -> {
                            if(!withoutDetonation) detonateMine(mine);
        })));
    }

    protected void detonateMineAndRemoveFromDatabase(Location blockLocation) {
        detonateMineAndRemoveFromDatabase(blockLocation, false);
    }
}