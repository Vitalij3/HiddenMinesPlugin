package me.salatosik.hiddenminesplugin.utils.configuration.mine.ground;

public class Ground {
    public final double explosionPower;
    public final boolean cosmetic;
    public final boolean adaptiveCosmetic;

    public Ground(double explosionPower, boolean cosmetic, boolean adaptiveCosmetic) {
        this.explosionPower = explosionPower;
        this.cosmetic = cosmetic;
        this.adaptiveCosmetic = adaptiveCosmetic;
    }
}
