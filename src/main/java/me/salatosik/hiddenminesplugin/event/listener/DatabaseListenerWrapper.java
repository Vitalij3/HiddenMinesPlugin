package me.salatosik.hiddenminesplugin.event.listener;

import me.salatosik.hiddenminesplugin.core.database.DatabaseListener;
import me.salatosik.hiddenminesplugin.core.database.models.DatabaseObject;
import me.salatosik.hiddenminesplugin.core.database.models.mine.Mine;

import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

public class DatabaseListenerWrapper<T extends DatabaseObject> implements DatabaseListener<T> {
    private final LinkedBlockingDeque<T> staticObjects;

    private DatabaseListenerWrapper(LinkedBlockingDeque<T> staticList) {
        this.staticObjects = staticList;
    }

    protected LinkedBlockingDeque<T> getDatabaseObjects() {
        return staticObjects;
    }

    @Override
    public void onItemAdd(T item) {
        staticObjects.add(item);
    }

    @Override
    public void onItemRemove(T item) {
        staticObjects.remove(item);
    }

    @Override
    public void onItemRemoveList(List<T> items) {
        staticObjects.removeAll(items);
    }

    private boolean isFirstListenerInit = true;

    @Override
    public void onListenerAdded(List<T> items) {
        if(isFirstListenerInit) {
            isFirstListenerInit = false;
            staticObjects.addAll(items);
        }
    }

    public static class MineDatabaseListener extends DatabaseListenerWrapper<Mine> {
        private static final LinkedBlockingDeque<Mine> minesFromDatabase = new LinkedBlockingDeque<>();

        public MineDatabaseListener() {
            super(minesFromDatabase);
        }
    }
}
