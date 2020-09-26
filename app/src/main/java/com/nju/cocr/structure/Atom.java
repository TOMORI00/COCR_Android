package com.nju.cocr.structure;

import android.graphics.RectF;

public class Atom {

    int id;

    RectF rect;

    public Atom(int id, RectF rect) {
        this.id = id;
        this.rect = rect;
    }

    public int getId() {
        return id;
    }

    public RectF getRect() {
        return rect;
    }

    @Override
    public String toString() {
        return "Atom{" +
                "id=" + id +
                ", rect=" + rect +
                '}';
    }
}
