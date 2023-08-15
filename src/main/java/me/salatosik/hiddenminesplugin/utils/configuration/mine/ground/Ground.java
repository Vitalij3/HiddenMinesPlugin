package me.salatosik.hiddenminesplugin.utils.configuration.mine.ground;

public class Ground {
    private final double explosionPower;
    private final boolean cosmetic;
    private final boolean adaptiveCosmetic;

    public Ground(double explosionPower, boolean cosmetic, boolean adaptiveCosmetic) {
        this.explosionPower = explosionPower;
        this.cosmetic = cosmetic;
        this.adaptiveCosmetic = adaptiveCosmetic;
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
}
