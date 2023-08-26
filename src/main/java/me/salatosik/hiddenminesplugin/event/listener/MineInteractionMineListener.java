package me.salatosik.hiddenminesplugin.event.listener;

import me.salatosik.hiddenminesplugin.UtilMethods;
import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.core.database.models.Mine;
import me.salatosik.hiddenminesplugin.core.database.models.UnknownMine;
import me.salatosik.hiddenminesplugin.utils.configuration.Configuration;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MineInteractionMineListener extends BaseMineListener {
    public MineInteractionMineListener(JavaPlugin plugin, Database database, Configuration configuration) {
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

                    List<Mine> filteredMinesFromDatabase = minesFromDatabase.stream().filter((mine) -> mine.worldType == world.getEnvironment()).collect(Collectors.toList());

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
}
