package org.trueaim.entities.weapons;
import org.trueaim.shootable.Shootable;
import org.trueaim.stats.StatTracker;

/**
 * Basisklasse für alle Waffen.
 * Implementiert grundlegende Schussfunktionalität.
 */

//TODO Weapons komplett überarbieten (maybe shootable löschen)
public abstract class GenericWeapon implements Shootable {
    protected StatTracker stats = new StatTracker(); // Waffenstatistik

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
}