package me.salatosik.hiddenminesplugin.utils.configuration.mine;

import me.salatosik.hiddenminesplugin.utils.configuration.mine.ground.Ground;
import me.salatosik.hiddenminesplugin.utils.configuration.mine.hook.Hook;

public class MineConfiguration {
    public final Ground ground;
    public final Hook hook;

    public MineConfiguration(Ground ground, Hook hook) {
        this.ground = ground;
        this.hook = hook;
    }
}
