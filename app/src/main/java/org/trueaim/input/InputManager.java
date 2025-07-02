package org.trueaim.input;
import org.trueaim.Camera;
import org.trueaim.Window;
import java.util.ArrayList;
import java.util.List;
import static org.lwjgl.glfw.GLFW.*;

/**
 * Verwaltet alle Benutzereingaben.
 * Verarbeitet:
 * - Tastatureingaben
 * - Mausbewegung
 * - Mausklicks
 */
//TODO LAufen/ FLiegen gerade für DEbug, ändern (Collision + fester Bereich oder entfernen) (Camera muss auch geändert werden)

public class InputManager {
    private final long windowHandle;  // Fensterreferenz
    private final Camera camera;      // Steuerbare Kamera
    private final boolean[] keyStates = new boolean[GLFW_KEY_LAST + 1]; // Tastenstatus
    private boolean mouseLocked = true;  // Mauszeiger eingeschlossen?
    private float sensitivity = 0.1f;    // Mausempfindlichkeit
    private float movementSpeed = 5.0f;  // Bewegungsgeschwindigkeit
    private double lastMouseX, lastMouseY; // Letzte Mausposition
    private boolean firstMouse = true;    // Erste Bewegung?
    private final List<Runnable> leftClickCallbacks = new ArrayList<>();  // Linksklick-Handler
    private final List<Runnable> rightClickCallbacks = new ArrayList<>(); // Rechtsklick-Handler

    public InputManager(Window window, Camera camera) {
        this.windowHandle = window.getHandle();
        this.camera = camera;
        initCallbacks();  // GLFW-Callbacks registrieren
        setMouseLock(true);  // Maus initial sperren
    }

    /**
     * Registriert GLFW-Eingabe-Callbacks.
     */
    private void initCallbacks() {
        // Tastatur-Callback
        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            // Tastenstatus speichern
            if (key >= 0 && key < keyStates.length)
                keyStates[key] = action != GLFW_RELEASE;

            // ESCAPE zum Freigeben/Sperren der Maus
            if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS)
                toggleMouseLock();
        });

        // Mauspositions-Callback
        glfwSetCursorPosCallback(windowHandle, (window, xpos, ypos) -> {
            // Initialposition setzen
            if (firstMouse) {
                lastMouseX = xpos;
                lastMouseY = ypos;
                firstMouse = false;
                return;
            }

            // Deltas berechnen
            float dx = (float) (xpos - lastMouseX);
            float dy = (float) (lastMouseY - ypos);  // Umgekehrte Y-Achse
            lastMouseX = xpos;
            lastMouseY = ypos;

            // Kamerarotation bei gesperrter Maus
            if (mouseLocked)
                camera.rotate(dx * sensitivity, dy * sensitivity);
        });

        // Mausklick-Callback
        glfwSetMouseButtonCallback(windowHandle, (window, button, action, mods) -> {
            // Linksklick
            if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
                leftClickCallbacks.forEach(Runnable::run);
            }
            // Rechtsklick
            if (button == GLFW_MOUSE_BUTTON_RIGHT && action == GLFW_PRESS) {
                rightClickCallbacks.forEach(Runnable::run);
            }
        });
    }

    /**
     * Aktualisiert den Spielzustand basierend auf Eingaben.
     * @param deltaTime Zeit seit letztem Frame
     */
    public void update(float deltaTime) {
        float velocity = movementSpeed * deltaTime; // Distanz pro Frame

        // Kamerabewegung basierend auf Tastenstatus
        if (isKeyPressed(GLFW_KEY_W)) camera.moveForward(velocity);
        if (isKeyPressed(GLFW_KEY_S)) camera.moveBackward(velocity);
        if (isKeyPressed(GLFW_KEY_A)) camera.moveLeft(velocity);
        if (isKeyPressed(GLFW_KEY_D)) camera.moveRight(velocity);
    }

    // Hilfsmethoden
    public boolean isKeyPressed(int keyCode) {
        return keyCode >= 0 && keyCode < keyStates.length && keyStates[keyCode];
    }
    public void toggleMouseLock() {
        mouseLocked = !mouseLocked;
        setMouseLock(mouseLocked);
    }
    public void setMouseLock(boolean locked) {
        glfwSetInputMode(windowHandle, GLFW_CURSOR,
                locked ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
        if (locked) firstMouse = true; // Position zurücksetzen
    }

    // Callback-Registrierung
    public void addLeftClickCallback(Runnable callback) {
        leftClickCallbacks.add(callback);
    }
    public void addRightClickCallback(Runnable callback) {
        rightClickCallbacks.add(callback);
    }
}