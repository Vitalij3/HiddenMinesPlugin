package me.salatosik.hiddenminesplugin.utils.configuration.mine.ground;

public class Ground {
    private final double explosionPower;
    private final boolean cosmetic;
    private final boolean adaptiveCosmetic;
    private final boolean breakBlocks;
    private final boolean fireBlocks;

    public Ground(double explosionPower, boolean cosmetic, boolean adaptiveCosmetic, boolean breakBlocks, boolean fireBlocks) {
        this.explosionPower = explosionPower;
        this.cosmetic = cosmetic;
        this.adaptiveCosmetic = adaptiveCosmetic;
        this.breakBlocks = breakBlocks;
        this.fireBlocks = fireBlocks;
    }

     public double getExplosionPower() {
        return explosionPower;
     }

     public boolean getCosmetic() {
        return cosmetic;
     }

     public boolean getAdaptiveCosmetic() {
        return adaptiveCosmetic;
     }

     public boolean getBreakBlocks() {
        return breakBlocks;
     }

     public boolean getFireBlocks() {
        return fireBlocks;
     }
}
