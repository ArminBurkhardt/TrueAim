package org.trueaim;

import org.trueaim.entities.targets.Target;
import org.trueaim.entities.weapons.AK47;
import org.trueaim.entities.weapons.GenericWeapon;
import org.trueaim.input.InputManager;
import org.trueaim.rendering.GUI.StatGUI;
import org.trueaim.rendering.OverlayRenderer;
import org.trueaim.rendering.Renderer;
import org.trueaim.entities.targets.TargetManager;
import org.trueaim.stats.StatTracker;

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
    private GenericWeapon weapon;              // Spielerwaffe
    private boolean showFinalStats = false; // Flag für Statistikanzeige
    private final StatGUI statGUI; // GUI-Panel für Statistiken

    public GameEngine(Window window, InputManager inputManager, Camera camera) {
        this.window = window;
        this.inputManager = inputManager;
        this.camera = camera;
        this.renderer = new Renderer(window.getWidth(), window.getHeight()); // 3D-Renderer
        this.weapon = new AK47(camera, renderer);  // Waffe erstellen
        this.targetManager = new TargetManager();  // Ziele initialisieren
        this.raycaster = new Raycasting(targetManager, weapon.getStats()); // Trefferprüfung
        this.overlayRenderer = new OverlayRenderer(weapon.getStats(), window); // UI-Renderer


        // Eingabecallbacks registrieren
        inputManager.addLeftClickCallback(this::handleShoot);  // Linksklick: Schießen
        inputManager.addRightClickCallback(weapon::onRightPress); // Rechtsklick: Zielfernrohr
        inputManager.addRkeyCallback(weapon::Reload);      //R-Taste: Nachladen

        overlayRenderer.getIngameHUD().setEquippedWeapon(weapon);

        this.statGUI = new StatGUI(window, weapon.getStats(), overlayRenderer, targetManager); // Statistik-UI initialisieren
        inputManager.setStatGUI(statGUI); // Eingabemanager mit Statistik-UI verbinden

    }

    public StatTracker getStatTracker() {
        return weapon.getStats();
    }

    public void setWeapon(GenericWeapon weapon) {
        StatTracker tracker = getStatTracker();
        weapon.setStats(tracker); // Überträgt die Statistiken der aktuellen Waffe auf die neue Waffe
        this.weapon = weapon; // Setzt die Statistiken der neuen Waffe
        this.overlayRenderer.getIngameHUD().setEquippedWeapon(weapon); // Aktualisiert die HUD-Waffe
        // this.statGUI.setStatTracker(tracker); // Aktualisiert die Statistik-UI
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
        if (weapon.hasAmmo()){
            raycaster.checkHit(camera.getPosition(), camera.getFront()); // Trefferüberprüfung
            weapon.onLeftPress();  // Waffenlogik aktivieren
            camera.getRotation(); //Debug aufruf TODO delete
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

            // Waffe deaktivieren, falls im Menü
            if (statGUI.isVisible() && weapon.isActive()) {
                weapon.setActive(false); // Waffe deaktivieren, wenn Statistik-UI sichtbar ist
            } else if (!statGUI.isVisible() && !weapon.isActive()) {
                weapon.setRecoil(statGUI.getGunHasRecoil()); // Waffenstatus basierend auf Recoil-Einstellung der Statistik-UI setzen
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
        showFinalStats = true;  //Grade useless, maybe needed for render
        printFinalStatistics(); // Statistik ausgeben
        window.cleanup();       // Ressourcen freigeben
        statGUI.cleanup();      // Statistik-UI aufräumen
        overlayRenderer.cleanup(); // UI-Renderer aufräumen
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

    /**
     * Gibt Spielstatistiken in der Konsole aus.
     */
    //TODO entfernen wenn man Statistiken am Ende dann rendert
    private void printFinalStatistics() {
        StatTracker stats = weapon.getStats();
        System.out.println("\n=== FINALE STATISTIKEN ===");
        System.out.printf("Genauigkeit: %.1f%%\n", stats.getAccuracy());
        System.out.printf("Kopftreffer: %d/%d (%.1f%%)\n",
                stats.getHeadshots(), stats.getHits(), stats.getHeadshotRate());
        System.out.printf("Schüsse: %d | Treffer: %d | Fehlschüsse: %d\n",
                stats.getShotsFired(), stats.getHits(), stats.getMisses());
        System.out.printf("Reloads: %d\n", stats.getReloads());
        System.out.printf("Schüsse/Minute: %.1f\n", stats.getShotsPerMinute());
        System.out.println("=========================");
    }
}