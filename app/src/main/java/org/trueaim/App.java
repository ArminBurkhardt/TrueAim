package org.trueaim;
import org.trueaim.input.InputManager;

/**
 * Hauptklasse der Anwendung.
 * Einstiegspunkt des Programms.
 */
public class App {
    public static void main(String[] args) {
        // Hauptkomponenten initialisieren
        Window window = new Window(1280, 720, "TrueAim");
        Camera camera = new Camera();
        InputManager inputManager = new InputManager(window, camera);

        // Spielengine starten
        GameEngine gameEngine = new GameEngine(window, inputManager, camera);
        gameEngine.run();
    }
}