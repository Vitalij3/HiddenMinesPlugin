package me.salatosik.hiddenminesplugin.listener;

import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.core.database.models.Mine;
import me.salatosik.hiddenminesplugin.core.database.models.UnknownMine;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class MineInteractionMineListener extends BaseMineListener {
    public MineInteractionMineListener(JavaPlugin plugin, Database database) {
        super(plugin, database);
    }

    @Override
    String getChildClassName() {
        return this.getClass().getName();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location moveBlockLocation = event.getTo().getBlock().getLocation();
        moveBlockLocation.setY(moveBlockLocation.getY() - 1);

        World moveBlockWorld = moveBlockLocation.getWorld();
        Block moveBlockBottom = moveBlockWorld.getBlockAt(moveBlockLocation);

        if(itIsPossibleGroundMine(moveBlockBottom)) {
            Location possibleMineLocation = moveBlockBottom.getLocation();
            UnknownMine unknownMine = new UnknownMine(possibleMineLocation.getBlockX(), possibleMineLocation.getBlockY(), possibleMineLocation.getBlockZ());

            minesFromDatabase.forEach((mineFromDatabase) -> {
                if(mineFromDatabase.equals(unknownMine)) {
                    detonateMine(possibleMineLocation);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockRedstone(BlockRedstoneEvent event) {
        if(itIsPossibleHookMine(event.getBlock())) {
            Location possibleHookMineLocation = event.getBlock().getLocation();
            UnknownMine possibleMine = new UnknownMine(possibleHookMineLocation.getBlockX(), possibleHookMineLocation.getBlockY(), possibleHookMineLocation.getBlockZ());

            for(Mine mineFromDatabase: minesFromDatabase) {
                if(mineFromDatabase.equals(possibleMine)) {
                    detonateMine(possibleHookMineLocation);
                }
            }
        }
    }
}
