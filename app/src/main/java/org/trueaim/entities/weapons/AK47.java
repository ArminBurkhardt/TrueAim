package org.trueaim.entities.weapons;
import org.joml.Vector2f;
import org.trueaim.Camera;
import org.trueaim.rendering.Renderer;
import org.trueaim.sound.SoundPlayer;

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
    protected SoundPlayer soundPlayer; // Sound-Manager für Schussgeräusche
    private int bulletCount = 30;           // Aktuelle Munitionsanzahl
    private boolean active = true;      // Aktiviert/Deaktiviert Schießen
    private boolean fullAuto = true;    // Vollautomatischer Modus
                                        // TODO: später vllt Single Shot Modus hinzufügen oder andere Waffe mit Single Shot Modus hinzufügen
    private boolean hasRecoil = true; // Standard: Waffe hat Rückstoß
    private boolean pressed = false; // Flag für gedrückte Taste
    private int RPM = 600; // Schüsse pro Minute (Standard für AK-47), nur relevant im Vollautomatikmodus
    private boolean hasInfiniteAmmo = false; // Standard: Waffe hat keine unendliche Munition

    public AK47(Camera camera, Renderer renderer, SoundPlayer soundPlayer) {
        this.camera = camera;
        this.renderer = renderer;
        this.soundPlayer = soundPlayer;
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
    public void onRightRelease() {
        pressed = false; // Taste losgelassen
        renderer.setFOV(60); // FOV zurücksetzen
    }

    @Override
    public void onLeftRelease() {
        pressed = false; // Taste losgelassen
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
        if (!active || hasInfiniteAmmo) {
            return; // Waffe ist deaktiviert, Nachladen nicht möglich
        }
        bulletCount = ammo; // Setzt die Munition auf das Maximum zurück
        consecutiveShots = 0; // Schusskette zurücksetzen
        lastShotTime = 0; // Letzten Schusszeitpunkt zurücksetzen
        stats.registerReload(); // Statistik aktualisieren
        soundPlayer.play(SoundPlayer.RELOAD); // Nachlade-Sound abspielen
        System.out.println("Tut Tut, Wir haben nachgeladen, Tut Tut");
        System.out.println("das ist obviously ne Testnachricht, nicht vergessen zu entfernen xd");
    }

    //Schusseffekte (Raycasting für Schuss / Ammo überprüfung wird in GameEngine gestartet)
    @Override
    public void onLeftPress() {
        if (!active) {
            return; // Waffe ist deaktiviert
        }
        pressed = true;
        if (!allowedToShoot()) {
            return; // Nicht genug Zeit seit dem letzten Schuss
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
        soundPlayer.play(SoundPlayer.SHOOT); // Schuss-Sound abspielen
    }

    @Override
    public boolean allowedToShoot() {
        // Berechnet die Zeit seit dem letzten Schuss
        long currentTime = System.currentTimeMillis();
        long timeSinceLastShot = currentTime - lastShotTime;

        // Berechnet die minimale Zeit zwischen Schüssen (in Millisekunden)
        long minTimeBetweenShots = 60000 / RPM; // RPM in RPS umrechnen

        // Prüft, ob genug Zeit seit dem letzten Schuss vergangen ist
        return timeSinceLastShot >= minTimeBetweenShots;
    }

    @Override
    public boolean wantsToShoot() {
        // Prüft, ob die linke Maustaste gedrückt ist und die Waffe aktiv ist
        return pressed && active;
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
    public Vector2f getRecoil() {
        // Gibt den aktuellen Rückstoßwert zurück
        if (!hasRecoil) {
            return new Vector2f(0, 0); // Kein Rückstoß
        }
        return RecoilPattern.getRecoil(consecutiveShots);
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
    public void onRightPress() {
        super.onRightPress();
        consecutiveShots = 0; // Zielen unterbricht Schusskette
        renderer.setFOV(40);
    }
}