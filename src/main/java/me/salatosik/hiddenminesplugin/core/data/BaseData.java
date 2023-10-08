package me.salatosik.hiddenminesplugin.core.data;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public interface BaseData {
    default ItemStack toItemStack(JavaPlugin plugin, int amount) {
        ItemStack itemStack = new ItemStack(getOutputType(), amount);
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
        persistentDataContainer.set(new NamespacedKey(plugin, getNamespacedKeyString()), PersistentDataType.STRING, getPersistentDataString());
        itemMeta.displayName(Component.text(getDisplayName()));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    default ItemStack toItemStack(JavaPlugin plugin) {
        return toItemStack(plugin, 1);
    }

    String getPersistentDataString();
    String getDisplayName();
    Material getOutputType();
    String getNamespacedKeyString();

    class NamespacedKeyContainer {
        private NamespacedKey namespacedKey = null;

        public void setNamespacedKey(NamespacedKey namespacedKey) {
            this.namespacedKey = namespacedKey;
        }

        public NamespacedKey getNamespacedKey() {
            return this.namespacedKey;
        }
    }

    NamespacedKeyContainer namespacedKeyContainer = new NamespacedKeyContainer();

    default NamespacedKey getNamespacedKeyInstance(JavaPlugin javaPlugin) {
        if(namespacedKeyContainer.getNamespacedKey() == null) {
            namespacedKeyContainer.setNamespacedKey(new NamespacedKey(javaPlugin, getNamespacedKeyString()));
            return namespacedKeyContainer.getNamespacedKey();
        }

        return namespacedKeyContainer.getNamespacedKey();
    }
}
