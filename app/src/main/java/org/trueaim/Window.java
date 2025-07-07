package org.trueaim;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVidMode;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Verwaltet das Anwendungsfenster und die OpenGL-Kontextinitialisierung.
 * Verantwortlich für:
 * - Fenstererstellung und Lebenszyklus
 * - OpenGL-Fähigkeitsprüfung
 * - Zeitmanagement für Frame-Berechnungen
 */
public class Window {
    private final long windowHandle;  // GLFW-Fensterreferenz
    private final int width;          // Fensterbreite in Pixeln
    private final int height;         // Fensterhöhe in Pixeln
    private double lastFrameTime;     // Zeitpunkt des letzten Frames (in Sekunden)
    private float deltaTime;          // Zeit seit letztem Frame (in Sekunden)
    private static long[] monitors = null;
    private static boolean isfullscreen = false;
    public boolean antialiasing = false; // Antialiasing-Flag
    private boolean forceClose; // Flag zum Erzwingen des Schließens

    public Window(int width, int height, String title) {
        this.width = width;
        this.height = height;
        forceClose = false;

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


    public Window(String title) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();


        this.width = (int) screenSize.getWidth();
        this.height = (int) screenSize.getHeight();

        // GLFW Initialisierung
        if (!glfwInit()) throw new IllegalStateException("GLFW konnte nicht initialisiert werden");

        // Fenstererstellung
        windowHandle = glfwCreateWindow(width, height, title, NULL, 0);
        if (windowHandle == 0) throw new IllegalStateException("Fenstererstellung fehlgeschlagen");


        // OpenGL-Kontext aktivieren
        glfwMakeContextCurrent(windowHandle);
        createCapabilities();  // LWJGL-Fähigkeiten laden

        // read monitors
        PointerBuffer pointerBuffer = glfwGetMonitors();
        int remaining = pointerBuffer.remaining();
        monitors = new long[remaining];
        for (int i = 0; i < remaining; i++) {
            monitors[i] = pointerBuffer.get(i);
        }
        // Vollbildmodus aktivieren
        toggleFullscreen();

        // Grundlegende OpenGL-Einstellungen
        glEnable(GL_DEPTH_TEST);  // Tiefentest aktivieren
        lastFrameTime = glfwGetTime();  // Startzeitpunkt setzen
    }

    public void toggleFullscreen() {
        if (isfullscreen) {
            // Fenster zurück in den Fenstermodus
            glfwSetWindowMonitor(windowHandle, NULL, 100, 100, width-200, height-200, GLFW_DONT_CARE);
        } else {
            // Vollbildmodus aktivieren
            glfwSetWindowMonitor(windowHandle, monitors[0], 0, 0, width, height, GLFW_DONT_CARE);

        }
        isfullscreen = !isfullscreen;
    }

    // Zugriffsmethoden
    public long getHandle() { return windowHandle; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean shouldClose() { return (glfwWindowShouldClose(windowHandle) || forceClose); }
    public float getDeltaTime() { return deltaTime; }
    public void forceClose() { forceClose = true; }

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

    public void restoreState() {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }
}