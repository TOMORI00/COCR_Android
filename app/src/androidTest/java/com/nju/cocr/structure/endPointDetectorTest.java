package com.nju.cocr.structure;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;

import com.nju.cocr.R;
import com.nju.cocr.dnn.Detector;
import com.nju.cocr.dnn.DetectorInterface;
import com.nju.cocr.dnn.Recognition;
import com.nju.cocr.dnn.Utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class endPointDetectorTest {
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

    @Test
    public void insertDotTest(){
        String TAG = "insertDotTest";
        PointF startPoint = new PointF(1,1);
        PointF endPoint = new PointF(15,15);
        List<List<PointF>> paths = new ArrayList<>();
        List<PointF> path = new ArrayList<>();
        path.add(startPoint);
        path.add(endPoint);
        paths.add(path);
        endpointDetector e = new endpointDetector(paths,null);
        List<PointF> result = e.insertDots();
        Log.d(TAG, "insertDotTest: "+result);
        assertEquals(11,result.size());
    }

    @Test
    public void detectTest(){
        String TAG = "detectTest";
        Bitmap image = Utils.convertScriptToBitmap(Utils.normalizeScript(nHeptane));
        List<Recognition> objects = detector.getRecognition(image);
        List<RectF> labels = new ArrayList<>();
        for(Recognition recognition:objects){
            //<=4的时候说明框里面是键
            if(recognition.getIndex()<=4) {
                labels.add(recognition.getBoundingBox());
            }
        }
        endpointDetector e = new endpointDetector(nHeptane,labels);
        List<Direction> result = e.detect();
        for(int i=0;i<objects.size();i++){
            Log.d(TAG, result.get(i).toString());
            Log.d(TAG,objects.get(i).toString());
        }


    }

    @After
    public void tearDown() throws Exception {
        detector.close();
    }
}
