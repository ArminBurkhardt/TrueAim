package org.trueaim.entities.weapons;
import org.joml.Vector2f;
import org.trueaim.Camera;
import org.trueaim.rendering.Renderer;

// TODO: REFEACTOR & CLEANUP

/**
 * Spezifische Implementierung der V9S-Waffe.
 * Erweitert GenericWeapon.
 */
public class V9S extends GenericWeapon {
    private int consecutiveShots = 0; // Anzahl aufeinanderfolgender Schüsse
    private long lastShotTime = 0;    // Zeitpunkt des letzten Schusses
    private final Camera camera;      // Referenz zur Spielkamera
    private final Renderer renderer;  //Referenz zu Renderer (für FOV Change)
    protected int ammo = 12;             // Magazingröße
    private int bulletCount = 12;           // Aktuelle Munitionsanzahl
    private boolean active = true;      // Aktiviert/Deaktiviert Schießen
    private boolean fullAuto = false;    // Single Shot Modus (Standard: Nein, da V9S eine Sniper ist)
    private boolean hasRecoil = true; // Standard: Waffe hat Rückstoß
    private boolean hasInfiniteAmmo = false; // Standard: Waffe hat keine unendliche Munition

    public V9S(Camera camera, Renderer renderer) {
        this.camera = camera;
        this.renderer = renderer;
    }

    @Override
    public int getAmmo() { return ammo; }
    @Override
    public int getBulletCount() { return bulletCount; }

    @Override
    public boolean isActive() {
        return this.active;
    }
    @Override
    public void setActive(boolean active) {
        this.active = active;
        this.stats.setEnabled(active);
    }

    @Override
    public boolean isFullAuto() {
        return fullAuto;
    }
    @Override
    public void setFullAuto(boolean fullAuto) {
        this.fullAuto = fullAuto;
    }

    @Override
    public boolean hasRecoil() {
        return hasRecoil;
    }
    @Override
    public void setRecoil(boolean hasRecoil) {
        this.hasRecoil = hasRecoil; // Rückstoß kann deaktiviert werden
    }

    @Override
    public boolean hasAmmo(){
        return bulletCount > 0;
    }

    /**
     * Lädt die Waffe nach.
     * Setzt die Munition auf das Maximum zurück.
     */
    @Override
    public void Reload(){
        if (!active || hasInfiniteAmmo) {
            return; // Waffe ist deaktiviert, Nachladen nicht möglich
        }
        bulletCount = ammo; // Setzt die Munition auf das Maximum zurück
        consecutiveShots = 0; // Schusskette zurücksetzen
        lastShotTime = 0; // Letzten Schusszeitpunkt zurücksetzen
        stats.registerReload(); // Statistik aktualisieren
    }

    //Schusseffekte (Raycasting für Schuss / Ammo überprüfung wird in GameEngine gestartet)
    @Override
    public void onLeftPress() {
        if (!active) {
            return; // Waffe ist deaktiviert
        }
        if (hasRecoil) {
            applyRecoil();        // Rückstoß anwenden
        }
        if (!hasInfiniteAmmo) {
            bulletCount--;              // Munition verringern
        }
        stats.incrementShotsFired(); // Statistik aktualisieren
        consecutiveShots++;   // Schusszähler erhöhen
        lastShotTime = System.currentTimeMillis(); // Zeit speichern
    }

    /**
     * Wendet den Rückstoß auf die Kamera an.
     */
    private void applyRecoil() {
        // Rückstoß für aktuellen Schuss holen
        // TODO: Anderes Recoil Pattern für V9S
        Vector2f recoil = RecoilPattern.getRecoil(consecutiveShots);

        // Rückstoß anwenden:
        // recoil.x: Horizontaler Rückstoß (Yaw)
        // recoil.y: Vertikaler Rückstoß (Pitch)
        camera.rotate(recoil.x, recoil.y);
    }

    @Override
    public Vector2f getRecoil() {
        // Gibt den Rückstoß für die aktuelle Schusskette zurück
        if (!hasRecoil) {
            return new Vector2f(0, 0); // Kein Rückstoß
        }
        return RecoilPattern.getRecoil(consecutiveShots);
    }

    @Override
    public void onRightPress() {
        // Kein ADS :(
    }

    @Override
    public void setHasInfiniteAmmo(boolean hasInfiniteAmmo) {
        this.hasInfiniteAmmo = hasInfiniteAmmo; // Setzt den Status für unendliche Munition
        if (hasInfiniteAmmo) {
            bulletCount = Integer.MAX_VALUE; // Setzt die Munitionsanzahl auf unendlich
        } else {
            bulletCount = ammo; // Setzt die Munitionsanzahl auf das Maximum zurück
        }
    }

    @Override
    public boolean hasInfiniteAmmo() {
        return hasInfiniteAmmo; // Gibt den Status für unendliche Munition zurück
    }

    @Override
    public boolean allowedToShoot() {
        // Keine RPM, kein Full Auto Modus => darf immer schießen
        return true;
    }

    @Override
    public boolean wantsToShoot() {
        // Prüft, ob die linke Maustaste gedrückt ist und die Waffe aktiv ist
        return active; // Nur schießen, wenn Munition vorhanden
    }

}