package me.salatosik.hiddenminesplugin.event.listener.mine;

import me.salatosik.hiddenminesplugin.core.data.BaseData;
import me.salatosik.hiddenminesplugin.core.data.MineData;
import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.utils.configuration.Configuration;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class CraftMineListener extends BaseMineListener {
    public CraftMineListener(JavaPlugin plugin, Database database, Configuration configuration) {
        super(plugin, database, configuration);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        ItemMeta resultItemMeta = event.getRecipe().getResult().getItemMeta();
        if(resultItemMeta == null) return;

        PersistentDataContainer persistentDataContainer = resultItemMeta.getPersistentDataContainer();

        for(BaseData baseData: MineData.values()) {
            String persistentData = persistentDataContainer.get(baseData.getNamespacedKeyInstance(plugin), PersistentDataType.STRING);

            if(persistentData == null) continue;

            HumanEntity player = event.getView().getPlayer();
            String messagePattern = ChatColor.DARK_RED + "You can`t craft {item_name} because this is not allowed.";

            if(persistentData.equals(MineData.HOOK.getPersistentDataString())) {
                if(configuration.getMineConfiguration().getHook().isAllow()) return;
                player.sendMessage(messagePattern.replace("{item_name}", "hook mine"));
                event.setCancelled(true);

            } else if(persistentData.equals(MineData.GROUND.getPersistentDataString())) {
                if(configuration.getMineConfiguration().getGround().isAllow()) return;
                player.sendMessage(messagePattern.replace("{item_name}", "ground mine"));
                event.setCancelled(true);
            }

            break;
        }
    }
}
