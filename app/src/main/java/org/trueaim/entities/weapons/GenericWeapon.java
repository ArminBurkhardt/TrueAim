package org.trueaim.entities.weapons;
import lombok.Getter;
import org.joml.Vector2f;
import org.trueaim.shootable.Shootable;
import org.trueaim.stats.StatTracker;

//TODO Waffen laden nach wenn man sie wechselt, sollte obviously nicht so sein

/**
 * Basisklasse für alle Waffen.
 * Implementiert grundlegende Schussfunktionalität.
 */

//TODO Weapons komplett überarbieten (maybe shootable löschen)
public abstract class GenericWeapon implements Shootable {
    protected StatTracker stats = new StatTracker(); // Waffenstatistik
    protected int ammo;                // Magazingröße
    private int bulletCount;           // Aktuelle Munitionsanzahl
    private boolean active;
    private boolean fullAuto; // Vollautomatischer Modus
    private boolean hasRecoil = true; // Standard: Waffe hat Rückstoß
    private boolean hasInfiniteAmmo = false; // Standard: Waffe hat keine unendliche Munition

    @Override
    public abstract void onLeftPress();

    @Override
    public void onRightPress() {
        // Standard: Zielfernrohr aktivieren
        System.out.println("ADS activated");
    }

    public void onRightRelease() {}
    public void onLeftRelease() {}

    public abstract boolean hasAmmo();

    // Zugriffsmethoden
    public StatTracker getStats() { return stats; }
    public void setStats(StatTracker tracker) {
        this.stats = tracker; // Setzt die Statistiken der Waffe
    };

    public abstract int getAmmo();
    public abstract int getBulletCount();
    public abstract void Reload();
    public abstract boolean isActive();
    public abstract void setActive(boolean active);
    public abstract boolean isFullAuto();
    public abstract void setFullAuto(boolean fullAuto);
    public abstract boolean hasRecoil();
    public abstract void setRecoil(boolean hasRecoil);
    public abstract Vector2f getRecoil(); // Getter für Rückstoß
    public abstract boolean allowedToShoot();
    public abstract boolean wantsToShoot();
    public abstract void setHasInfiniteAmmo(boolean hasInfiniteAmmo);
    public abstract boolean hasInfiniteAmmo();

}