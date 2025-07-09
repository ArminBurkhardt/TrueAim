package org.trueaim.sound;

import org.joml.Vector3f;
import org.trueaim.Camera;
import org.lwjgl.openal.*;

import java.io.File;

public class SoundSystemTest {

    public static void main(String[] args) {
        SoundPlayer player = new SoundPlayer();

        // Teste UI-Sound
        player.play(SoundPlayer.SHOOT);
        // Teste Cleanup
        player.cleanup();
        System.out.println("SoundSystem erfolgreich getestet!");
    }
}