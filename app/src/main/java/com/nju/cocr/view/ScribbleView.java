package com.nju.cocr.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import android.graphics.Path;

import com.nju.cocr.dnn.DetectorInterface;
import com.nju.cocr.dnn.Recognition;
import com.nju.cocr.structure.Atom;
import com.nju.cocr.structure.Bond;
import com.nju.cocr.structure.Direction;
import com.nju.cocr.structure.Synthesizer;
import com.nju.cocr.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.nju.cocr.dnn.Config.SOSO11_LABELS;
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

    //记录当前画笔状态，0为默认画笔，1为橡皮擦，2为标准化
    private int drawingstate = 0;

    public int getDrawingstate() {
        return drawingstate;
    }

    public void setDrawingstate(int drawingstate) {
        this.drawingstate = drawingstate;
    }

    //记录橡皮擦路线的中心坐标、起点终点坐标
    private double era_cenX = 0.0;
    private double era_cenY = 0.0;
    private double era_binX = 0.0;
    private double era_endX = 0.0;
    private double era_binY = 0.0;
    private double era_endY = 0.0;

    //记录直线起笔落笔位置
    private double line_downX = 0.0;
    private double line_upX = 0.0;
    private double line_downY = 0.0;
    private double line_upY = 0.0;
    static {
        paint = new Paint();
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    public List<Atom> getAtoms1() {
        return atoms1;
    }

    public List<Bond> getBonds1() {
        return bonds1;
    }

    public List<Atom> removeAtoms1() {
        atoms1 = new ArrayList<>();
        return atoms1 ;
    }

    public List<Bond> removeBonds1() {
        bonds1=new ArrayList<>();
        return bonds1;
    }
    /**
     * 原子序列
     **/
    public List<Atom> atoms1 = new ArrayList<>() ;
    /**
     * 化学键序列
     **/
    public List<Bond> bonds1=new ArrayList<>();
    /**
     * 连接关系
     **/
    public List<Direction> directions1 = new ArrayList<>();

    // 当前正在绘制的笔画
    Stroke stroke;

    //记录橡皮擦产生的临时画笔
    Stroke tempStroke;

    // 记录所有笔画的结构
    Script script = new Script();

    //记录记录擦除痕迹的临时结构
    Script tempScript = new Script();

    // 默认线宽
    float strokeWidth = 16;

    //int checkstate = 0;

    DetectorInterface detector;
    MainActivity mainActivity;


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

    public List<List<PointF>> getScript() {
        return script.get();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(drawingstate==0){
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
        }
        else if(drawingstate==1){
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (event.getPointerCount() > 1) {
                        break;
                    }
                    tempStroke = new Stroke(Color.valueOf(Color.WHITE),
                            SystemClock.currentThreadTimeMillis());
                    tempStroke.add(new PointF(event.getX(), event.getY()), strokeWidth);
                    tempScript.add(tempStroke);
                    //script.add(tempStroke);
                    era_binX = event.getX();
                    era_binY = event.getY();
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    tempStroke.add(new PointF(event.getX(), event.getY()), strokeWidth);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    tempStroke.add(new PointF(event.getX(), event.getY()), strokeWidth);
                    era_endX = event.getX();
                    era_endY = event.getY();
                    era_cenX = (era_binX+era_endX)/2.0;
                    era_cenY = (era_binY+era_endY)/2.0;
                    for (Stroke s : script.data){
                        if(((s.data.get(0).x <= era_cenX && era_cenX <= s.data.get(s.data.size()-1).x) ||
                                (s.data.get(0).x >= era_cenX && era_cenX >= s.data.get(s.data.size()-1).x)) &&
                                ((s.data.get(0).y <= era_cenY && era_cenY <= s.data.get(s.data.size()-1).y) ||
                                        (s.data.get(0).y >= era_cenY && era_cenY >= s.data.get(s.data.size()-1).y))){
                            script.data.remove(s);
                            invalidate();
                            era_cenX = (double)0.0;
                            era_cenY = (double)0.0;
                            break;
                        }
                    }
                    invalidate();
                    break;
        }
        }else if (drawingstate==2){
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d(TAG, "onClick: "+getDrawingstate());
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    break;
        }}
        return true;
    }

    /**
     * view转bitmap
     */
    public Bitmap viewConversionBitmap(View v) {
        int w = v.getWidth();
        int h = v.getHeight();

        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);

        c.drawColor(Color.WHITE);
        /** 如果不设置canvas画布为白色，则生成透明 */

        v.layout(0, 0, w, h);
        v.draw(c);

        return bmp;
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
                script.data.get(i).data.get(j).x = (float) ((cos(r) * x1 + sin(r) * y1) + cx);
                script.data.get(i).data.get(j).y = (float) ((-sin(r) * x1 + cos(r) * y1) + cy);
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
        //Log.d(TAG, mainActivity.a);
        Log.d(TAG, "draw...");
            if (drawingstate != 2) {
                Log.d(TAG, "draw...");
                Log.d(TAG, "onClick: "+getDrawingstate());
                canvas.save();
                // canvas 关联到当前 View，把 script 全部重绘到当前 View
                script.drawBy(canvas);
                canvas.restore();
            }else {
                Log.d(TAG, "draw...");
                //Log.d(TAG, "__________");
                //for (Atom atom : atoms1) {
                //    Log.d(TAG, atom.toString());
                //}
                //for (Bond bond : bonds1) {
                //    Log.d(TAG, bond.toString());
                //}
                Log.d(TAG, "onClick: "+getDrawingstate());
                for (Atom atom : atoms1) {
                    paint.setTextSize(100);
                    paint.setFakeBoldText(true);
                    paint.setColor(Color.BLACK);
                    //Log.d(TAG, atom.toString());
                    switch (atom.getId()){
                        case 5:
                            canvas.save();
                            Log.d(TAG, "__________");
                            Log.d(TAG, atom.toString());
                            canvas.drawText("C", atom.getRect().left, atom.getRect().bottom, paint);
                            //canvas.drawRect(atom.getRect(),paint);
                            canvas.restore();
                            break;
                        case 6:
                            canvas.save();
                            canvas.drawText("H", atom.getRect().left, atom.getRect().bottom, paint);
                            canvas.restore();
                            break;
                        case 7:
                            canvas.save();
                            canvas.drawText("O", atom.getRect().left, atom.getRect().bottom, paint);
                            canvas.restore();
                            break;
                        case 8:
                            canvas.save();
                            canvas.drawText("N", atom.getRect().left, atom.getRect().bottom, paint);
                            canvas.restore();
                            break;
                        case 9:
                            canvas.save();
                            canvas.drawText("P", atom.getRect().left, atom.getRect().bottom, paint);
                            canvas.restore();
                            break;
                        case 10:
                            canvas.save();
                            canvas.drawText("S", atom.getRect().left, atom.getRect().bottom, paint);
                            canvas.restore();
                            break;
                    }
                }
                int size  =bonds1.size();
                int temp = 0;
                for (Bond bond : bonds1) {
                        paint.setStrokeWidth(strokeWidth);
                        Log.d(TAG, bond.toString());
                        switch (bond.getId()){
                            case 1:
                                if(directions1.get(temp) ==Direction.LeftBottom_RightTop){
                                    canvas.drawLine(bond.getRect().left, bond.getRect().bottom, bond.getRect().right, bond.getRect().top, paint);
                                    //canvas.drawRect(bond.getRect(),paint);
                                    temp++;
                                }else if(directions1.get(temp)==Direction.LeftTop_RightBottom){
                                    canvas.drawLine(bond.getRect().left, bond.getRect().top, bond.getRect().right, bond.getRect().bottom, paint);
                                    //canvas.drawRect(bond.getRect(),paint);
                                    temp++;
                                }else {
                                    canvas.drawLine(bond.getStartPoint().x, bond.getStartPoint().y, bond.getEndPoint().x, bond.getEndPoint().y, paint);
                                    //canvas.drawRect(bond.getRect(),paint);
                                    temp++;
                                }
                                break;
                            case 2:
                                if(directions1.get(temp)==Direction.LeftBottom_RightTop){
                                    canvas.drawLine(bond.getRect().left, bond.getRect().bottom, bond.getRect().right, bond.getRect().top, paint);
                                    canvas.drawLine(bond.getRect().left, bond.getRect().bottom-strokeWidth*2, bond.getRect().right, bond.getRect().top-strokeWidth*2, paint);
                                    temp++;
                                }else if(directions1.get(temp)==Direction.LeftTop_RightBottom){
                                    canvas.drawLine(bond.getRect().left, bond.getRect().top, bond.getRect().right, bond.getRect().bottom, paint);
                                    canvas.drawLine(bond.getRect().left, bond.getRect().top-strokeWidth*2, bond.getRect().right, bond.getRect().bottom-strokeWidth*2, paint);
                                    temp++;
                                }else {
                                    canvas.drawLine(bond.getStartPoint().x, bond.getStartPoint().y, bond.getEndPoint().x, bond.getEndPoint().y, paint);
                                    canvas.drawLine(bond.getStartPoint().x,bond.getStartPoint().y-strokeWidth*2,bond.getEndPoint().x,bond.getEndPoint().y-strokeWidth*2,paint);
                                    temp++;
                                }
                                break;
                                //canvas.drawLine(bond.getStartPoint().x,bond.getStartPoint().y-strokeWidth*2,bond.getEndPoint().x,bond.getEndPoint().y-strokeWidth*2,paint);
                                //break;
                            case 3:
                                break;
                            case 4:
                                break;
                        }

                }
                removeAtoms1();
                removeBonds1();
                //drawingstate=0;
                //if(abs(data.get(0).y-data.get(data.size()-1).y)>=3*widths.get(0)){
                // 防止size=1时，漏掉一个点
                //   paint.setStrokeWidth(widths.get(0));
                //    canvas.drawLine(data.get(0).x, data.get(0).y,
                //            data.get(0).x, data.get(0).y, paint);
                // paint.setStrokeWidth(widths.get(0));
                //canvas.drawLine(data.get(0).x, data.get(0).y, data.get(data.size()-1).x, data.get(data.size()-1).y, paint);
                //}else {
                //    paint.setColor(Color.BLACK);
                //    paint.setStyle(Paint.Style.STROKE);
                //    RectF oval = new RectF(data.get(0).x-6*widths.get(0), data.get(0).y-6*widths.get(0), data.get(0).x, data.get(0).y);

                //    canvas.drawArc(oval,148,240,false,paint);
                //}
                //双线
                //canvas.drawLine(data.get(0).x, data.get(0).y,
                //       data.get(data.size()-1).x, data.get(data.size()-1).y, paint);
                //canvas.drawLine(data.get(0).x, data.get(0).y-widths.get(0)*2,
                //       data.get(data.size()-1).x, data.get(data.size()-1).y-widths.get(0)*2, paint);
                //C
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
            List<PointF> res = new ArrayList<>();
            for (PointF p : data) {
                res.add(new PointF(p.x, p.y));
            }
            return res;
        }

        /**
         * 向 canvas 绘制这个笔画
         *
         * @param canvas
         */
        public void drawBy(Canvas canvas) {
            if(drawingstate != 2){
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
    }



    /**
     * 表示一组笔画的集合
     */
    class Script implements Cloneable {
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
            List<List<PointF>> lists = new ArrayList<>();
            for (Stroke s : data) {
                List<PointF> tmp = new ArrayList<>();
                for (PointF p : s.get()) {
                    tmp.add(new PointF(p.x, p.y));
                }
                lists.add(tmp);
            }
            return lists;
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