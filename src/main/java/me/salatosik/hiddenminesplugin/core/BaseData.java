package me.salatosik.hiddenminesplugin.core;

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
    String[] getRecipe();
    Ingredient[] getIngredients();
}
