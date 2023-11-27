package salatosik.hiddenmines;

import com.j256.ormlite.logger.Level;
import lombok.SneakyThrows;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import salatosik.hiddenmines.configuration.PluginConfiguration;
import salatosik.hiddenmines.core.MineManager;
import salatosik.hiddenmines.listener.mine.MineListener;

import java.io.File;

public final class HiddenMines extends JavaPlugin {

    @Override
    @SneakyThrows
    public void onEnable() {
        com.j256.ormlite.logger.Logger.setGlobalLogLevel(Level.ERROR);

        saveDefaultConfig();
        PluginConfiguration configuration = new PluginConfiguration(getConfig());

        MineManager mineManager = new MineManager(new File(getDataFolder().getAbsoluteFile(), configuration.getDatabaseFilename()).getAbsoluteFile(), configuration.getDatabaseSaveRate());
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new MineListener(configuration,mineManager), this);
    }

    private static JavaPlugin getJavaPlugin() {
        return JavaPlugin.getPlugin(HiddenMines.class);
    }

    public static void registerListener(Listener listener) {
        JavaPlugin javaPlugin = getJavaPlugin();
        javaPlugin.getServer().getPluginManager().registerEvents(listener, javaPlugin);
    }

    public static void registerRecipe(ShapedRecipe shapedRecipe) {
        getJavaPlugin().getServer().addRecipe(shapedRecipe);
    }

    public static void runTimerTask(BukkitRunnable bukkitRunnable, long period) {
        bukkitRunnable.runTaskTimer(getJavaPlugin(), 20, period);
    }

    public static void runTimeTaskAsynchronously(BukkitRunnable bukkitRunnable, long period) {
        bukkitRunnable.runTaskTimerAsynchronously(getJavaPlugin(), 20, period);
    }

    public static void runTimeTaskAsynchronously(Runnable runnable, long period) {
        runTimeTaskAsynchronously(new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }, period);
    }

    public static void runTaskLater(BukkitRunnable bukkitRunnable, long delay) {
        bukkitRunnable.runTaskLater(getJavaPlugin(), delay);
    }

    public static void runTaskLater(Runnable runnable, long delay) {
        runTaskLater(new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }, delay);
    }
}
