package me.salatosik.hiddenminesplugin.event.command.client;

import me.salatosik.hiddenminesplugin.UtilMethods;
import me.salatosik.hiddenminesplugin.core.database.Database;
import me.salatosik.hiddenminesplugin.core.database.models.Mine;
import me.salatosik.hiddenminesplugin.core.MineType;
import me.salatosik.hiddenminesplugin.utils.configuration.Configuration;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RemoveMinesExecutor extends BaseClientExecutor implements TabCompleter {
    public RemoveMinesExecutor(JavaPlugin plugin, Database database, Configuration configuration) {
        super(plugin, database, configuration);
    }

    @Override
    void onCommand(@NotNull Player player, @NotNull Command command, @NotNull String[] args) {
        if(args.length != 2 && args.length != 4 && args.length != 3) return;
        int radius, max;
        boolean detonate;
        MineType type;

        try {
            if(!args[0].equalsIgnoreCase("true") && !args[0].equalsIgnoreCase("false")) throw new Exception();
            else detonate = Boolean.parseBoolean(args[0]);

            radius = Integer.parseInt(args[1]);

            try { type = MineType.valueOf(args[2].toUpperCase()); }
            catch(ArrayIndexOutOfBoundsException ex) { type = MineType.EMPTY; }

            try { max = Integer.parseInt(args[3]); }
            catch(ArrayIndexOutOfBoundsException ex) { max = Integer.MAX_VALUE; }

        } catch(Exception exception) {
            player.sendMessage(ChatColor.DARK_RED + "Invalid values!");
            return;
        }

        if(max <= 0) {
            player.sendMessage(ChatColor.DARK_RED + "Max value not allowed less of 0!");
            return;
        }

        if(radius <= 0) {
            player.sendMessage(ChatColor.DARK_RED + "Radius value not allowed less 0!");
            return;
        }

        Vector playerVector = player.getLocation().toVector();
        World playerWorld = player.getWorld();
        List<Mine> removeList = new ArrayList<>();

        for(int i = 0, count = 0; i < minesFromDatabase.size(); i++) {
            if(count >= max) break;
            Mine mine = minesFromDatabase.get(i);

            if(mine.worldType != playerWorld.getEnvironment()) continue;
            if(type != MineType.EMPTY) if(mine.mineType != type) continue;

            Vector mineVector = new Vector(mine.x, mine.y, mine.z);
            double distance = playerVector.distance(mineVector);

            if(distance <= radius) {
                removeList.add(mine);
                count++;
            }
        }

        if(!removeList.isEmpty()) {
            UtilMethods.createBukkitAsyncThreadAndStart(plugin, () -> {
                if(detonate) {
                    List<Mine> detonateMines = new LinkedList<>();
                    UtilMethods.removeMinesFromDatabase(removeList, database, logger, detonateMines::add);

                    UtilMethods.createBukkitThreadAndStart(plugin, () -> {
                        detonateMines.forEach((mine) -> {
                            switch(mine.mineType) {
                                case HOOK:
                                    Location hookMineLocation = mine.toLocation(playerWorld);
                                    playerWorld.createExplosion(
                                            hookMineLocation.getX(),
                                            hookMineLocation.getY(),
                                            hookMineLocation.getZ(),
                                            (float) configuration.getMineConfiguration().getHook().getExplosionPower(),
                                            configuration.getMineConfiguration().getHook().getFireBlocks(),
                                            configuration.getMineConfiguration().getHook().getBreakBlocks()
                                    );
                                    break;

                                case GROUND:
                                    Location groundMineLocation = mine.toLocation(playerWorld);
                                    playerWorld.createExplosion(
                                            groundMineLocation.getX(),
                                            groundMineLocation.getY(),
                                            groundMineLocation.getZ(),
                                            (float) configuration.getMineConfiguration().getGround().getExplosionPower(),
                                            configuration.getMineConfiguration().getGround().getFireBlocks(),
                                            configuration.getMineConfiguration().getGround().getBreakBlocks()
                                    );
                                    break;
                            }
                        });
                    });

                } else UtilMethods.removeMinesFromDatabase(removeList, database, logger);

                player.sendMessage(ChatColor.GREEN + String.valueOf(removeList.size()) + " mines removed!");
            });

        } else player.sendMessage(ChatColor.DARK_RED + "Mines not found!");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        if(!(sender instanceof Player)) return list;
        Player player = (Player) sender;

        switch(args.length) {
            case 1:
                list.add("true");
                list.add("false");
                break;

            case 2:
                list.add(String.valueOf(Integer.MAX_VALUE));
                break;

            case 3:
                for(MineType mineType: MineType.values()) list.add(mineType.name().toLowerCase());
                break;

            case 4:
                int radius;
                MineType type;

                try { radius = Integer.parseInt(args[1]); }
                catch(NumberFormatException | IndexOutOfBoundsException exception) { return list; }

                try {
                    String mineNameFromArgs = args[2].toUpperCase();
                    type = MineType.valueOf(mineNameFromArgs);
                } catch(IndexOutOfBoundsException indexOutOfBoundsException) {
                    return list;
                } catch(IllegalArgumentException illegalArgumentException) {
                    type = MineType.EMPTY;
                }

                Vector playerVector = player.getLocation().toVector();
                World playerWorld = player.getLocation().getWorld();
                int count = 0;

                for(Mine mine: minesFromDatabase) {
                    if(mine.worldType != playerWorld.getEnvironment()) continue;
                    if(type != MineType.EMPTY) if(type != mine.mineType) continue;
                    Vector mineVector = new Vector(mine.x, mine.y, mine.z);
                    double distance = playerVector.distance(mineVector);
                    if (distance <= radius) count++;
                }

                list.add(String.valueOf(count));
                break;
        }

        return list;
    }
}
