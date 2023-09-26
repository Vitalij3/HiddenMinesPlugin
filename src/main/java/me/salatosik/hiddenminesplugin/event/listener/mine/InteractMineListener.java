package me.salatosik.hiddenminesplugin.event.listener.mine;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import me.salatosik.hiddenminesplugin.UtilMethods;
import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.core.database.models.mine.Mine;
import me.salatosik.hiddenminesplugin.core.database.models.mine.UnknownMine;
import me.salatosik.hiddenminesplugin.utils.configuration.Configuration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class InteractMineListener extends BaseMineListener {
    public InteractMineListener(JavaPlugin plugin, Database database, Configuration configuration) {
        super(plugin, database, configuration);

        EntityTimer entityTimer = new EntityTimer();
        entityTimer.runTaskTimerAsynchronously(plugin, 20, EntityTimer.UPDATE_RATE_TICK);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockRedstone(BlockRedstoneEvent event) {
        if(itIsPossibleHookMine(event.getBlock())) {
            Location possibleHookMineLocation = event.getBlock().getLocation();
            UnknownMine possibleMine = new UnknownMine(possibleHookMineLocation);

            for(Mine mine: minesFromDatabase) {
                if(mine.equals(possibleMine)) {
                    detonateMineAndRemoveFromDatabase(possibleHookMineLocation);
                    return;
                }
            }
        }
    }

    private class EntityTimer extends BukkitRunnable {
        private final AtomicInteger worldThreadActive = new AtomicInteger(0);
        public static final int UPDATE_RATE_TICK = 2;

        @Override
        public void run() {
            if(worldThreadActive.get() >= 1) return;

            plugin.getServer().getWorlds().forEach((world) -> {
                UtilMethods.createBukkitAsyncThreadAndStart(plugin, () -> {
                    synchronized(worldThreadActive) { worldThreadActive.set(worldThreadActive.incrementAndGet()); }

                    List<Mine> filteredMinesFromDatabase = minesFromDatabase.stream().filter((mine) -> mine.getWorldType() == world.getEnvironment()).collect(Collectors.toList());

                    world.getEntities().forEach((entity) -> {
                        if(!entity.isOnGround()) return;

                        Location possibleMineLocation = entity.getBoundingBox().getMin().toLocation(world);
                        possibleMineLocation.setY(possibleMineLocation.getBlockY() - 1);

                        if(itIsPossibleGroundMine(possibleMineLocation.getBlock())) {
                            UnknownMine unknownMine = new UnknownMine(possibleMineLocation);

                            for(Mine mineFromDatabase: filteredMinesFromDatabase) {
                                if(mineFromDatabase.equals(unknownMine)) {
                                    detonateMineAndRemoveFromDatabase(possibleMineLocation);
                                    break;
                                }
                            }
                        }
                    });

                    synchronized(worldThreadActive) { worldThreadActive.set(worldThreadActive.decrementAndGet()); }
                });
            });
        }

    }

    private void detonateMineOnPistonEvent(BlockPistonEvent event) {
        List<Block> pistonBlocks = null;

        if(event instanceof BlockPistonExtendEvent) {
            BlockPistonExtendEvent extendEvent = (BlockPistonExtendEvent) event;
            pistonBlocks = extendEvent.getBlocks();

        } else if(event instanceof BlockPistonRetractEvent) {
            BlockPistonRetractEvent retractEvent = (BlockPistonRetractEvent) event;
            pistonBlocks = retractEvent.getBlocks();
        }

        if(pistonBlocks != null) for(Block block: pistonBlocks) if(itIsMine(block)) detonateMineAndRemoveFromDatabase(block.getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPistonIn(BlockPistonExtendEvent event) {
        detonateMineOnPistonEvent(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPistonOut(BlockPistonRetractEvent event) {
        detonateMineOnPistonEvent(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if(ALLOWED_MINE_GROUNDS.contains(event.getBlock().getType())) {
            detonateMineAndRemoveFromDatabase(event.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockDestroy(BlockDestroyEvent event) {
        if(itIsPossibleMine(event.getBlock())) {
            detonateMineAndRemoveFromDatabase(event.getBlock().getLocation(), false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockDamage(BlockDamageEvent event) {
        if(itIsPossibleMine(event.getBlock())) {
            if(!SHOVELS.contains(event.getItemInHand().getType())) {
                detonateMineAndRemoveFromDatabase(event.getBlock().getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockFertilize(BlockFertilizeEvent event) {
        if(itIsPossibleMine(event.getBlock())) {
            detonateMineAndRemoveFromDatabase(event.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if(itIsPossibleMine(event.getBlock())) {
            detonateMineAndRemoveFromDatabase(event.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockIgniteByPlayer(BlockPlaceEvent event) {
        if(event.getBlockPlaced().getType() == Material.FIRE) {
            Location placedBlockLocation = event.getBlockPlaced().getLocation();
            placedBlockLocation.setY(placedBlockLocation.getY() - 1);
            Block bottomPlacedBlock = placedBlockLocation.getBlock();

            if(itIsPossibleMine(bottomPlacedBlock)) detonateMineAndRemoveFromDatabase(bottomPlacedBlock.getLocation());
        }
    }
}
