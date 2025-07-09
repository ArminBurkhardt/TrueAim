package org.trueaim.strahlwerfen;

public class HeatmapValues {
    public double xOffset;
    public double yOffset;
    public boolean hitStatus;

    public HeatmapValues(double xOffset, double yOffset, boolean hitStatus) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.hitStatus = hitStatus;
    }

    public HeatmapValues(double xOffset, double yOffset) {
        this(xOffset, yOffset, false);
    }

    public void setHitStatus(boolean hitStatus) {
        this.hitStatus = hitStatus;
    }
    //Debug TODO delete
    public void print() {
        System.out.println("HeatmapValues{" +
                "xOffset=" + xOffset +
                ", yOffset=" + yOffset +
                ", hitStatus=" + hitStatus +
                '}');
    }
}
