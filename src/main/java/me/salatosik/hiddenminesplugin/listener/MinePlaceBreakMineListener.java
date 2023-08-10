package me.salatosik.hiddenminesplugin.listener;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import me.salatosik.hiddenminesplugin.core.MineData;
import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.UtilMethods;
import me.salatosik.hiddenminesplugin.core.database.models.Mine;
import me.salatosik.hiddenminesplugin.core.database.models.MineType;
import me.salatosik.hiddenminesplugin.core.database.models.UnknownMine;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class MinePlaceBreakMineListener extends BaseMineListener {
    public MinePlaceBreakMineListener(JavaPlugin plugin, Database database) {
        super(plugin, database);
    }

    @Override
    String getChildClassName() {
        return this.getClass().getName();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPlaceBlock(BlockPlaceEvent event) {
        if(!itIsMine(event.getBlock())) return;

        Material placedBlockType = event.getBlock().getType();
        World placedBlockWorld = event.getBlock().getWorld();
        Location placedBlockLocation = event.getBlockPlaced().getLocation();
        placedBlockLocation.setY(placedBlockLocation.getY() - 1);
        Material bottomPlacedBlockType = placedBlockWorld.getBlockAt(placedBlockLocation).getType();

        if(placedBlockType == Material.TNT && !ALLOWED_GROUND.contains(bottomPlacedBlockType)) {
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "You cannot use this mine here.");
            event.setCancelled(true);
            event.getPlayer().getInventory().addItem(MineData.GROUND.toItemStack(plugin));
            return;
        }

        NamespacedKey placedBlockNamespaceKey = null;

        switch(placedBlockType) {
            case TNT:
                placedBlockNamespaceKey = new NamespacedKey(plugin, MineData.GROUND.nameSpacedKey);
                break;
            case TRIPWIRE_HOOK: placedBlockNamespaceKey = new NamespacedKey(plugin, MineData.HOOK.nameSpacedKey);
                break;
        }

        if(placedBlockNamespaceKey == null) return;

        String data = event.getItemInHand().getItemMeta().getPersistentDataContainer()
                .get(placedBlockNamespaceKey, PersistentDataType.STRING);

        if(data != null) {
            Block block = event.getBlockPlaced();

            if(data.equals(MineData.HOOK.persistentData)) {
                Mine hookMine = new Mine(block.getX(), block.getY(), block.getZ(), MineType.HOOK);
                UtilMethods.addMineToDatabase(hookMine, database, logger);

            } else if(data.equals(MineData.GROUND.persistentData)) {
                block.setType(Material.AIR);
                Mine groundMine = new Mine(block.getX(), block.getY() - 1, block.getZ(), MineType.GROUND);
                UtilMethods.addMineToDatabase(groundMine, database, logger);
                event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "The mine is placed!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerBreakBlock(BlockBreakEvent event) {
        if(!itIsPossibleMine(event.getBlock())) return;

        Block breakedBlock = event.getBlock();
        UnknownMine unknownMine = new UnknownMine(breakedBlock.getX(), breakedBlock.getY(), breakedBlock.getZ());

        Mine mine = UtilMethods.findMineByUnknownMine(minesFromDatabase, unknownMine);
        if(mine == null) return;

        event.setDropItems(false);

        World breakedBlockWorld = breakedBlock.getWorld();
        Location breakedBlockLocation = breakedBlock.getLocation();

        switch(mine.mineType) {
            case HOOK:
                breakedBlockWorld.dropItem(breakedBlockLocation, MineData.HOOK.toItemStack(plugin));
                break;

            case GROUND:
                Material itemInMainHand = event.getPlayer().getInventory().getItemInMainHand().getType();

                breakedBlockLocation.setY(breakedBlockLocation.getY() + 1);
                if(SHOVELS.contains(itemInMainHand)) breakedBlockWorld.dropItem(breakedBlockLocation, MineData.GROUND.toItemStack(plugin));
                else breakedBlockWorld.createExplosion(breakedBlockLocation, EXPLOSION_POWER);

                break;
        }

        UtilMethods.removeMineFromDatabase(mine, database, logger);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block eventBlock = event.getBlock();
        Block sourceBlock = event.getSourceBlock();
        Location eventBlockLocation = eventBlock.getLocation();

        if(itIsPossibleGroundMine(eventBlock)) {
            Location sourceBlockLocation = sourceBlock.getLocation();

            int eventX = eventBlockLocation.getBlockX();
            int eventY = eventBlockLocation.getBlockY();
            int eventZ = eventBlockLocation.getBlockZ();

            int sourceX = sourceBlockLocation.getBlockX();
            int sourceY = sourceBlockLocation.getBlockY();
            int sourceZ = sourceBlockLocation.getBlockZ();

            if(eventX == sourceX && eventZ == sourceZ) {
                if(eventY < sourceY) {
                    if(sourceY - eventY == 1) {
                        detonateMine(event.getBlock().getLocation());
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockDestroy(BlockDestroyEvent event) {
        if(itIsPossibleMine(event.getBlock())) {
            detonateMine(event.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBurn(BlockBurnEvent event) {
        if(itIsPossibleMine(event.getBlock())) {
            detonateMine(event.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockDamage(BlockDamageEvent event) {
        if(itIsPossibleMine(event.getBlock())) {
            detonateMine(event.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockExplode(BlockExplodeEvent event) {
        if(itIsPossibleMine(event.getBlock())) {
            detonateMine(event.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockFertilize(BlockFertilizeEvent event) {
        if(itIsPossibleMine(event.getBlock())) {
            detonateMine(event.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockFade(BlockFadeEvent event) {
        if(itIsPossibleMine(event.getBlock())) {
            detonateMine(event.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if(itIsPossibleMine(event.getBlock())) {
            detonateMine(event.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPistonIn(BlockPistonExtendEvent event) {
        if(itIsPossibleMine(event.getBlock())) {
            detonateMine(event.getBlock().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPistonOut(BlockPistonRetractEvent event) {
        if(itIsPossibleMine(event.getBlock())) {
            detonateMine(event.getBlock().getLocation());
        }
    }
}
