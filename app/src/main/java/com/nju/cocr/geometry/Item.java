package com.nju.cocr.geometry;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;

public class Item<T> {
    static Paint paint;

    static {
        paint = new Paint();
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    List<T> data = new ArrayList<>();

    public void drawBy(Canvas canvas) {

    }

    public void add(T component) {
        data.add(component);
    }

    public int size() {
        return data.size();
    }

    public List<T> getDataClone() {
        return data;
    }

    public List<T> getData() {
        return data;
    }

    public void clear() {
        data = new ArrayList<>();
    }
}
