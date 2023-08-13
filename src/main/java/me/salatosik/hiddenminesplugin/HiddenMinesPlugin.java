package me.salatosik.hiddenminesplugin;

import me.salatosik.hiddenminesplugin.core.MineData;
import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.listener.MineCosmeticListener;
import me.salatosik.hiddenminesplugin.listener.MineInteractionMineListener;
import me.salatosik.hiddenminesplugin.listener.MinePlaceBreakMineListener;
import me.salatosik.hiddenminesplugin.utils.CommonFunctionThrowsException;
import me.salatosik.hiddenminesplugin.utils.configuration.Configuration;
import me.salatosik.hiddenminesplugin.utils.configuration.database.DatabaseConfiguration;
import me.salatosik.hiddenminesplugin.utils.configuration.mine.MineConfiguration;
import me.salatosik.hiddenminesplugin.utils.configuration.mine.ground.Ground;
import me.salatosik.hiddenminesplugin.utils.configuration.mine.hook.Hook;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

// TODO вивести всі виводи для ігрового чату в мовний файл (uk_UA.properties, en_US.properties and etc.)
public final class HiddenMinesPlugin extends JavaPlugin {
    private final Logger logger = getLogger();
    private Configuration configuration;
    private Database database;

    private boolean templateInit(String exceptionLoggerText, CommonFunctionThrowsException function) {
        try {
            function.invoke();
            return false;
        } catch(Exception exception) {
            exception.printStackTrace();
            logger.warning(exceptionLoggerText);
            getPluginLoader().disablePlugin(this);
            return true;
        }
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if(templateInit("Configuration is not initialized!", () -> { configuration = initConfiguration(); })) return;
        if(templateInit("Database file is not created!", () -> { database = initDatabase(configuration.databaseConfiguration.filename); })) return;

        initRecipes();

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new MinePlaceBreakMineListener(this, database, configuration), this);
        pluginManager.registerEvents(new MineInteractionMineListener(this, database, configuration), this);
        pluginManager.registerEvents(new MineCosmeticListener(this, database, configuration), this);
    }

    @Override
    public void onDisable() {
        logger.info("Plugin disabled!");
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        templateInit("Failed to reload configuration!", () -> { configuration = initConfiguration(); });
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

    private Configuration initConfiguration() {
        FileConfiguration fc = getConfig();

        // database config
        String databaseFilename = fc.getString("database.filename", "database.db");
        if(!databaseFilename.endsWith(".db") || !databaseFilename.endsWith("db")) databaseFilename = databaseFilename + ".db";
        DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration(databaseFilename);

        // primitive attrs for mine.ground
        double groundMineExplosionPower = fc.getDouble("mine.ground.explosionPower");
        boolean groundMineCosmetic = fc.getBoolean("mine.ground.cosmetic");
        boolean groundMineAdaptiveCosmetic = fc.getBoolean("mine.ground.adaptiveCosmetic");

        // primitive attrs for mine.hook
        double hookMineExplosionPower = fc.getDouble("mine.hook.explosionPower");
        boolean hookMineCosmetic = fc.getBoolean("mine.hook.cosmetic");

        // mines configuration
        Ground groundMineConfiguration = new Ground(groundMineExplosionPower, groundMineCosmetic, groundMineAdaptiveCosmetic);
        Hook hookMineConfiguration = new Hook(hookMineExplosionPower, hookMineCosmetic);

        // all mines configuration
        MineConfiguration mineConfiguration = new MineConfiguration(groundMineConfiguration, hookMineConfiguration);

        return new Configuration(databaseConfiguration, mineConfiguration);
    }
}
