package me.salatosik.hiddenminesplugin.utils.configuration.mine.hook;

public class HookCfg {
    private final double explosionPower;
    private final boolean cosmetic;
    private final boolean breakBlocks;
    private final boolean fireBlocks;
    private final boolean allow;

    public HookCfg(double explosionPower, boolean cosmetic, boolean breakBlocks, boolean fireBlocks, boolean allow) {
        this.explosionPower = explosionPower;
        this.cosmetic = cosmetic;
        this.breakBlocks = breakBlocks;
        this.fireBlocks = fireBlocks;
        this.allow = allow;
    }

    public double getExplosionPower() {
        return explosionPower;
    }

    public boolean getCosmetic() {
        return cosmetic;
    }

    public boolean getBreakBlocks() {
        return breakBlocks;
    }

    public boolean getFireBlocks() {
        return fireBlocks;
    }

    public boolean isAllow() {
        return allow;
    }
}
