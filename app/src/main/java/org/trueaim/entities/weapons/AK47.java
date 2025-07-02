package org.trueaim.entities.weapons;
import org.joml.Vector2f;
import org.trueaim.Camera;

/**
 * Spezifische Implementierung der AK-47-Waffe.
 * Erweitert GenericWeapon mit rückstoßbasierter Mechanik.
 */
public class AK47 extends GenericWeapon {
    private int consecutiveShots = 0; // Anzahl aufeinanderfolgender Schüsse
    private long lastShotTime = 0;    // Zeitpunkt des letzten Schusses
    private final Camera camera;      // Referenz zur Spielkamera

    public AK47(Camera camera) {
        this.camera = camera;
    }

    @Override
    public void onLeftPress() {
        // Schuss abfeuern wenn Munition vorhanden
        if (ammo > 0) {
            applyRecoil();        // Rückstoß anwenden
            ammo--;              // Munition verringern
            stats.incrementShotsFired(); // Statistik aktualisieren
            consecutiveShots++;   // Schusszähler erhöhen
            lastShotTime = System.currentTimeMillis(); // Zeit speichern
        }
    }

    /**
     * Wendet den Rückstoß auf die Kamera an.
     */
    private void applyRecoil() {
        // Rückstoß für aktuellen Schuss holen
        Vector2f recoil = RecoilPattern.getRecoil(consecutiveShots);

        // Rückstoß anwenden:
        // recoil.x: Horizontaler Rückstoß (Yaw)
        // recoil.y: Vertikaler Rückstoß (Pitch)
        camera.rotate(recoil.x, recoil.y);
    }

    @Override
    public void onRightPress() {
        super.onRightPress();
        consecutiveShots = 0; // Zielen unterbricht Schusskette
    }
}