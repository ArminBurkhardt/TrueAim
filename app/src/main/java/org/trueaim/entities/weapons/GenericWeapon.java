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
    protected int ammo = 30000000;                        // Magazingröße

    @Override
    public void onRightPress() {
        // Standard: Zielfernrohr aktivieren
        System.out.println("ADS activated");
    }

    // Zugriffsmethoden
    public StatTracker getStats() { return stats; }
}