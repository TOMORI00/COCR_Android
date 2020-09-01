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

public class ScribbleView extends View {
    static final String TAG = "ScribbleView";
    static Paint paint;

    static {
        paint = new Paint();
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    Stroke stroke;
    Script script = new Script();
    float strokeWidth = 5;

    public ScribbleView(Context context) {
        super(context);
    }

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
    // int strokeIndex = -1, ptsIndex = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "鼠标--->down@" + event.getX() + "," + event.getY());
                stroke = new Stroke(Color.valueOf(Color.BLACK),
                        SystemClock.currentThreadTimeMillis());
                stroke.add(new PointF(event.getX(), event.getY()), strokeWidth);
                script.add(stroke);
                // ptsIndex = 0;
                // strokeIndex = script.size() - 1;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "鼠标--->move@" + event.getX() + "," + event.getY());
                stroke.add(new PointF(event.getX(), event.getY()), strokeWidth);
                // ptsIndex = stroke.size() - 1;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "鼠标--->up@" + event.getX() + "," + event.getY());
                stroke.add(new PointF(event.getX(), event.getY()), strokeWidth);
                // ptsIndex = stroke.size() - 1;
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (script != null) {
            Log.d(TAG, "draw...");
            canvas.save();
            script.drawBy(canvas);
            canvas.restore();
        }
    }

    class Stroke {
        List<PointF> data;
        List<Float> widths;
        Color color;
        long timestamp;

        public Stroke(Color color, long timestamp) {
            data = new ArrayList<>();
            widths = new ArrayList<>();
            this.color = color;
            this.timestamp = timestamp;
        }

        public int size() {
            return data.size();
        }

        public void clear() {
            data = new ArrayList<>();
            widths = new ArrayList<>();
        }

        public void add(PointF pts, float width) {
            data.add(pts);
            widths.add(width);
        }

        public List<PointF> get() {
            return data;
        }

        public void drawBy(Canvas canvas) {
            if (data.size() == 0) {
                return;
            }
            paint.setColor(color.pack());
            paint.setStrokeWidth(widths.get(0));
            canvas.drawLine(data.get(0).x, data.get(0).y,
                    data.get(0).x, data.get(0).y, paint);
            for (int i = 1; i < data.size(); i++) {
                paint.setStrokeWidth(widths.get(i));
                canvas.drawLine(data.get(i).x, data.get(i).y,
                        data.get(i - 1).x, data.get(i - 1).y, paint);
            }
        }
    }

    class Script {
        List<Stroke> data;

        public Script() {
            data = new ArrayList<>();
        }

        public int size() {
            return data.size();
        }

        public void add(Stroke stroke) {
            data.add(stroke);
        }

        public void clear() {
            data = new ArrayList<>();
        }

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

        public void drawBy(Canvas canvas) {
            for (Stroke s : data) {
                s.drawBy(canvas);
            }
        }
    }
}
