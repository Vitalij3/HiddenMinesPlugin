package salatosik.hiddenmines.listener.mine;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import salatosik.hiddenmines.HiddenMines;
import salatosik.hiddenmines.configuration.PluginConfiguration;
import salatosik.hiddenmines.core.MineManager;
import salatosik.hiddenmines.core.mine.Mine;
import salatosik.hiddenmines.core.mine.MineType;
import salatosik.hiddenmines.listener.BasePluginListener;

import java.util.List;

public class BaseMineListener extends BasePluginListener {
    protected final MineManager mineManager;

    public BaseMineListener(PluginConfiguration configuration, MineManager mineManager) {
        super(configuration);
        this.mineManager = mineManager;

        // registering hook mine recipe
        ItemStack hookMineItemStack = new ItemStack(MineType.HOOK.getMaterial());
        getConfiguration().getHookMineLocalizationSection().applyForItemStack(hookMineItemStack);
        markMineByType(hookMineItemStack, MineType.HOOK);
        HiddenMines.registerRecipe(configuration.getShapedRecipeSectionHook().toShapedRecipe(hookMineItemStack, MineType.HOOK.getNamespacedKey()));

        // registering ground mine recipe
        ItemStack groundMineItemStack = new ItemStack(MineType.GROUND.getMaterial());
        getConfiguration().getGroundMineLocalizationSection().applyForItemStack(groundMineItemStack);
        markMineByType(groundMineItemStack, MineType.GROUND);
        HiddenMines.registerRecipe(configuration.getShapedRecipeSectionGround().toShapedRecipe(groundMineItemStack, MineType.GROUND.getNamespacedKey()));

        this.hookMine = hookMineItemStack;
        this.groundMine = groundMineItemStack;
    }

    private final ItemStack hookMine;
    private final ItemStack groundMine;

    @EventHandler
    public void onItemCraft(CraftItemEvent event) {
        for(ItemStack itemStack: event.getInventory().getMatrix()) {
            if(itemStack != null && (itemStack.equals(hookMine) || itemStack.equals(groundMine))) {
                event.setCancelled(true);
                break;
            }
        }
    }

    private boolean itIsMineMaterial(Material material) {
        return material == MineType.HOOK.getMaterial() || material == MineType.GROUND.getMaterial();
    }

    protected MineType defineMineType(ItemStack itemStack) {
        if(!itIsMineMaterial(itemStack.getType())) {
            return null;
        }

        for(MineType mineType: MineType.values()) {
            if(itemStack.getItemMeta().getPersistentDataContainer().has(mineType.getNamespacedKey(), PersistentDataType.BYTE)) {
                return mineType;
            }
        }

        return null;
    }

    protected void markMineByType(ItemStack itemStack, MineType mineType) {
        if(!itIsMineMaterial(itemStack.getType())) {
            return;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();

        if(!persistentDataContainer.has(mineType.getNamespacedKey())) {
            persistentDataContainer.set(mineType.getNamespacedKey(), PersistentDataType.BYTE, (byte) 0);
        }

        itemStack.setItemMeta(itemMeta);
    }

    protected void explodeMine(Location location, MineType mineType) {
        if(mineManager.remove(location)) {
            switch(mineType) {
                case GROUND -> {
                    Location clonedLocation = location.clone();
                    clonedLocation.add(0, 1, 0);
                    location.getWorld().createExplosion(clonedLocation, getConfiguration().getGroundMineExplosionPower(), false, getConfiguration().isGroundMineBreakBlocks());
                }
                case HOOK -> location.getWorld().createExplosion(location, getConfiguration().getHookMineExplosionPower(), false, getConfiguration().isHookMineBreakBlocks());
            }
        }
    }

    protected void explodeUnknownMine(Location location) {
        MineType mineType = mineManager.getMineTypeByLocation(location);

        if(mineType == null) {
            return;
        }

        explodeMine(location, mineType);
    }

    protected boolean groundIsSuitable(Material material) {
        return switch(material) {
            case SAND, SOUL_SAND, RED_SAND, SUSPICIOUS_SAND, GRASS_BLOCK, DIRT, DIRT_PATH, COARSE_DIRT, ROOTED_DIRT -> true;
            default -> false;
        };
    }

    protected boolean isSuitableForMine(Material material) {
        return groundIsSuitable(material) || material == Material.TRIPWIRE_HOOK;
    }

    protected void decrementItemStack(GameMode gameMode, ItemStack itemStack) {
        switch(gameMode) {
            case SURVIVAL, ADVENTURE -> itemStack.setAmount(itemStack.getAmount() - 1);
        }
    }

    protected ItemStack getMineItemStack(MineType mineType, int count) {
        ItemStack itemStack = new ItemStack(mineType.getMaterial(), count);

        switch(mineType) {
            case HOOK -> getConfiguration().getHookMineLocalizationSection().applyForItemStack(itemStack);
            case GROUND -> getConfiguration().getGroundMineLocalizationSection().applyForItemStack(itemStack);
        }

        markMineByType(itemStack, mineType);
        return itemStack;
    }

    protected ItemStack getMineItemStack(MineType mineType) {
        return getMineItemStack(mineType, 1);
    }
}
