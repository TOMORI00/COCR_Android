package com.nju.cocr.structure;

import android.graphics.PointF;
import android.graphics.RectF;

public class Bond {

    private int id;

    private RectF rect;

    private PointF startPoint;

    private PointF endPoint;

    public Bond(int id, RectF rect, PointF startPoint, PointF endPoint) {
        this.id = id;
        this.rect = rect;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    public int getId() {
        return id;
    }

    public RectF getRect() {
        return rect;
    }

    public PointF getStartPoint() {
        return startPoint;
    }

    public PointF getEndPoint() {
        return endPoint;
    }

    @Override
    public String toString() {
        return "Bond{" +
                "id=" + id +
                ", rect=" + rect +
                ", startPoint=" + startPoint +
                ", endPoint=" + endPoint +
                '}';
    }
}
