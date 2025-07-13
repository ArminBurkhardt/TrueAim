package org.trueaim.input;
import org.lwjgl.glfw.GLFW;
import org.trueaim.Camera;
import org.trueaim.Window;
import org.trueaim.rendering.GUI.StatGUI;

import java.util.ArrayList;
import java.util.List;

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
    private final boolean[] keyStates = new boolean[GLFW.GLFW_KEY_LAST + 1]; // Tastenstatus
    private boolean mouseLocked = true;  // Mauszeiger eingeschlossen?
    private float sensitivity = 0.1f;    // Mausempfindlichkeit //TODO maybe option zum anpassen
    private float movementSpeed = 5.0f;  // Bewegungsgeschwindigkeit
    private double lastMouseX, lastMouseY; // Letzte Mausposition
    private boolean firstMouse = true;    // Erste Bewegung?
    private final List<Runnable> leftClickCallbacks = new ArrayList<>();  // Linksklick-Handler
    private final List<Runnable> rightClickCallbacks = new ArrayList<>(); // Rechtsklick-Handler
    private final List<Runnable> RkeyCallbacks = new ArrayList<>(); // Rkey-Handler
    private final List<Runnable> leftReleaseCallbacks = new ArrayList<>();  // Links loslassen-Handler
    private final List<Runnable> rightRealseCallbacks = new ArrayList<>(); // Rechts loslassen-Handler
    private final Window window;
    private boolean showEscMenu = false; // Flag für Escape Menü
    private StatGUI statGUI; // GUI-Panel für Statistiken
    private Runnable setAK47Callback; // Callback zum Setzen der AK47
    private Runnable setV9SCallback; // Callback zum Setzen der V9S

    public InputManager(Window window, Camera camera) {
        this.windowHandle = window.getHandle();
        this.window = window;
        this.camera = camera;
        initCallbacks();  // GLFW-Callbacks registrieren
        setMouseLock(true);  // Maus initial sperren
    }

    public void setStatGUI(StatGUI statGUI) {
        this.statGUI = statGUI; // GUI-Panel für Statistiken setzen
    }

    public void bindSetWeaponCallbacks(Runnable setAK47Callback, Runnable setV9SCallback) {
        this.setAK47Callback = setAK47Callback; // Callback zum Setzen der AK47
        this.setV9SCallback = setV9SCallback; // Callback zum Setzen der V9S
    }


    /**
     * Registriert GLFW-Eingabe-Callbacks.
     */
    private void initCallbacks() {
        // Tastatur-Callback
        GLFW.glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            // Tastenstatus speichern
            if (key >= 0 && key < keyStates.length)
                keyStates[key] = action != GLFW.GLFW_RELEASE;

            // ESCAPE zum Freigeben/Sperren der Maus
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS) {
                toggleMouseLock();
                // this.window.toggleFullscreen();
                showEscMenu = !showEscMenu; // Menü anzeigen/verstecken
                if (showEscMenu) {
                    statGUI.enable(); // Statistiken anzeigen
                } else {
                    statGUI.disable(); // Statistiken ausblenden
                }

            }

            if (key == GLFW.GLFW_KEY_F11 && action == GLFW.GLFW_PRESS) {
                this.window.toggleFullscreen();
            }


            if (key == GLFW.GLFW_KEY_R && action == GLFW.GLFW_PRESS) {
                RkeyCallbacks.forEach(Runnable::run);
            }

            if (key == GLFW.GLFW_KEY_1 && action == GLFW.GLFW_PRESS) {
                setAK47Callback.run();
            }

            if (key == GLFW.GLFW_KEY_2 && action == GLFW.GLFW_PRESS) {
                setV9SCallback.run();
            }


        });

        // Mauspositions-Callback
        GLFW.glfwSetCursorPosCallback(windowHandle, (window, xpos, ypos) -> {
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
        GLFW.glfwSetMouseButtonCallback(windowHandle, (window, button, action, mods) -> {
            // Linksklick
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == GLFW.GLFW_PRESS) {
                leftClickCallbacks.forEach(Runnable::run);
                this.statGUI.onClick(); // Klick an StatGUI weiterleiten
            }
            // Rechtsklick
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && action == GLFW.GLFW_PRESS) {
                rightClickCallbacks.forEach(Runnable::run);
            }
            // Links- oder Rechtsklick loslassen
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == GLFW.GLFW_RELEASE) {
                leftReleaseCallbacks.forEach(Runnable::run);
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && action == GLFW.GLFW_RELEASE) {
                rightRealseCallbacks.forEach(Runnable::run);
            }
            // StatGUI Close -> Mouse Unlock
            if (!statGUI.isVisible() && showEscMenu) {
                showEscMenu = false; // Menü formal ausblenden
                toggleMouseLock(); // Maus entsperren
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
        if (isKeyPressed(GLFW.GLFW_KEY_W)) camera.moveForward(velocity);
        if (isKeyPressed(GLFW.GLFW_KEY_S)) camera.moveBackward(velocity);
        if (isKeyPressed(GLFW.GLFW_KEY_A)) camera.moveLeft(velocity);
        if (isKeyPressed(GLFW.GLFW_KEY_D)) camera.moveRight(velocity);
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
        GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR,
                locked ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
        if (locked) firstMouse = true; // Position zurücksetzen
    }

    // Callback-Registrierung
    public void addLeftClickCallback(Runnable callback) {
        leftClickCallbacks.add(callback);
    }
    public void addRightClickCallback(Runnable callback) {
        rightClickCallbacks.add(callback);
    }
    public void addRkeyCallback(Runnable callback) {
        RkeyCallbacks.add(callback);
    }


    // Callback-Entfernung
    public void clearLeftClickCallbacks() {
        leftClickCallbacks.clear();
    }
    public void clearRightClickCallbacks() {
        rightClickCallbacks.clear();
    }
    public void clearLeftReleaseCallbacks() {
        leftReleaseCallbacks.clear();
    }
    public void clearRightReleaseCallbacks() {
        rightRealseCallbacks.clear();
    }


    // Sensitivity
    public void setSensitivity(float sensitivity) {
        this.sensitivity = sensitivity;
    }
    public float getSensitivity() {
        return sensitivity;
    }

    public void addLeftReleaseCallback(Runnable callback) {
        leftReleaseCallbacks.add(callback);
    }
    public void addRightReleaseCallback(Runnable callback) {
        rightRealseCallbacks.add(callback);
    }
}