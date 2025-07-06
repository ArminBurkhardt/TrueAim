package org.trueaim.entities.weapons;
import lombok.Getter;
import org.trueaim.shootable.Shootable;
import org.trueaim.stats.StatTracker;

/**
 * Basisklasse für alle Waffen.
 * Implementiert grundlegende Schussfunktionalität.
 */

//TODO Weapons komplett überarbieten (maybe shootable löschen)
public abstract class GenericWeapon implements Shootable {
    protected StatTracker stats = new StatTracker(); // Waffenstatistik
    protected int ammo;                // Magazingröße
    private int bulletCount;           // Aktuelle Munitionsanzahl

    @Override
    public abstract void onLeftPress();

    @Override
    public void onRightPress() {
        // Standard: Zielfernrohr aktivieren
        System.out.println("ADS activated");
    }

    public abstract boolean hasAmmo();

    // Zugriffsmethoden
    public StatTracker getStats() { return stats; }

    public abstract int getAmmo();
    public abstract int getBulletCount();
}