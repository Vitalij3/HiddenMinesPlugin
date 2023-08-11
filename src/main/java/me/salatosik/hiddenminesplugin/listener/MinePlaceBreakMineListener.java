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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
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
        if(event.getBlock().getType() != Material.TNT) return;

        Block placedBlock = event.getBlockPlaced();
        World placedBlockWorld = placedBlock.getWorld();
        Location placedBlockLocation = placedBlock.getLocation();
        placedBlockLocation.setY(placedBlockLocation.getY() - 1);
        Material bottomPlacedBlockType = placedBlockWorld.getBlockAt(placedBlockLocation).getType();

        if(!ALLOWED_GROUND.contains(bottomPlacedBlockType)) {
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "You cannot use this mine here.");
            event.setCancelled(true);
            event.getPlayer().getInventory().addItem(MineData.GROUND.toItemStack(plugin));
            return;
        }

        NamespacedKey placedBlockNamespaceKey = new NamespacedKey(plugin, MineData.GROUND.nameSpacedKey);
        String data = event.getItemInHand().getItemMeta().getPersistentDataContainer()
                .get(placedBlockNamespaceKey, PersistentDataType.STRING);

        if(data != null && data.equals(MineData.GROUND.persistentData)) {
            placedBlock.setType(Material.AIR);
            Mine groundMine = new Mine(placedBlock.getX(), placedBlock.getY() - 1, placedBlock.getZ(), MineType.GROUND);
            UtilMethods.addMineToDatabase(groundMine, database, logger);
            removeItemFromInventory(event.getItemInHand(), 1, event.getPlayer().getInventory());
            event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "Ground mine is placed!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRightClick(PlayerInteractEvent event) {
        ItemStack clickedItem = event.getItem();
        if(clickedItem == null) return;

        Block clickedBlock = event.getClickedBlock();
        if(clickedBlock == null || event.getClickedBlock().getType() != Material.TRIPWIRE_HOOK) return;
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemMeta clickedItemMeta = clickedItem.getItemMeta();
        PersistentDataContainer clickedDataContainer = clickedItemMeta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(plugin, MineData.HOOK.nameSpacedKey);

        String clickedPersistentString = clickedDataContainer.get(namespacedKey, PersistentDataType.STRING);
        if(clickedPersistentString == null || !clickedPersistentString.equals(MineData.HOOK.persistentData)) return;

        Location clickedLocation = clickedBlock.getLocation();
        UnknownMine clickedUnknownMine = new UnknownMine(clickedLocation.getBlockX(), clickedLocation.getBlockY(), clickedLocation.getBlockZ());

        for(Mine mine: minesFromDatabase) {
            if(mine.equals(clickedUnknownMine)) {
                event.getPlayer().sendMessage(ChatColor.DARK_RED + "This is already mine!");
                return;
            }
        }

        UtilMethods.addMineToDatabase(new Mine(clickedUnknownMine, MineType.HOOK), database, logger);
        removeItemFromInventory(clickedItem, 1, event.getPlayer().getInventory());
        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "Hook mine is placed!");
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
                breakedBlockWorld.dropItem(breakedBlockLocation, new ItemStack(Material.TRIPWIRE_HOOK, 1));
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
        if(itIsPossibleMine(event.getBlock())) {
            detonateMine(event.getBlock().getLocation());
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
