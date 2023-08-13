package me.salatosik.hiddenminesplugin.listener;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import me.salatosik.hiddenminesplugin.core.MineData;
import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.UtilMethods;
import me.salatosik.hiddenminesplugin.core.database.models.Mine;
import me.salatosik.hiddenminesplugin.core.database.models.MineType;
import me.salatosik.hiddenminesplugin.core.database.models.UnknownMine;
import me.salatosik.hiddenminesplugin.utils.configuration.Configuration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class MinePlaceBreakMineListener extends BaseMineListener {
    public MinePlaceBreakMineListener(JavaPlugin plugin, Database database, Configuration configuration) {
        super(plugin, database, configuration);
    }

    @Override
    String getChildClassName() {
        return this.getClass().getName();
    }

    private boolean setupMine(PlayerInteractEvent event, MineType mineType) {
        Block clickedBlock = event.getClickedBlock();
        if(clickedBlock == null) return false;

        switch(mineType) {
            case GROUND:
                if(!itIsPossibleGroundMine(clickedBlock)) {
                    event.getPlayer().sendMessage(ChatColor.DARK_RED + "You cannot use this mine here.");
                    return false;
                }
                break;

            case HOOK:
                if(!itIsPossibleHookMine(clickedBlock)) {
                    event.getPlayer().sendMessage(ChatColor.DARK_RED + "This is not hook!");
                    return false;
                }
                break;
        }

        if(itIsMine(clickedBlock)) {
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "This is already {mine_name} mine!".replace("{mine_name}", mineType.mineName));
            return false;
        }

        Mine groundMine = UtilMethods.getMineByBlock(clickedBlock.getLocation(), mineType);
        UtilMethods.addMineToDatabase(groundMine, database, logger);

        event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "{mine_name} mine is placed!"
                .replace("{mine_name}", mineType.mineName
                        .replace(mineType.mineName.charAt(0), Character.toUpperCase(mineType.mineName.charAt(0)))));

        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRightClick(PlayerInteractEvent event) {
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack clickedItem = event.getItem();
        if(clickedItem == null || clickedItem.getType() != MineData.GENERAL_OUTPUT_ITEM) return;

        Block clickedBlock = event.getClickedBlock();
        if(clickedBlock == null) return;

        ItemMeta clickedItemMeta = clickedItem.getItemMeta();
        PersistentDataContainer clickedItemMetaPersistentData = clickedItemMeta.getPersistentDataContainer();

        NamespacedKey hookMineNamespacedKey = new NamespacedKey(plugin, MineData.HOOK.nameSpacedKey);
        NamespacedKey groundMineNamespacedKey = new NamespacedKey(plugin, MineData.GROUND.nameSpacedKey);

        String clickedItemPersistentDataHook = clickedItemMetaPersistentData.get(hookMineNamespacedKey, PersistentDataType.STRING);
        String clickedItemPersistentDataGround = clickedItemMetaPersistentData.get(groundMineNamespacedKey, PersistentDataType.STRING);

        boolean initializedSuccessful = false;
        if(clickedItemPersistentDataGround != null) initializedSuccessful = setupMine(event, MineType.GROUND);
        else if(clickedItemPersistentDataHook != null) initializedSuccessful = setupMine(event, MineType.HOOK);

        boolean allPersistentDataNull = clickedItemPersistentDataGround == null || clickedItemPersistentDataHook == null;

        if(initializedSuccessful && !allPersistentDataNull) {
            if(event.getPlayer().getGameMode() != GameMode.CREATIVE)
                removeItemFromInventory(clickedItem, 1, event.getPlayer().getInventory());
        }

        if(allPersistentDataNull) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerBreakBlock(BlockBreakEvent event) {
        if(!itIsPossibleMine(event.getBlock())) return;

        UnknownMine unknownMine = UtilMethods.getUnknownMineByBlock(event.getBlock());
        Mine mine = UtilMethods.findMineByUnknownMine(minesFromDatabase, unknownMine);
        if(mine == null) return;

        event.setDropItems(false);

        World breakedBlockWorld = event.getBlock().getWorld();
        Location breakedBlockLocation = event.getBlock().getLocation();
        GameMode eventPlayerGamemode = event.getPlayer().getGameMode();

        switch(mine.mineType) {
            case HOOK:
                UtilMethods.removeMineFromDatabase(mine, database, logger);
                if(eventPlayerGamemode != GameMode.CREATIVE) {
                    breakedBlockWorld.dropItem(breakedBlockLocation, MineData.HOOK.toItemStack(plugin));
                    breakedBlockWorld.dropItem(breakedBlockLocation, new ItemStack(Material.TRIPWIRE_HOOK, 1));
                }
                break;

            case GROUND:
                Material itemInMainHand = event.getPlayer().getInventory().getItemInMainHand().getType();

                if(SHOVELS.contains(itemInMainHand)) {
                    UtilMethods.removeMineFromDatabase(mine, database, logger);
                    if(eventPlayerGamemode != GameMode.CREATIVE) {
                        breakedBlockWorld.dropItem(breakedBlockLocation, MineData.GROUND.toItemStack(plugin));
                    }
                } else detonateMineAndRemoveFromDatabase(breakedBlockLocation);
                break;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockDestroy(BlockDestroyEvent event) {
        if(itIsPossibleMine(event.getBlock())) {
            detonateMineAndRemoveFromDatabase(event.getBlock().getLocation(), false);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockDamage(BlockDamageEvent event) {
        if(itIsPossibleMine(event.getBlock())) {
            detonateMineAndRemoveFromDatabase(event.getBlock().getLocation());
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

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPistonIn(BlockPistonExtendEvent event) {
        detonateMineOnPistonEvent(event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPistonOut(BlockPistonRetractEvent event) {
        detonateMineOnPistonEvent(event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if(ALLOWED_GROUND_MINE_GROUNDS.contains(event.getBlock().getType())) {
            detonateMineAndRemoveFromDatabase(event.getBlock().getLocation());
        }
    }
}
