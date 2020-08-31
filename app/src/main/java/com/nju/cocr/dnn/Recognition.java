package com.nju.cocr.dnn;

import android.graphics.RectF;
import androidx.annotation.NonNull;

import static com.nju.cocr.dnn.Config.SOSO11_LABELS;

public class Recognition {
    int index;
    float confidence;
    RectF boundingBox;

    public Recognition(int index, float confidence, RectF boundingBox) {
        this.index = index;
        this.confidence = confidence;
        this.boundingBox = boundingBox;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public RectF getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(RectF boundingBox) {
        this.boundingBox = boundingBox;
    }

    @NonNull
    @Override
    public String toString() {
        return "\n[" + SOSO11_LABELS.get(index) + "," + confidence + "," + boundingBox + "]";
    }
}
