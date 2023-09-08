package me.salatosik.hiddenminesplugin;

import me.salatosik.hiddenminesplugin.core.BaseData;
import me.salatosik.hiddenminesplugin.core.Ingredient;
import me.salatosik.hiddenminesplugin.core.MineData;
import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.event.command.client.RemoveMinesExecutor;
import me.salatosik.hiddenminesplugin.event.listener.*;
import me.salatosik.hiddenminesplugin.utils.CommonFunctionThrowsException;
import me.salatosik.hiddenminesplugin.utils.configuration.Configuration;
import me.salatosik.hiddenminesplugin.utils.configuration.database.DatabaseConfiguration;
import me.salatosik.hiddenminesplugin.utils.configuration.mine.MineConfiguration;
import me.salatosik.hiddenminesplugin.utils.configuration.mine.ground.Ground;
import me.salatosik.hiddenminesplugin.utils.configuration.mine.hook.Hook;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

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

        if(templateInit("Configuration is not initialized!", () -> configuration = initConfiguration())) return;
        if(templateInit("Database file is not created!", () -> database = initDatabase(configuration.getDatabaseConfiguration().getFilename()))) return;

        initRecipes();

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new MinePlaceBreakListener(this, database, configuration), this);
        pluginManager.registerEvents(new InteractListener(this, database, configuration), this);
        pluginManager.registerEvents(new CosmeticListener(this, database, configuration), this);
        pluginManager.registerEvents(new CraftListener(this, database, configuration), this);

        RemoveMinesExecutor removeMinesExecutor = new RemoveMinesExecutor(this, database, configuration);
        initCommandExecutor("remove", removeMinesExecutor, removeMinesExecutor);
    }

    @Override
    public void onDisable() {
        logger.info("Plugin disabled!");
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        templateInit("Failed to reload configuration!", () -> configuration = initConfiguration());
    }

    private Database initDatabase(String filename) throws IOException, ClassNotFoundException, SQLException {
        File databaseFile = new File(getDataFolder(), filename);
        if(!databaseFile.exists()) if(!databaseFile.createNewFile()) throw new IOException("The database file is not created.");
        return new Database(databaseFile, this);
    }

    private void initRecipes() {
        for(BaseData data: MineData.values()) {
            NamespacedKey namespacedKey = new NamespacedKey(this, data.getNamespacedKeyString());
            ItemStack itemStack = data.toItemStack(this);
            ShapedRecipe shapedRecipe = new ShapedRecipe(namespacedKey, itemStack);
            shapedRecipe.shape(data.getRecipe());
            for(Ingredient ingredient: data.getIngredients()) shapedRecipe.setIngredient(ingredient.key, ingredient.item);
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
        boolean groundBreakBlocks = fc.getBoolean("mine.ground.breakBlocks");
        boolean groundFireBlocks = fc.getBoolean("mine.ground.fireBlocks");
        boolean groundAllow = fc.getBoolean("mine.ground.allow");

        // primitive attrs for mine.hook
        double hookMineExplosionPower = fc.getDouble("mine.hook.explosionPower");
        boolean hookMineCosmetic = fc.getBoolean("mine.hook.cosmetic");
        boolean hookMineBreakBlocks = fc.getBoolean("mine.hook.breakBlocks");
        boolean hookMineFireBlocks = fc.getBoolean("mine.hook.fireBlocks");
        boolean hookAllow = fc.getBoolean("mine.hook.allow");

        // mines configuration
        Ground groundMineConfiguration = new Ground(groundMineExplosionPower, groundMineCosmetic, groundMineAdaptiveCosmetic, groundBreakBlocks, groundFireBlocks, groundAllow);
        Hook hookMineConfiguration = new Hook(hookMineExplosionPower, hookMineCosmetic, hookMineBreakBlocks, hookMineFireBlocks, hookAllow);

        // all mines configuration
        MineConfiguration mineConfiguration = new MineConfiguration(groundMineConfiguration, hookMineConfiguration);

        return new Configuration(databaseConfiguration, mineConfiguration);
    }

    private void initCommandExecutor(String commandName, CommandExecutor commandExecutor, TabCompleter tabCompleter) {
        PluginCommand pluginCommand = getCommand(commandName);
        if(pluginCommand != null) {
            pluginCommand.setExecutor(commandExecutor);
            pluginCommand.setTabCompleter(tabCompleter);
        }
    }
}
