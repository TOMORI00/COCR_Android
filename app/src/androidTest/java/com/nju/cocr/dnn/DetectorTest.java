package com.nju.cocr.dnn;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.util.Log;
import androidx.test.core.app.ApplicationProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DetectorTest {
    static final String TAG = "DetectorTest";
    DetectorInterface detector;
    List<List<PointF>> nHeptane;// 正庚烷的笔迹
    Context ctx;

    @Before
    public void setUp() throws Exception {
        detector = Detector.getInstance();
        detector.setThreadNum(4);
        detector.enableNNAPI(true);
        detector.setConfThresh(0.3f);
        detector.setIOUThresh(0.3f);
        ctx = ApplicationProvider.getApplicationContext();
        detector.initialize(ctx.getAssets());
        nHeptane = new ArrayList<List<PointF>>() {{
            add(new ArrayList<PointF>() {{
                add(new PointF(0, 1));
                add(new PointF(1.73f, 0));
            }});
            add(new ArrayList<PointF>() {{
                add(new PointF(1.73f, 0));
                add(new PointF(3.46f, 1));
            }});
            add(new ArrayList<PointF>() {{
                add(new PointF(3.46f, 1));
                add(new PointF(5.91f, 0));
            }});
            add(new ArrayList<PointF>() {{
                add(new PointF(0, 1));
                add(new PointF(-1.73f, 0));
            }});
            add(new ArrayList<PointF>() {{
                add(new PointF(-1.73f, 0));
                add(new PointF(-3.46f, 1));
            }});
            add(new ArrayList<PointF>() {{
                add(new PointF(-3.46f, 1));
                add(new PointF(-5.91f, 0));
            }});
        }};
    }

    @After
    public void tearDown() throws Exception {
        detector.close();
    }

    void runForBitmap(final String filename, int trueNum, boolean showDetails) {
        Bitmap image = Utils.readBitmapFromAssets(ctx.getAssets(), filename);
        long start = System.currentTimeMillis();
        List<Recognition> objects = detector.getRecognition(image);
        long end = System.currentTimeMillis();
        Log.d(TAG, "cost " + (end - start) + "ms");
        if (showDetails) {
            Log.d(TAG, "find " + objects.size() + " items:" + objects);
        }
        assertEquals(objects.size(), trueNum);
        image.recycle();
    }

    @Test
    public void getRecognitionForBitmap() {
        runForBitmap("testcase/1a0h.jpg", 28, false);
        runForBitmap("testcase/234d.jpg", 51, false);
    }


    @Test
    public void getRecognitionForScript() {
        Bitmap image = Utils.convertScriptToBitmap(Utils.normalizeScript(nHeptane));
        long start = System.currentTimeMillis();
        List<Recognition> objects = detector.getRecognition(image);
        long end = System.currentTimeMillis();
        Log.d(TAG, "start @" + start);
        Log.d(TAG, "end @" + end);
        Log.d(TAG, "cost " + (end - start) + "ms");
        Log.d(TAG, "find " + objects.size() + " items:" + objects);
        assertEquals(objects.size(), 6);// 直链烷烃，7个原子6根键
        image.recycle();
    }
}