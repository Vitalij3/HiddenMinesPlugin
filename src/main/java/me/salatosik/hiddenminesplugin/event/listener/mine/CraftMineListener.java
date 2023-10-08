package me.salatosik.hiddenminesplugin.event.listener.mine;

import me.salatosik.hiddenminesplugin.core.data.MineData;
import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.utils.configuration.Configuration;
import me.salatosik.hiddenminesplugin.utils.configuration.mine.MineConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class CraftMineListener extends BaseMineListener {
    public CraftMineListener(JavaPlugin plugin, Database database, Configuration configuration) {
        super(plugin, database, configuration);
    }

    @EventHandler
    public void onPlayerCraftItem(CraftItemEvent event) {
        ItemStack resultItemStack = event.getRecipe().getResult();
        ItemMeta resultItemMeta = resultItemStack.getItemMeta();
        PersistentDataContainer resultPersistentContainer = resultItemMeta.getPersistentDataContainer();
        MineConfiguration mineConfiguration = getConfiguration().getMineConfiguration();

        for(MineData mineData: MineData.values()) {
            String minePersistentData = resultPersistentContainer.get(mineData.getNamespacedKeyInstance(getPlugin()), PersistentDataType.STRING);

            if(minePersistentData == null) continue;

            switch(mineData) {
                case GROUND:
                    if(mineConfiguration.getGround().isAllow()) return;
                    break;
                case HOOK:
                    if(mineConfiguration.getHook().isAllow()) return;
                    break;
            }

            event.getView().getPlayer().sendMessage(ChatColor.DARK_RED + "You cannot create this item.");
            event.setCancelled(true);
        }
    }
}
