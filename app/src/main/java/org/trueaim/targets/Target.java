package org.trueaim.targets;

import org.joml.Vector3f;

public class Target {
    private Vector3f position;
    private float size;

    public Target(Vector3f position, float size) {
        this.position = position;
        this.size = size;
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getSize() {
        return size;
    }
}
