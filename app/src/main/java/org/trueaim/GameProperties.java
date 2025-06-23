package org.trueaim;

public class GameProperties {
    int fps_target = 120;

    public GameProperties() {}


    public double secsPerUpdate() {
        // Target frame time
        return 1.0d / (double) fps_target;
    }
}
