package me.salatosik.hiddenminesplugin.event.listener;

import me.salatosik.hiddenminesplugin.UtilMethods;
import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.core.database.models.UnknownMine;
import me.salatosik.hiddenminesplugin.utils.configuration.Configuration;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MineInteractionMineListener extends BaseMineListener {
    public MineInteractionMineListener(JavaPlugin plugin, Database database, Configuration configuration) {
        super(plugin, database, configuration);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location moveBlockLocation = event.getTo().getBlock().getLocation();
        moveBlockLocation.setY((int) moveBlockLocation.getY() - 1);

        World moveBlockWorld = moveBlockLocation.getWorld();
        Block moveBlockBottom = moveBlockWorld.getBlockAt(moveBlockLocation);

        if(itIsPossibleGroundMine(moveBlockBottom)) {
            Location possibleMineLocation = moveBlockBottom.getLocation();
            UnknownMine unknownMine = UtilMethods.getUnknownMineByBlock(possibleMineLocation);

            minesFromDatabase.forEach((mineFromDatabase) -> {
                if(mineFromDatabase.equals(unknownMine)) {
                    detonateMineAndRemoveFromDatabase(possibleMineLocation);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockRedstone(BlockRedstoneEvent event) {
        if(itIsPossibleHookMine(event.getBlock())) {
            Location possibleHookMineLocation = event.getBlock().getLocation();
            UnknownMine possibleMine = UtilMethods.getUnknownMineByBlock(possibleHookMineLocation);

            minesFromDatabase.forEach((minesFromDatabase) -> {
                if(minesFromDatabase.equals(possibleMine)) {
                    detonateMineAndRemoveFromDatabase(possibleHookMineLocation);
                }
            });
        }
    }
}
