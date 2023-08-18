package me.salatosik.hiddenminesplugin.utils.configuration.mine.hook;

public class Hook {
    private final double explosionPower;
    private final boolean cosmetic;
    private final boolean breakBlocks;
    private final boolean fireBlocks;

    public Hook(double explosionPower, boolean cosmetic, boolean breakBlocks, boolean fireBlocks) {
        this.explosionPower = explosionPower;
        this.cosmetic = cosmetic;
        this.breakBlocks = breakBlocks;
        this.fireBlocks = fireBlocks;
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
}
