package me.salatosik.hiddenminesplugin;

import me.salatosik.hiddenminesplugin.core.MineData;
import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.listener.MineInteractionMineListener;
import me.salatosik.hiddenminesplugin.listener.MinePlaceBreakMineListener;
import me.salatosik.hiddenminesplugin.utils.ConfigValue;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

// TODO вивести всі виводи для ігрового чату в мовний файл (uk_UA.properties, en_US.properties and etc.)
public final class HiddenMinesPlugin extends JavaPlugin {
    private final Logger logger = getLogger();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Database database;
        try {
            String databaseFilename = getConfig().getString(ConfigValue.DATABASE.key);
            database = initDatabase(databaseFilename);
        } catch(Exception exception) {
            exception.printStackTrace();
            logger.warning("Database file is not created!");
            getPluginLoader().disablePlugin(this);
            return;
        }

        initRecipes();

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new MinePlaceBreakMineListener(this, database), this);
        pluginManager.registerEvents(new MineInteractionMineListener(this, database), this);
    }

    @Override
    public void onDisable() {
        logger.info("Plugin disabled!");
    }

    private Database initDatabase(String filename) throws IOException, ClassNotFoundException, SQLException {
        File databaseFile = new File(getDataFolder(), filename);
        if(!databaseFile.exists()) if(!databaseFile.createNewFile()) throw new IOException("The database file is not created.");
        return new Database(databaseFile);
    }

    private void initRecipes() {
        for(MineData mineData : MineData.values()) {
            if(mineData.nameSpacedKey == null) continue;
            NamespacedKey namespacedKey = new NamespacedKey(this, mineData.nameSpacedKey);
            ItemStack itemStack = mineData.toItemStack(namespacedKey);
            ShapedRecipe shapedRecipe = new ShapedRecipe(namespacedKey, itemStack);
            shapedRecipe.shape(mineData.recipe);
            for(MineData.Ingredient ingredient: mineData.ingredients) shapedRecipe.setIngredient(ingredient.key, ingredient.item);
            Bukkit.addRecipe(shapedRecipe);
        }
    }
}
