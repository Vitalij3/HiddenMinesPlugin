package me.salatosik.hiddenminesplugin.utils.configuration.mine;

import me.salatosik.hiddenminesplugin.utils.configuration.mine.ground.GroundCfg;
import me.salatosik.hiddenminesplugin.utils.configuration.mine.hook.HookCfg;

public class MineConfiguration {
    private final GroundCfg groundCfg;
    private final HookCfg hookCfg;

    public MineConfiguration(GroundCfg groundCfg, HookCfg hookCfg) {
        this.groundCfg = groundCfg;
        this.hookCfg = hookCfg;
    }

    public GroundCfg getGround() {
        return groundCfg;
    }

    public HookCfg getHook() {
        return hookCfg;
    }
}
