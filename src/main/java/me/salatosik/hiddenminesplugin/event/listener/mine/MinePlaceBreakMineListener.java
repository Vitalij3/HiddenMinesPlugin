package me.salatosik.hiddenminesplugin.event.listener.mine;

import me.salatosik.hiddenminesplugin.core.data.MineData;
import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.UtilMethods;
import me.salatosik.hiddenminesplugin.core.database.models.mine.Mine;
import me.salatosik.hiddenminesplugin.core.database.models.mine.UnknownMine;
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

public class MinePlaceBreakMineListener extends BaseMineListener {
    public MinePlaceBreakMineListener(JavaPlugin plugin, Database database, Configuration configuration) {
        super(plugin, database, configuration);
    }

    private enum SetupMineState {
        IS_NOT_GROUND_MINE,
        IS_NOT_HOOK_MINE,
        IT_IS_ALREADY_MINE,
        SUCCESS,
        CLICKED_BLOCK_IS_NULL,
        NULL,
        NOT_ALLOWED
    }

    private SetupMineState setupMine(PlayerInteractEvent event, MineData mineType) {
        Block clickedBlock = event.getClickedBlock();
        if(clickedBlock == null) return SetupMineState.CLICKED_BLOCK_IS_NULL;

        switch(mineType) {
            case GROUND:
                if(!getConfiguration().getMineConfiguration().getGround().isAllow()) return SetupMineState.NOT_ALLOWED;
                if(!itIsPossibleGroundMine(clickedBlock)) return SetupMineState.IS_NOT_GROUND_MINE;
                break;

            case HOOK:
                if(!getConfiguration().getMineConfiguration().getHook().isAllow()) return SetupMineState.NOT_ALLOWED;
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

        NamespacedKey hookMineNamespacedKey = MineData.HOOK.getNamespacedKeyInstance(getPlugin());
        NamespacedKey groundMineNamespacedKey = MineData.GROUND.getNamespacedKeyInstance(getPlugin());

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
                    UtilMethods.createBukkitAsyncThreadAndStart(getPlugin(), () -> {
                        Mine groundMine = new Mine(new UnknownMine(clickedBlock), selectedMineType);
                        UtilMethods.addMineToDatabase(groundMine, getDatabase(), (v) -> UtilMethods.createBukkitThreadAndStart(getPlugin(), () -> {
                            event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "Mine placed!");
                            if(event.getPlayer().getGameMode() != GameMode.CREATIVE)
                                clickedItem.setAmount(clickedItem.getAmount() - 1);
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
        Mine mine = UtilMethods.findMineByUnknownMine(getDatabase().getMines(), unknownMine);
        if(mine == null) return;

        event.setDropItems(false);

        World breakedBlockWorld = event.getBlock().getWorld();
        Location breakedBlockLocation = event.getBlock().getLocation();
        GameMode eventPlayerGamemode = event.getPlayer().getGameMode();

        switch(mine.getMineType()) {
            case HOOK:
                UtilMethods.createBukkitAsyncThreadAndStart(getPlugin(),
                        () -> UtilMethods.removeMineFromDatabase(mine, getDatabase()));
                break;

            case GROUND:
                Material itemInMainHand = event.getPlayer().getInventory().getItemInMainHand().getType();

                if(SHOVELS.contains(itemInMainHand)) {
                    UtilMethods.createBukkitAsyncThreadAndStart(getPlugin(), () ->
                            UtilMethods.removeMineFromDatabase(mine, getDatabase(), (v) -> {

                        if(eventPlayerGamemode != GameMode.CREATIVE) {
                            UtilMethods.createBukkitThreadAndStart(getPlugin(), () -> breakedBlockWorld.dropItem(breakedBlockLocation, MineData.GROUND.toItemStack(getPlugin())));
                        }
                    }));
                } else detonateMineAndRemoveFromDatabase(breakedBlockLocation);
                break;
        }
    }
}
