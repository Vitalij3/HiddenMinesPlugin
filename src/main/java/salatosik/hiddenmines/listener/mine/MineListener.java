package salatosik.hiddenmines.listener.mine;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import org.bukkit.*;
import org.bukkit.block.data.type.TripwireHook;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import salatosik.hiddenmines.HiddenMines;
import salatosik.hiddenmines.configuration.PluginConfiguration;
import salatosik.hiddenmines.core.MineManager;
import salatosik.hiddenmines.core.mine.Mine;
import salatosik.hiddenmines.core.mine.MineType;

import java.util.HashMap;
import java.util.Map;


public class MineListener extends BaseMineListener {
    public MineListener(PluginConfiguration configuration, MineManager mineManager) {
        super(configuration, mineManager);
        HiddenMines.runTimerTask(new PlayerMoveTimer(), 5);
    }

    @EventHandler
    public void onGroundMinePlace(BlockPlaceEvent event) {
        MineType mineType = defineMineType(event.getItemInHand());

        if(mineType != MineType.GROUND) {
            return;
        }

        Location placedBlockLocation = event.getBlockPlaced().getLocation().clone();
        placedBlockLocation.add(0, -1, 0);

        if(!groundIsSuitable(placedBlockLocation.getBlock().getType())) {
            event.setCancelled(true);
            return;
        }

        if(mineManager.contains(placedBlockLocation)) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        decrementItemStack(event.getPlayer().getGameMode(), event.getItemInHand());
        mineManager.add(new Mine(placedBlockLocation, mineType));
    }

    @EventHandler
    public void onHookMinePlace(PlayerInteractEvent event) {
        if(!event.getAction().isRightClick() || event.getClickedBlock() == null || event.getItem() == null) {
            return;
        }

        MineType mineType = defineMineType(event.getItem());

        if(mineType != MineType.HOOK) {
            return;
        }

        if(event.getClickedBlock().getType() != Material.TRIPWIRE_HOOK) {
            event.setCancelled(true);
            return;
        }

        if(mineManager.contains(event.getClickedBlock().getLocation())) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        decrementItemStack(event.getPlayer().getGameMode(), event.getItem());
        mineManager.add(new Mine(event.getClickedBlock().getLocation(), mineType));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        mineManager.showMinesFor(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        mineManager.showMinesFor(event.getPlayer());
    }

    private class PlayerMoveTimer extends BukkitRunnable {
        private final Map<Player, Location> playerLocationMap = new HashMap<>();

        @Override
        public void run() {
            for(World world: Bukkit.getServer().getWorlds()) {
                for(Player player: world.getPlayers()) {
                    Location locationFromMap = playerLocationMap.get(player);

                    if(locationFromMap == null) {
                        playerLocationMap.put(player, player.getLocation().clone());
                    }

                    if(locationFromMap != null) {
                        if(locationFromMap.equals(player.getLocation())) {
                            return;
                        }

                        playerLocationMap.replace(player, player.getLocation());
                    }

                    onPlayer(player);
                }
            }
        }

        private void onPlayer(Player player) {
            Location location = player.getLocation().clone();

            if(location.getY() - (int) location.getY() > 0.2) {
                return;
            }

            location.add(0, -1, 0);

            if(!mineManager.contains(location, MineType.GROUND)) {
                return;
            }

            explodeMine(location, MineType.GROUND);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().forEach((block) -> {
            if(isSuitableForMine(block.getType())) {
                explodeUnknownMine(block.getLocation());
            }
        });
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().forEach((block) -> {
            if(isSuitableForMine(block.getType())) {
                explodeUnknownMine(block.getLocation());
            }
        });
    }

    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        event.getBlocks().forEach((block) -> {
            if(isSuitableForMine(block.getType())) {
                explodeUnknownMine(block.getLocation());
            }
        });
    }

    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        event.getBlocks().forEach((block) -> {
            if(isSuitableForMine(block.getType())) {
                explodeUnknownMine(block.getLocation());
            }
        });
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        if(isSuitableForMine(event.getBlock().getType())) {
            mineManager.remove(event.getBlock().getLocation());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(isSuitableForMine(event.getBlock().getType())) {
            explodeUnknownMine(event.getBlock().getLocation());
        }
    }

    @EventHandler
    public void onBlockDestroy(BlockDestroyEvent event) {
        if(!isSuitableForMine(event.getBlock().getType())) {
            return;
        }

        MineType mineType = mineManager.getMineTypeByLocation(event.getBlock().getLocation());

        if(mineType == null) {
            return;
        }

        event.getBlock().getLocation().getWorld().dropItem(event.getBlock().getLocation(), getMineItemStack(mineType));
        mineManager.remove(event.getBlock().getLocation());
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if(event.getIgnitingBlock() == null) {
            return;
        }

        if(!isSuitableForMine(event.getIgnitingBlock().getType())) {
            return;
        }

        HiddenMines.runTaskLater(() -> explodeUnknownMine(event.getIgnitingBlock().getLocation()), 40);
    }

    @EventHandler
    public void onBlockIgniteByPlayer(BlockPlaceEvent event) {
        if(event.getBlockPlaced().getType() != Material.FIRE) {
            return;
        }

        Location location = event.getBlockPlaced().getLocation().clone();
        location.add(0, -1, 0);

        if(!isSuitableForMine(location.getBlock().getType())) {
            return;
        }

        HiddenMines.runTaskLater(() -> explodeUnknownMine(location), 40);
    }

    @EventHandler
    public void onPlayerTripwireHookActivate(BlockRedstoneEvent event) {
        if(event.getBlock().getType() != Material.TRIPWIRE_HOOK) {
            return;
        }

        TripwireHook tripwireHookData = (TripwireHook) event.getBlock().getBlockData();
        RayTraceResult rayTraceResult = event.getBlock().getWorld().rayTraceEntities(event.getBlock().getLocation(), tripwireHookData.getFacing().getDirection(), 16d, 1);

        if(rayTraceResult == null || rayTraceResult.getHitEntity() == null) {
            return;
        }

        if(rayTraceResult.getHitEntity().getType() == EntityType.PLAYER) {
            explodeMine(event.getBlock().getLocation(), MineType.HOOK);
        }
    }
}
