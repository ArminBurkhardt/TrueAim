package org.trueaim;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;

/**
 * Verwaltet das Anwendungsfenster und die OpenGL-Kontextinitialisierung.
 * Verantwortlich für:
 * - Fenstererstellung und Lebenszyklus
 * - OpenGL-Fähigkeitsprüfung
 * - Zeitmanagement für Frame-Berechnungen
 */
//TODO maybe Fullscreen, wenn dann aber erst am Ende
public class Window {
    private final long windowHandle;  // GLFW-Fensterreferenz
    private final int width;          // Fensterbreite in Pixeln
    private final int height;         // Fensterhöhe in Pixeln
    private double lastFrameTime;     // Zeitpunkt des letzten Frames (in Sekunden)
    private float deltaTime;          // Zeit seit letztem Frame (in Sekunden)

    public Window(int width, int height, String title) {
        this.width = width;
        this.height = height;

        // GLFW Initialisierung
        if (!glfwInit()) throw new IllegalStateException("GLFW konnte nicht initialisiert werden");

        // Fenstererstellung
        windowHandle = glfwCreateWindow(width, height, title, 0, 0);
        if (windowHandle == 0) throw new IllegalStateException("Fenstererstellung fehlgeschlagen");

        // OpenGL-Kontext aktivieren
        glfwMakeContextCurrent(windowHandle);
        createCapabilities();  // LWJGL-Fähigkeiten laden

        // Grundlegende OpenGL-Einstellungen
        glEnable(GL_DEPTH_TEST);  // Tiefentest aktivieren
        lastFrameTime = glfwGetTime();  // Startzeitpunkt setzen
    }

    // Zugriffsmethoden
    public long getHandle() { return windowHandle; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean shouldClose() { return glfwWindowShouldClose(windowHandle); }
    public float getDeltaTime() { return deltaTime; }

    /**
     * Aktualisiert das Fenster für den nächsten Frame.
     * Führt folgende Schritte aus:
     * 1. Tauscht Front- und Backbuffer (Double Buffering)
     * 2. Verarbeitet Systemevents
     * 3. Berechnet die Framezeit (Delta Time)
     */
    public void update() {
        glfwSwapBuffers(windowHandle);  // Puffer tauschen
        glfwPollEvents();               // Events verarbeiten

        // Delta Time berechnen
        double currentTime = glfwGetTime();
        deltaTime = (float) (currentTime - lastFrameTime);
        lastFrameTime = currentTime;
    }

    /**
     * Gibt Fensterressourcen frei und beendet GLFW.
     * Muss am Ende der Anwendung aufgerufen werden.
     */
    public void cleanup() {
        glfwDestroyWindow(windowHandle);
        glfwTerminate();
    }
}