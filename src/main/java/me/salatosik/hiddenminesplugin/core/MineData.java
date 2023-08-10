package me.salatosik.hiddenminesplugin.core;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public enum MineData {
    HOOK(
            "hook-mine",
            "this-is-hook-mine",
            new String[]{ "X", "Y" },
            ChatColor.DARK_RED + "Hook mine",
            new Ingredient[]{ new Ingredient('Y', Material.TRIPWIRE_HOOK), new Ingredient('X', Material.TNT) },
            Material.TRIPWIRE_HOOK
    ),

    GROUND(
            "ground-mine",
            "this-is-ground-mine",
            new String[]{ "Y", "X" },
            ChatColor.DARK_RED + " Ground mine",
            new Ingredient[]{ new Ingredient('Y', Material.STONE_BUTTON), new Ingredient('X', Material.TNT) },
            Material.TNT
    );

    public final String nameSpacedKey;
    public final String[] recipe;
    public final String persistentData;
    public final String localizedName;
    public final Ingredient[] ingredients;
    public final Material outputItem;

    MineData(String nameSpacedKey, String persistentData, String[] recipe, String localizedName, Ingredient[] ingredients, Material outputItem) {
        this.nameSpacedKey = nameSpacedKey;
        this.recipe = recipe;
        this.persistentData = persistentData;
        this.localizedName = localizedName;
        this.ingredients = ingredients;
        this.outputItem = outputItem;
    }

    public ItemStack toItemStack(JavaPlugin javaPlugin) {
        ItemStack itemStack = new ItemStack(outputItem, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(new NamespacedKey(javaPlugin, nameSpacedKey), PersistentDataType.STRING, persistentData);
        itemMeta.displayName(Component.text(localizedName));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public ItemStack toItemStack(NamespacedKey namespacedKey) {
        ItemStack itemStack = new ItemStack(outputItem, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(namespacedKey, PersistentDataType.STRING, persistentData);
        itemMeta.displayName(Component.text(localizedName));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static class Ingredient {
        public final char key;
        public final Material item;

        public Ingredient(char key, Material item) {
            this.key = key;
            this.item = item;
        }
    }
}
