package me.salatosik.hiddenminesplugin.core.database.interfaces;

import me.salatosik.hiddenminesplugin.core.database.models.Mine;

import java.util.List;

public interface DatabaseListener {
    void onMineAdd(Mine mine);
    void onMineRemove(Mine mine);
    void onListenerAdded(List<Mine> mines);
    void onMineRemoveList(List<Mine> removedMines);
}
