package org.trueaim;

import org.joml.Vector3f;
import org.trueaim.entities.targets.Target;
import org.trueaim.entities.weapons.AK47;
import org.trueaim.entities.weapons.V9S;
import org.trueaim.entities.weapons.GenericWeapon;
import org.trueaim.input.InputManager;
import org.trueaim.rendering.GUI.StatGUI;
import org.trueaim.rendering.OverlayRenderer;
import org.trueaim.rendering.Renderer;
import org.trueaim.entities.targets.TargetManager;
import org.trueaim.sound.SoundPlayer;
import org.trueaim.stats.StatTracker;
import org.trueaim.strahlwerfen.HeatmapCheck;
import org.trueaim.strahlwerfen.Raycasting;

import static org.lwjgl.opengl.GL11.*;

/**
 * Hauptsteuerungsklasse der Spielengine.
 * Koordiniert:
 * - Hauptspielschleife
 * - Eingabeverarbeitung
 * - Spielzustandsaktualisierung
 * - Rendering-Pipeline
 */
public class GameEngine {
    private final Window window;            // Fensterinstanz
    private final InputManager inputManager; // Eingabemanager
    private final Camera camera;            // Spielkamera
    private final Renderer renderer;        // 3D-Renderer
    private final OverlayRenderer overlayRenderer; // UI-Renderer
    private final TargetManager targetManager;     // Zielmanager
    private final Raycasting raycaster;     // Treffererkennung
    private final SoundPlayer soundPlayer; // Sound-Manager für Spielereignisse
    private GenericWeapon weapon;              // Spielerwaffe
    private final StatGUI statGUI; // GUI-Panel für Statistiken
    private final HeatmapCheck heatmapCheck; // Heatmap-Datensammlung
    private int AKbulletCount = 30; // Standard-Munitionsanzahl für AK-47
    private int V9SbulletCount = 12; // Standard-Munitionsanzahl für V9S

    public GameEngine(Window window, InputManager inputManager, Camera camera) {
        this.window = window;
        this.inputManager = inputManager;
        this.camera = camera;
        this.renderer = new Renderer(window.getWidth(), window.getHeight()); // 3D-Renderer
        this.soundPlayer = new SoundPlayer(); // Sound-Manager initialisieren
        this.weapon = new AK47(camera, renderer, soundPlayer);  // Waffe erstellen
        this.heatmapCheck = new HeatmapCheck(); // Heatmap-Datensammlung initialisieren
        this.targetManager = new TargetManager();  // Ziele initialisieren
        this.raycaster = new Raycasting(targetManager, weapon.getStats(), heatmapCheck); // Trefferprüfung
        this.overlayRenderer = new OverlayRenderer(weapon.getStats(), window); // UI-Renderer


        // Eingabecallbacks registrieren
        inputManager.addLeftClickCallback(this::handleShoot);  // Linksklick: Schießen
        inputManager.addRightClickCallback(weapon::onRightPress); // Rechtsklick: Zielfernrohr
        inputManager.addRkeyCallback(this::handleReload);      //R-Taste: Nachladen
        inputManager.addLeftReleaseCallback(weapon::onLeftRelease); // Linke Maustaste loslassen: Waffe abfeuern
        inputManager.addRightReleaseCallback(weapon::onRightRelease); // Rechte Maustaste loslassen: Zielfernrohr deaktivieren

        inputManager.bindSetWeaponCallbacks(this::setWeaponAK47, this::setWeaponV9S); // Waffenwechsel-Callbacks

        overlayRenderer.getIngameHUD().setEquippedWeapon(weapon);

        this.statGUI = new StatGUI(window, weapon.getStats(), overlayRenderer, targetManager, inputManager, renderer); // Statistik-UI initialisieren
        inputManager.setStatGUI(statGUI); // Eingabemanager mit Statistik-UI verbinden

    }

    public StatTracker getStatTracker() {
        return weapon.getStats();
    }

    public void setWeapon(GenericWeapon weapon) {
        if (this.weapon.getClass().equals(weapon.getClass())) {
            return; // Keine Änderung, wenn die Waffe bereits aktiv ist
        }
        // Callbacks entfernen, um Konflikte zu vermeiden
        inputManager.clearRightClickCallbacks();
        inputManager.clearLeftReleaseCallbacks();
        inputManager.clearRightReleaseCallbacks();

        StatTracker tracker = getStatTracker();
        weapon.setStats(tracker); // Überträgt die Statistiken der aktuellen Waffe auf die neue Waffe
        weapon.setRecoil(this.weapon.hasRecoil()); // Setzt den Rückstoßstatus der neuen Waffe
        weapon.setActive(this.weapon.isActive()); // Setzt den Aktivierungsstatus der neuen Waffe
        weapon.setHasInfiniteAmmo(this.weapon.hasInfiniteAmmo()); // Setzt den unendliche Munition Status der neuen Waffe
        inputManager.addRightClickCallback(weapon::onRightPress); // Rechtsklick: Zielfernrohr
        inputManager.addRkeyCallback(weapon::Reload);      //R-Taste: Nachladen
        inputManager.addLeftReleaseCallback(weapon::onLeftRelease); // Linke Maustaste loslassen: Waffe abfeuern
        inputManager.addRightReleaseCallback(weapon::onRightRelease); // Rechte Maustaste loslassen: Zielfernrohr deaktivieren
        this.weapon = weapon; // Setzt die Statistiken der neuen Waffe
        this.overlayRenderer.getIngameHUD().setEquippedWeapon(weapon); // Aktualisiert die HUD-Waffe
        // this.statGUI.setStatTracker(tracker); // Aktualisiert die Statistik-UI

        if (weapon instanceof AK47) {
            weapon.setBulletCount(AKbulletCount); // Setzt die Munitionsanzahl der AK47
        } else if (weapon instanceof V9S) {
            weapon.setBulletCount(V9SbulletCount); // Setzt die Munitionsanzahl der V9S
        }
    }

    /**
     * Verarbeitet Schussereignisse, wenn Munition im Magazin
     * 1. Startet Trefferüberprüfung
     * 2. Aktiviert Waffeneffekte
     *
     * Ist besser das Raycasting hier statt in der Waffenklasse zu starten, da man die
     * Raycasting Klasse mit dem momentanen Klassenaufbau dort nicht initialisieren kann
     */
    private void handleShoot() {
        if (statGUI.isVisible()) {
            soundPlayer.play(SoundPlayer.UI_CLICK);
        }
        if (weapon.hasAmmo()){
            raycaster.checkHit(camera.getPosition(), camera.getFront()); // Trefferüberprüfung
            weapon.onLeftPress();  // Waffenlogik aktivieren
            overlayRenderer.getIngameHUD().applyRecoilVector(weapon.getRecoil()); // Recoil auf HUD anwenden
        }
        if (!weapon.hasAmmo() && !statGUI.isVisible()) {
            soundPlayer.play(SoundPlayer.GUN_EMPTY); // Leeren Schuss-Sound abspielen
        }
    }

    /**
     * Verarbeitet Nachladeereignisse.
     */
    private void handleReload() {
        weapon.Reload();
    }


    /**
     * Fortlaufende Schussverarbeitung bei Vollautomatikwaffen.
     * Prüft ob die Waffe schießen darf und ob Munition vorhanden ist.
     */
    private void continueHandleShoot() {
        if (weapon.hasAmmo() && weapon.isFullAuto() && weapon.allowedToShoot() && weapon.wantsToShoot()) {
            handleShoot();
        }
    }

    /**
     * Hauptspielschleife.
     * Führt pro Frame aus:
     * 1. Eingabe- und Spielzustandsaktualisierung
     * 2. Rendering
     * 3. Fensteraktualisierung
     */
    public void run() {
        // Hauptschleife (läuft bis Fenster geschlossen wird)
        while (!window.shouldClose()) {
            float deltaTime = window.getDeltaTime(); // Zeit seit letztem Frame

            // Spielzustand aktualisieren
            inputManager.update(deltaTime);   // Eingaben verarbeiten
            targetManager.update(deltaTime);  // Ziele aktualisieren
            continueHandleShoot(); // Fortlaufendes Schießen bei Vollautomatik

            // Waffe deaktivieren, falls im Menü
            if (statGUI.isVisible() && weapon.isActive()) {
                weapon.setActive(false); // Waffe deaktivieren, wenn Statistik-UI sichtbar ist
            } else if (!statGUI.isVisible() && !weapon.isActive()) {
                weapon.setRecoil(statGUI.getGunHasRecoil()); // Waffenstatus basierend auf Recoil-Einstellung der Statistik-UI setzen
                weapon.setHasInfiniteAmmo(statGUI.hasInfiniteAmmo()); // Unendliche Munition basierend auf Statistik-UI-Einstellung setzen
                weapon.setActive(true); // Waffe aktivieren, wenn Statistik-UI nicht sichtbar ist
            }

            // Ziele zurücksetzen, wenn alle getroffen
            if (allTargetsHit()) {
                targetManager.resetAll();
            }

            // Rendering durchführen
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT); // Puffer löschen
            renderer.render(camera, targetManager);  // 3D-Szene rendern
            overlayRenderer.render(window);                // UI rendern
            statGUI.render(window);                  // Escape-UI /Statistik-UI rendern


            window.update(); // Frame abschließen
        }

        // Nach Spielende
        window.cleanup();       // Ressourcen freigeben
        statGUI.cleanup();      // Statistik-UI aufräumen
        overlayRenderer.cleanup(); // UI-Renderer aufräumen
        soundPlayer.cleanup(); // Sound-Manager aufräumen
    }

    /**
     * Prüft ob alle Ziele getroffen wurden.
     * @return true wenn kein Ziel mehr aktiv ist
     */
    private boolean allTargetsHit() {
        for (Target target : targetManager.getTargets()) {
            if (!target.isHit()) return false;
        }
        return true;
    }

    public void setWeaponAK47() {
        V9SbulletCount = weapon.getBulletCount(); // Speichert die aktuelle V9S-Munitionsanzahl
        setWeapon(new AK47(camera, renderer, soundPlayer)); // Setzt die Waffe auf AK47
    }

    public void setWeaponV9S() {
        if (weapon instanceof V9S) {
            return; // Keine Änderung, wenn die Waffe bereits V9S ist
        }
        AKbulletCount = weapon.getBulletCount(); // Speichert die aktuelle AK47-Munitionsanzahl
        setWeapon(new V9S(camera, renderer, soundPlayer)); // Setzt die Waffe auf V9S
    }

}