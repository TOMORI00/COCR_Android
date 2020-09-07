package com.nju.cocr.structure;

import android.graphics.PointF;

public class Utils {

    /**
     * 获取两点之间的距离
     *
     * @return 距离
     */
    static public double getDistance(PointF startPoint, PointF endPoint) {
        return Math.sqrt((startPoint.x - endPoint.x) * (startPoint.x - endPoint.x) + (startPoint.y - endPoint.y) * (startPoint.y - endPoint.y));
    }
}
