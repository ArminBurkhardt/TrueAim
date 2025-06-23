package org.trueaim;

import org.lwjgl.glfw.*;
import org.lwjgl.system.MemoryUtil;

import java.util.concurrent.Callable;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class main {

    public static void main(String[] args) {
        GameProperties GP = new GameProperties();
        GP.fps_target = 60;
        Game main = new Game(GP);
    }

    public void gameLoop(Game game) {

        double prevFrame = System.currentTimeMillis();

    }

}
