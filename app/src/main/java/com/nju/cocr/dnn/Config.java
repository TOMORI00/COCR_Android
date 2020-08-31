package com.nju.cocr.dnn;

import android.graphics.Color;
import android.graphics.Paint;

import java.util.HashMap;
import java.util.Map;

public class Config {
    static public final int SIZE_PER_PIX = 3;
    static public final int SIZE_FLOAT = 4;
    static public final int CLASS_NUM = 11;
    static public final int LOC_PARM_NUM = 4;
    static public final int SIZE_FIXED_INPUT = 640;
    static public final String MODEL_FILE_NAME = "soso11-yolov4-tiny-640-fp16.tflite";
    static public final Map<Integer, String> SOSO11_LABELS = new HashMap<>();
    static public final Paint DETECTOR_PAINTER = new Paint(Paint.ANTI_ALIAS_FLAG);

    static {
        SOSO11_LABELS.put(0, "@");
        SOSO11_LABELS.put(1, "-");
        SOSO11_LABELS.put(2, "=");
        SOSO11_LABELS.put(3, "#");
        SOSO11_LABELS.put(4, "@@");
        SOSO11_LABELS.put(5, "C");
        SOSO11_LABELS.put(6, "H");
        SOSO11_LABELS.put(7, "O");
        SOSO11_LABELS.put(8, "N");
        SOSO11_LABELS.put(9, "P");
        SOSO11_LABELS.put(10, "S");

        DETECTOR_PAINTER.setColor(Color.BLACK);
        DETECTOR_PAINTER.setStrokeWidth(2);
        DETECTOR_PAINTER.setStrokeCap(Paint.Cap.ROUND);
        DETECTOR_PAINTER.setStrokeJoin(Paint.Join.ROUND);
    }
}
