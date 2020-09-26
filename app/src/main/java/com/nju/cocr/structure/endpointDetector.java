package com.nju.cocr.structure;

import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
// todo 楔形键from与to区分
//todo 鲁棒性提升，如果笔画挨得很近，需要做更为详细的阈值判断
//todo 非单键情况的鲁棒性，具体表现为双键的两个线段不平行

/**
 * 此类用于判断直线的端点位置
 */
public class endpointDetector {

    /*** 用于log输出*/
    final String TAG = "Structure";

    /**
     * 从ScribbleView中获取的笔画信息
     **/
    private List<List<PointF>> paths;

    /**
     * 模型导出的方框数据，此方框要求只包含键
     **/
    private List<RectF> labels;


    public endpointDetector(List<List<PointF>> paths, List<RectF> labels) {
        this.paths = paths;
        this.labels = labels;
    }

    /**
     * 用于检测被方框框出的直线端点方法
     *
     * @return 记录方框内键朝向的list，与方框的索引对应
     */
    public List<Direction> detect() {

        //保存结果
        List<Direction> result = new ArrayList<>();

        //插点后的结果点集
        List<PointF> dots = insertDots();

        //遍历所有方框
        for (RectF rect : labels) {

            //用于四角小方框内点的个数的计数
            int leftTopCount = 0;
            int rightTopCount = 0;
            int leftBottomCount = 0;
            int rightBottomCount = 0;

            //方框大小为0.3倍的总方框大小
            RectF leftTop = new RectF(rect.left, rect.top, rect.left + rect.width() * 0.3f, rect.top + rect.height() * 0.3f);
            RectF rightTop = new RectF(rect.left + rect.width() * 0.7f, rect.top, rect.right, rect.top + rect.height() * 0.3f);
            RectF leftBottom = new RectF(rect.left, rect.top + rect.height() * 0.7f, rect.left + rect.width() * 0.3f, rect.bottom);
            RectF rightBottom = new RectF(rect.left + rect.width() * 0.7f, rect.top + rect.height() * 0.7f, rect.right, rect.bottom);

            //遍历点集，判断在哪一个小方框内，或者都不在
            for (PointF dot : dots) {
                if (leftTop.contains(dot.x, dot.y)) {
                    leftTopCount++;
                } else if (leftBottom.contains(dot.x, dot.y)) {
                    leftBottomCount++;
                } else if (rightTop.contains(dot.x, dot.y)) {
                    rightTopCount++;
                } else if (rightBottom.contains(dot.x, dot.y)) {
                    rightBottomCount++;
                }
            }

            //判断朝向,将其存入结果集
            if (leftBottomCount + rightTopCount > leftTopCount + rightBottomCount) {
                result.add(Direction.LeftBottom_RightTop);
            } else if (rightTopCount + leftBottomCount < rightBottomCount + leftTopCount) {
                result.add(Direction.LeftTop_RightBottom);
            }
            //两个一样的情况说明是水平或者竖直，实际上如果倾斜角度很小，那么也有可能被划归为此类
            else if (rect.width() > rect.height()) {
                result.add(Direction.Horizon);
            } else if (rect.height() > rect.width()) {
                result.add(Direction.Vertical);
            } else {
                Log.d(TAG, "detect: ERROR");

            }
        }
        return result;
    }

    /**
     * 用于均匀插点的方法类
     *
     * @return 查完点后所有点的合集，包括start与end
     */
    public List<PointF> insertDots() {
        List<PointF> result = new ArrayList<>();
        for (List<PointF> i : paths) {
            PointF startPoint = i.get(0);
            PointF endPoint = i.get(1);
            result.add(startPoint);
            result.add(endPoint);
            //默认选择插入个点
            for (int j = 1; j < 10; j++) {
                result.add(new PointF((endPoint.x - startPoint.x) / 10 * j, (endPoint.y - startPoint.y) / 10 * j));
            }
        }
        return result;
    }


}
