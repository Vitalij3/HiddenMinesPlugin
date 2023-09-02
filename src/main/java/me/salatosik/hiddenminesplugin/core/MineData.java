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
            "hook",
            new Ingredient[]{ new Ingredient('Y', Material.STONE_BUTTON), new Ingredient('X', Material.TNT) }
    ),

    GROUND(
            "ground-mine",
            "this-is-ground-mine",
            new String[]{ "Y", "X" },
            ChatColor.DARK_RED + "Ground mine",
            "ground",
            new Ingredient[]{ new Ingredient('Y', Material.STONE_PRESSURE_PLATE), new Ingredient('X', Material.TNT) }
    );

    public static final Material GENERAL_OUTPUT_ITEM = Material.TNT;

    public final String nameSpacedKey;
    public final String[] recipe;
    public final String persistentData;
    public final String localizedName;
    public final Ingredient[] ingredients;
    public final String mineName;

    MineData(String nameSpacedKey, String persistentData, String[] recipe, String localizedName, String mineName, Ingredient[] ingredients) {
        this.nameSpacedKey = nameSpacedKey;
        this.recipe = recipe;
        this.persistentData = persistentData;
        this.localizedName = localizedName;
        this.mineName = mineName;
        this.ingredients = ingredients;
    }

    public ItemStack toItemStack(JavaPlugin javaPlugin) {
        ItemStack itemStack = new ItemStack(GENERAL_OUTPUT_ITEM, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(new NamespacedKey(javaPlugin, nameSpacedKey), PersistentDataType.STRING, persistentData);
        itemMeta.displayName(Component.text(localizedName));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public ItemStack toItemStack(NamespacedKey namespacedKey) {
        ItemStack itemStack = new ItemStack(GENERAL_OUTPUT_ITEM, 1);
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

    public static MineData valueOfMineName(String mineName) {
        for(MineData md: MineData.values()) {
            if(md.mineName.equals(mineName)) {
                return md;
            }
        }

        return null;
    }
}
