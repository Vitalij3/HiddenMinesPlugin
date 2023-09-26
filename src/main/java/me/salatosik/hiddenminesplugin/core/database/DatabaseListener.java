package me.salatosik.hiddenminesplugin.core.database;

import java.util.List;

public interface DatabaseListener <T> {
    void onItemAdd(T item);
    void onItemRemove(T item);
    void onItemRemoveList(List<T> items);
    void onListenerAdded(List<T> items);
}
