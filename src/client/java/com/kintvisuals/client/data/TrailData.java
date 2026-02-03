package com.kintvisuals.client.data;

import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;

public class TrailData {
    private final List<Vec3d> points = new ArrayList<>();
    private final int maxPoints;

    public TrailData(int maxPoints) {
        this.maxPoints = maxPoints;
    }

    public void addPoint(Vec3d pos) {
        points.add(pos);
        while (points.size() > maxPoints) {
            points.remove(0);
        }
    }

    public List<Vec3d> getPoints() {
        return points;
    }

    public void clear() {
        points.clear();
    }

    public boolean isEmpty() {
        return points.isEmpty();
    }
}