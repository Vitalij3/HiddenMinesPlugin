package me.salatosik.hiddenminesplugin.event.command.client;

import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.event.command.BaseCommandExecutor;
import me.salatosik.hiddenminesplugin.utils.configuration.Configuration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class BaseClientExecutor extends BaseCommandExecutor {
    protected BaseClientExecutor(JavaPlugin plugin, Database database, Configuration configuration) {
        super(plugin, database, configuration);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player) onCommand((Player) sender, command, args);
        return true;
    }

    abstract void onCommand(@NotNull Player player, @NotNull Command command, @NotNull String[] args);
}
