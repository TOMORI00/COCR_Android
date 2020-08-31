package com.nju.cocr;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.nju.cocr.dnn.Detector;
import com.nju.cocr.dnn.DetectorInterface;
import com.nju.cocr.dnn.Recognition;
import com.nju.cocr.dnn.Utils;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "MainActivity";

    private void demo1() {
        DetectorInterface detector = Detector.getInstance();
        detector.setThreadNum(2);
        detector.enableNNAPI(true);
        detector.setConfThresh(0.5f);
        detector.setIOUThresh(0.5f);
        detector.initialize(getAssets());
        Bitmap image = Utils.readBitmapFromAssets(getAssets(), "testcase/1a0h.jpg");
        long start = System.currentTimeMillis();
        Log.d(TAG, "start @" + start);
        List<Recognition> objects = detector.getRecognition(image);
        long end = System.currentTimeMillis();
        Log.d(TAG, "end @" + end);
        Log.d(TAG, "cost " + (end - start) + "ms");
        image.recycle();
        Log.d(TAG, "find " + objects.size() + " items:" + objects);
        // 建议界面处于焦点时， detector 常驻后台，不要 close
        detector.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        demo1();
    }
}