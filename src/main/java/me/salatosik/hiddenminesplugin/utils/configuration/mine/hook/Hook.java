package me.salatosik.hiddenminesplugin.utils.configuration.mine.hook;

public class Hook {
    private final double explosionPower;
    private final boolean cosmetic;

    public Hook(double explosionPower, boolean cosmetic) {
        this.explosionPower = explosionPower;
        this.cosmetic = cosmetic;
    }

    public double getExplosionPower() {
        return explosionPower;
    }

    public boolean getCosmetic() {
        return cosmetic;
    }
}
