package com.nju.cocr.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

public class ScribbleView extends View {
    static final String TAG = "ScribbleView";
    // 记录颜色、线宽、反锯齿策略的变量，用的时候设置属性即可
    static Paint paint;

    //移动速率
    private static double SPEED = 4.5;

    //放大速率
    private static double SCALE = 8;

    //旋转速率
    private static double ROTATE = 0.2;

    //缩放敏感度，越低需要越小的scale以触发缩放
    private static double SENSE = 0.005;

    //旋转敏感度，越低需要越小的rotation以触发旋转
    private static double SENSE2 = 0.85;

    static {
        paint = new Paint();
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    // 当前正在绘制的笔画
    Stroke stroke;
    // 记录所有笔画的结构
    Script script = new Script();
    // 默认线宽
    float strokeWidth = 16;

    public ScribbleView(Context context) {
        super(context);
    }
    // int strokeIndex = -1, ptsIndex = -1;

    public ScribbleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    public ScribbleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public ScribbleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void clear() {
        script = new Script();
        // strokeIndex = ptsIndex = -1;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getPointerCount() > 1) {
                    break;
                }
                Log.d(TAG, "单指--->down@" + event.getX() + "," + event.getY());
                stroke = new Stroke(Color.valueOf(Color.BLACK),
                        SystemClock.currentThreadTimeMillis());
                stroke.add(new PointF(event.getX(), event.getY()), strokeWidth);
                script.add(stroke);
                // ptsIndex = 0;
                // strokeIndex = script.size() - 1;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 2) {
                    if (event.getHistorySize() == 0) return true;
                    double center_X = (event.getX(0) + event.getX(1)) / 2;
                    double center_Y = (event.getY(0) + event.getY(1)) / 2;
                    double center_X_O = (event.getHistoricalX(0, event.getHistorySize() - 1) + event.getHistoricalX(1, event.getHistorySize() - 1)) / 2;
                    double center_Y_O = (event.getHistoricalY(0, event.getHistorySize() - 1) + event.getHistoricalY(1, event.getHistorySize() - 1)) / 2;

                    double distance_X = abs(event.getX(0) - event.getX(1));
                    double distance_Y = abs(event.getY(0) - event.getY(1));
                    double distance_X_O = abs((event.getHistoricalX(0, event.getHistorySize() - 1) - event.getHistoricalX(1, event.getHistorySize() - 1)));
                    double distance_Y_O = abs((event.getHistoricalY(0, event.getHistorySize() - 1) - event.getHistoricalY(1, event.getHistorySize() - 1)));

                    double D_X = center_X - center_X_O;
                    double D_Y = center_Y - center_Y_O;
                    double D_scale = (distance_X * distance_X + distance_Y * distance_Y) / (distance_X_O * distance_X_O + distance_Y_O * distance_Y_O);
                    double D_rotate = getRotation(event);

                    Log.d(TAG, "双指手势---X方向：" + D_X + " Y方向：" + D_Y + " 大小：" + D_scale + " 旋转：" + D_rotate);

                    handleActions(D_X * SPEED, D_Y * SPEED, D_scale, center_X, center_Y, D_rotate);

                    break;
                } else if (event.getPointerCount() > 2) {
                    Log.d(TAG, "三指及以上移动");
                }
                Log.d(TAG, "单指--->move@" + event.getX() + "," + event.getY());
                stroke.add(new PointF(event.getX(), event.getY()), strokeWidth);
                // ptsIndex = stroke.size() - 1;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "单指--->up@" + event.getX() + "," + event.getY());
                stroke.add(new PointF(event.getX(), event.getY()), strokeWidth);
                // ptsIndex = stroke.size() - 1;
                invalidate();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.d(TAG, "PointerCountDown " + event.getPointerCount());
                if (event.getPointerCount() == 2 && stroke.size() == 1) {
                    Log.d("TAG", "delete");
                    script.del();
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.d(TAG, "PointerCountUp " + event.getPointerCount());
                stroke = new Stroke(Color.valueOf(Color.BLACK),
                        SystemClock.currentThreadTimeMillis());
                break;

        }
        return true;
    }

    private double getRotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        double delta_x_h = (event.getHistoricalX(0, event.getHistorySize() - 1) - event.getHistoricalX(1, event.getHistorySize() - 1));
        double delta_y_h = (event.getHistoricalY(0, event.getHistorySize() - 1) - event.getHistoricalY(1, event.getHistorySize() - 1));
        double radians_h = Math.atan2(delta_y_h, delta_x_h);
        return ((double) Math.toDegrees(radians_h)) - (double) Math.toDegrees(radians);
    }

    private void handleActions(double dx, double dy, double s, double cx, double cy, double r) {
        handleTranslation(dx, dy);
        if (abs(s - 1) < SENSE) {
            handleZooming(cx, cy, s);
        }
        if (abs(r) < SENSE2) {
            handleRotate(cx, cy, ROTATE * r);
        }
        invalidate();
    }

    private void handleRotate(double cx, double cy, double r) {
        for (int i = 0; i < script.size(); i++) {
            for (int j = 0; j < script.data.get(i).size(); j++) {
                double ox = script.data.get(i).data.get(j).x;
                double oy = script.data.get(i).data.get(j).y;
                double x1 = ox - cx;
                double y1 = oy - cy;
                script.data.get(i).data.get(j).x = (float) ((cos(r)*x1 + sin(r)*y1) + cx);
                script.data.get(i).data.get(j).y = (float) ((-sin(r)*x1 + cos(r)*y1) + cy);
            }
        }
    }

    private void handleZooming(double x, double y, double s) {
//        Log.d(TAG,s+"");
        for (int i = 0; i < script.size(); i++) {
            for (int j = 0; j < script.data.get(i).size(); j++) {
                double ox = script.data.get(i).data.get(j).x;
                double oy = script.data.get(i).data.get(j).y;
                script.data.get(i).data.get(j).x += (s - 1) * SCALE * (ox - x);
                script.data.get(i).data.get(j).y += (s - 1) * SCALE * (oy - y);
            }
        }
    }

    private void handleTranslation(double offsetX, double offsetY) {
        for (int i = 0; i < script.size(); i++) {
            for (int j = 0; j < script.data.get(i).size(); j++) {
                script.data.get(i).data.get(j).x += offsetX;
                script.data.get(i).data.get(j).y += offsetY;
            }
        }
    }


    /**
     * 刷新整个 View，View显示什么完全由这个函数决定
     * 背景颜色在 xml 文件中的 background 字段决定
     *
     * @param canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (script != null) {
            Log.d(TAG, "draw...");
            canvas.save();
            // canvas 关联到当前 View，把 script 全部重绘到当前 View
            script.drawBy(canvas);
            canvas.restore();
        }
    }

    /**
     * 表示从落笔到起笔的一组点序列
     */
    class Stroke {
        // 点序列
        List<PointF> data;
        // 用什么宽度连接相邻两点。以点对点的粒度记录线宽，目的是支持可能的墨迹效果
        List<Float> widths;
        // 这个笔画的颜色
        Color color;
        // 落笔的时间戳
        long timestamp;

        /**
         * 构造一个新笔画
         *
         * @param color     这个笔画的颜色
         * @param timestamp 落笔时间
         */
        public Stroke(Color color, long timestamp) {
            data = new ArrayList<>();
            widths = new ArrayList<>();
            this.color = color;
            this.timestamp = timestamp;
        }

        /**
         * 返回笔画包含几个点
         *
         * @return
         */
        public int size() {
            return data.size();
        }

        /**
         * 清空点坐标数组和笔画宽度信息
         */
        public void clear() {
            data = new ArrayList<>();
            widths = new ArrayList<>();
        }

        /**
         * 添加一个点，标识这个点和上个点用什么宽度连接
         *
         * @param pts
         * @param width
         */
        public void add(PointF pts, float width) {
            data.add(pts);
            widths.add(width);
        }

        public List<PointF> get() {
            return data;
        }

        /**
         * 向 canvas 绘制这个笔画
         *
         * @param canvas
         */
        public void drawBy(Canvas canvas) {
            if (data.size() == 0) {
                return;
            }
            paint.setColor(color.toArgb());
            // 防止size=1时，漏掉一个点
            paint.setStrokeWidth(widths.get(0));
            canvas.drawLine(data.get(0).x, data.get(0).y,
                    data.get(0).x, data.get(0).y, paint);
            for (int i = 1; i < data.size(); i++) {
                // 点连成线
                paint.setStrokeWidth(widths.get(i));
                canvas.drawLine(data.get(i).x, data.get(i).y,
                        data.get(i - 1).x, data.get(i - 1).y, paint);
            }
        }
    }

    /**
     * 表示一组笔画的集合
     */
    class Script {
        List<Stroke> data;

        public Script() {
            data = new ArrayList<>();
        }

        /**
         * 返回有多少笔画
         *
         * @return
         */
        public int size() {
            return data.size();
        }

        /**
         * 添加一个笔画
         *
         * @param stroke
         */
        public void add(Stroke stroke) {
            data.add(stroke);
        }

        public void clear() {
            data = new ArrayList<>();
        }

        public void del() {
            if (data.size() > 0) {
                data.remove(data.size() - 1);
            }
        }

        /**
         * 返回所有笔画的集合的深拷贝
         *
         * @return
         */
        public List<List<PointF>> get() {
            List<List<PointF>> copy = new ArrayList<>();
            for (Stroke s : data) {
                try {
                    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                    ObjectOutputStream out = new ObjectOutputStream(byteOut);
                    out.writeObject(s.get());
                    ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
                    ObjectInputStream in = new ObjectInputStream(byteIn);
                    List<PointF> sCopy = (List<PointF>) in.readObject();
                    copy.add(sCopy);
                } catch (IOException | ClassNotFoundException e) {

                }
            }
            return copy;
        }

        // public void drawBy(Canvas canvas) {
        //     if(ptsIndex<0){
        //         return;
        //     }
        //     PointF pts1, pts2;
        //     if (ptsIndex == 0) {
        //         pts1 = pts2 = script.data.get(strokeIndex).data.get(0);
        //         paint.setStrokeWidth(script.data.get(strokeIndex).widths.get(0));
        //     } else {
        //         pts1 = script.data.get(strokeIndex).data.get(ptsIndex - 1);
        //         pts2 = script.data.get(strokeIndex).data.get(ptsIndex);
        //         paint.setStrokeWidth(script.data.get(strokeIndex).widths.get(ptsIndex));
        //     }
        //     canvas.drawLine(pts1.x, pts1.y, pts2.x, pts2.y, paint);
        // }

        /**
         * 向 canvas 绘制所有笔画
         *
         * @param canvas
         */
        public void drawBy(Canvas canvas) {
            for (Stroke s : data) {
                s.drawBy(canvas);
            }
        }
    }
}