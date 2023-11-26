package salatosik.hiddenmines.core;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import salatosik.hiddenmines.HiddenMines;
import salatosik.hiddenmines.core.database.Database;
import salatosik.hiddenmines.core.mine.Mine;
import salatosik.hiddenmines.core.mine.MineType;

import static salatosik.hiddenmines.core.MineProtocolManager.*;

import java.io.File;
import java.util.List;

public class MineManager {
    private final List<Mine> mines;

    public MineManager(Database database) {
        mines = database.getPreviousSavedMines();

        HiddenMines.registerListener(new Listener() {
            @EventHandler(priority = EventPriority.HIGH)
            public void onPluginDisable(PluginDisableEvent event) {
                database.saveAll(mines);
                database.closeConnection();
            }
        });
    }

    public MineManager(File databaseFile) {
        this(new Database(databaseFile));
    }

    public MineManager(String databaseFilename) {
        this(new Database(databaseFilename));
    }

    public void add(Mine mine) {
        mines.add(mine);
        showMine(mine);
    }

    public boolean remove(Mine mine) {
        if(mines.remove(mine)) {
            hideMine(mine);
            return true;
        }

        return false;
    }

    public boolean remove(Location location) {
        for(Mine mine: mines) {
            if(mine.locationEqualsAccuracy(location)) {
                return remove(mine);
            }
        }

        return false;
    }

    public boolean contains(Mine mine) {
        return mines.contains(mine);
    }

    public boolean contains(Location location) {
        for(Mine mine: mines) {
            if(mine.locationEqualsAccuracy(location)) {
                return true;
            }
        }

        return false;
    }

    public boolean contains(Location location, MineType mineType) {
        for(Mine mine: mines) {
            if(mine.locationEqualsAccuracy(location) && mine.getMineType() == mineType) {
                return true;
            }
        }

        return false;
    }

    public void showMinesFor(Player player) {
        mines.forEach((mine) -> MineProtocolManager.showMine(mine, player));
    }

    public MineType getMineTypeByLocation(Location location) {
        for(Mine mine: mines) {
            if(mine.locationEqualsAccuracy(location)) {
                return mine.getMineType();
            }
        }

        return null;
    }
}
