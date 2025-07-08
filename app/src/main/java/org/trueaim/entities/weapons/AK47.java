package org.trueaim.entities.weapons;
import lombok.Getter;
import org.joml.Vector2f;
import org.trueaim.Camera;
import org.trueaim.rendering.Renderer;

/**
 * Spezifische Implementierung der AK-47-Waffe.
 * Erweitert GenericWeapon mit rückstoßbasierter Mechanik.
 */
public class AK47 extends GenericWeapon {
    private int consecutiveShots = 0; // Anzahl aufeinanderfolgender Schüsse
    private long lastShotTime = 0;    // Zeitpunkt des letzten Schusses
    private final Camera camera;      // Referenz zur Spielkamera
    //TODO renderer entfernen, falls nicht für zoom benötigt
    private final Renderer renderer;  //Referenz zu Renderer (für FOV Change)
    protected int ammo = 30;             // Magazingröße
    private int bulletCount = 30;           // Aktuelle Munitionsanzahl
    private boolean active = true;      // Aktiviert/Deaktiviert Schießen
    private boolean fullAuto = true;    // Vollautomatischer Modus
                                        // TODO: später vllt Single Shot Modus hinzufügen oder andere Waffe mit Single Shot Modus hinzufügen

    public AK47(Camera camera, Renderer renderer) {
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
    }

    @Override
    public boolean isFullAuto() {
        return fullAuto;
    }
    @Override
    public void setFullAuto(boolean fullAuto) {
        this.fullAuto = fullAuto;
    }

    /**
     * Überprüft ob Munition in der Waffe ist
     */
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
        if (!active) {
            return; // Waffe ist deaktiviert, Nachladen nicht möglich
        }
        bulletCount = ammo; // Setzt die Munition auf das Maximum zurück
        consecutiveShots = 0; // Schusskette zurücksetzen
        lastShotTime = 0; // Letzten Schusszeitpunkt zurücksetzen
        stats.registerReload(); // Statistik aktualisieren
        System.out.println("Tut Tut, Wir haben nachgeladen, Tut Tut");
        System.out.println("das ist obviously ne Testnachricht, nicht vergessen zu entfernen xd");
    }

    //Schusseffekte (Raycasting für Schuss / Ammo überprüfung wird in GameEngine gestartet)
    @Override
    public void onLeftPress() {
        if (!active) {
            return; // Waffe ist deaktiviert
        }
        applyRecoil();        // Rückstoß anwenden
        bulletCount--;              // Munition verringern
        stats.incrementShotsFired(); // Statistik aktualisieren
        consecutiveShots++;   // Schusszähler erhöhen
        lastShotTime = System.currentTimeMillis(); // Zeit speichern
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
        renderer.setFOV(40);
    }
}