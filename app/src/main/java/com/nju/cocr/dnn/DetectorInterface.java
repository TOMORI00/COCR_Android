package com.nju.cocr.dnn;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;

import java.util.List;

public interface DetectorInterface {
    int getThreadNum();

    /**
     * 调用之后必须再调用 initialize 方可生效
     *
     * @param threadNum 线程数
     */
    void setThreadNum(int threadNum);

    /**
     * 加载模型，需要传入一个 AssetManager，getAssets() 获得
     *
     * @param assetManager AssetManager
     * @return 是否初始化成功
     */
    boolean initialize(AssetManager assetManager);

    /**
     * 释放掉模型占用的所有内存，后面使用需要先调用 initialize
     */
    void close();

    boolean IsNNAPIEnabled();

    /**
     * 调用之后必须再调用 initialize 方可生效
     *
     * @param isNNAPIEnabled 是否使用 NNAPI
     */
    void enableNNAPI(boolean isNNAPIEnabled);

    float getIOUThresh();

    void setIOUThresh(float thresh);

    float getConfThresh();

    void setConfThresh(float thresh);

    /**
     * 输入的位图尺寸必须满足 Config 中的 SIZE_FIXED_INPUT，现在 tflite 不支持动态尺寸
     *
     * @param bitmap
     * @return
     */
    List<Recognition> getRecognition(Bitmap bitmap);

    List<Recognition> nms(List<Recognition> inputs);

    float iou(RectF a, RectF b);
}
