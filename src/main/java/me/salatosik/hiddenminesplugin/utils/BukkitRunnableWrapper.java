package me.salatosik.hiddenminesplugin.utils;

import org.bukkit.scheduler.BukkitRunnable;

public class BukkitRunnableWrapper extends BukkitRunnable {
    private final Runnable runnable;

    public BukkitRunnableWrapper(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void run() {
        runnable.run();
    }
}
