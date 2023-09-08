package me.salatosik.hiddenminesplugin.event.listener;

import me.salatosik.hiddenminesplugin.core.MineData;
import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.UtilMethods;
import me.salatosik.hiddenminesplugin.core.database.models.Mine;
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

public class MinePlaceBreakListener extends BaseListener {
    public MinePlaceBreakListener(JavaPlugin plugin, Database database, Configuration configuration) {
        super(plugin, database, configuration);
    }

    private enum SetupMineState {
        IS_NOT_GROUND_MINE, IS_NOT_HOOK_MINE, IT_IS_ALREADY_MINE, SUCCESS, CLICKED_BLOCK_IS_NULL, NULL, NOT_ALLOWED
    }

    private SetupMineState setupMine(PlayerInteractEvent event, MineData mineType) {
        Block clickedBlock = event.getClickedBlock();
        if(clickedBlock == null) return SetupMineState.CLICKED_BLOCK_IS_NULL;

        switch(mineType) {
            case GROUND:
                if(!configuration.getMineConfiguration().getGround().getAllow()) return SetupMineState.NOT_ALLOWED;
                if(!itIsPossibleGroundMine(clickedBlock)) return SetupMineState.IS_NOT_GROUND_MINE;
                break;

            case HOOK:
                if(!configuration.getMineConfiguration().getHook().getAllow()) return SetupMineState.NOT_ALLOWED;
                if(!itIsPossibleHookMine(clickedBlock)) return SetupMineState.IS_NOT_HOOK_MINE;
                break;
        }

        if(itIsMine(clickedBlock)) return SetupMineState.IT_IS_ALREADY_MINE;

        return SetupMineState.SUCCESS;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMineRightClick(PlayerInteractEvent event) {
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack clickedItem = event.getItem();
        if(clickedItem == null || clickedItem.getType() != MineData.GENERAL_OUTPUT_ITEM) return;

        Block clickedBlock = event.getClickedBlock();
        if(clickedBlock == null) return;

        ItemMeta clickedItemMeta = clickedItem.getItemMeta();
        PersistentDataContainer clickedItemMetaPersistentData = clickedItemMeta.getPersistentDataContainer();

        NamespacedKey hookMineNamespacedKey = new NamespacedKey(plugin, MineData.HOOK.getNamespacedKeyString());
        NamespacedKey groundMineNamespacedKey = new NamespacedKey(plugin, MineData.GROUND.getNamespacedKeyString());

        String clickedItemPersistentDataHook = clickedItemMetaPersistentData.get(hookMineNamespacedKey, PersistentDataType.STRING);
        String clickedItemPersistentDataGround = clickedItemMetaPersistentData.get(groundMineNamespacedKey, PersistentDataType.STRING);

        SetupMineState setupMineState = SetupMineState.NULL;
        MineData selectedMineType;

        if(clickedItemPersistentDataGround != null) {
            setupMineState = setupMine(event, MineData.GROUND);
            selectedMineType = MineData.GROUND;
        } else if(clickedItemPersistentDataHook != null) {
            setupMineState = setupMine(event, MineData.HOOK);
            selectedMineType = MineData.HOOK;
        } else selectedMineType = null;

        boolean allPersistentDataNull = clickedItemPersistentDataGround == null & clickedItemPersistentDataHook == null;

        if(!allPersistentDataNull & setupMineState != SetupMineState.NULL) {
            switch(setupMineState) {
                case SUCCESS:
                    UtilMethods.createBukkitAsyncThreadAndStart(plugin, () -> {
                        Mine groundMine = new Mine(new UnknownMine(clickedBlock), selectedMineType);
                        UtilMethods.addMineToDatabase(groundMine, database, logger, (v) -> UtilMethods.createBukkitThreadAndStart(plugin, () -> {
                            event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "Mine placed!");
                            if(event.getPlayer().getGameMode() != GameMode.CREATIVE)
                                removeItemFromInventory(clickedItem, 1, event.getPlayer().getInventory());
                        }));
                    });
                    event.setCancelled(true);
                    break;

                case IS_NOT_HOOK_MINE:
                    event.getPlayer().sendMessage(ChatColor.DARK_RED + "This is not hook!");
                    event.setCancelled(true);
                    break;

                case IS_NOT_GROUND_MINE:
                    event.getPlayer().sendMessage(ChatColor.DARK_RED + "You cannot use this mine here.");
                    event.setCancelled(true);
                    break;

                case IT_IS_ALREADY_MINE:
                    event.getPlayer().sendMessage(ChatColor.DARK_RED + "This is already {mine_name} mine!".replace("{mine_name}", selectedMineType.getDisplayName()));
                    event.setCancelled(true);
                    break;

                case NOT_ALLOWED:
                    event.getPlayer().sendMessage(ChatColor.DARK_RED + "You can`t place this mine because this mine type is not allowed.");
                    event.setCancelled(true);
                    break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMineBreak(BlockBreakEvent event) {
        if(!itIsPossibleMine(event.getBlock())) return;

        UnknownMine unknownMine = new UnknownMine(event.getBlock());
        Mine mine = UtilMethods.findMineByUnknownMine(minesFromDatabase, unknownMine);
        if(mine == null) return;

        event.setDropItems(false);

        World breakedBlockWorld = event.getBlock().getWorld();
        Location breakedBlockLocation = event.getBlock().getLocation();
        GameMode eventPlayerGamemode = event.getPlayer().getGameMode();

        switch(mine.mineType) {
            case HOOK:
                UtilMethods.createBukkitAsyncThreadAndStart(plugin,
                        () -> UtilMethods.removeMineFromDatabase(mine, database, logger));
                break;

            case GROUND:
                Material itemInMainHand = event.getPlayer().getInventory().getItemInMainHand().getType();

                if(SHOVELS.contains(itemInMainHand)) {
                    UtilMethods.createBukkitAsyncThreadAndStart(plugin, () ->
                            UtilMethods.removeMineFromDatabase(mine, database, logger, (v) -> {

                        if(eventPlayerGamemode != GameMode.CREATIVE) {
                            UtilMethods.createBukkitThreadAndStart(plugin, () -> breakedBlockWorld.dropItem(breakedBlockLocation, MineData.GROUND.toItemStack(plugin)));
                        }
                    }));
                } else detonateMineAndRemoveFromDatabase(breakedBlockLocation);
                break;
        }
    }
}
