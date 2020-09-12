package com.nju.cocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.nju.cocr.dnn.Detector;
import com.nju.cocr.dnn.DetectorInterface;
import com.nju.cocr.dnn.Recognition;
import com.nju.cocr.dnn.Utils;
import com.nju.cocr.view.ScribbleView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "MainActivity";
    ScribbleView scribbleView;
    FloatingActionButton clsButton, ocrButton, exchangeButton, eraserButton, penButton;
    FloatingActionsMenu menuButton;
    Context ctx;
    DetectorInterface detector;
    void runForBitmap(final String filename, int trueNum, boolean showDetails) {
        Bitmap image = Utils.readBitmapFromAssets(ctx.getAssets(), filename);
        long start = System.currentTimeMillis();
        List<Recognition> objects = detector.getRecognition(image);
        long end = System.currentTimeMillis();
        Log.d(TAG, "cost " + (end - start) + "ms");
        if (showDetails) {
            Log.d(TAG, "find " + objects.size() + " items:" + objects);
        }
        image.recycle();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scribbleView = findViewById(R.id.scribble_view);
        menuButton = findViewById(R.id.menu_button);
        clsButton = findViewById(R.id.cls_button);
        ocrButton = findViewById(R.id.ocr_button);
        exchangeButton = findViewById(R.id.exchange_button);
        eraserButton = findViewById(R.id.eraser_button);
        penButton = findViewById(R.id.pen_button);

        ctx=getBaseContext();
        detector = Detector.getInstance();
        detector.setThreadNum(4);
        detector.enableNNAPI(true);
        detector.setConfThresh(0.3f);
        detector.setIOUThresh(0.3f);
        detector.initialize(ctx.getAssets());

        clsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scribbleView.setDrawingstate(0);
                scribbleView.clear();
                scribbleView.invalidate();
                Toast toast = Toast.makeText(getBaseContext(), R.string.scribble_cls, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        ocrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scribbleView.setDrawingstate(0);
                Toast toast = Toast.makeText(getBaseContext(), R.string.scribble_ocr, Toast.LENGTH_SHORT);
                toast.show();
                // runForBitmap("testcase/1a0h.jpg", 28, false);
                // runForBitmap("testcase/234d.jpg", 51, false);
                List<Recognition> objects = detector.getRecognition(scribbleView.getScript());
                Log.d(TAG,"XGD:"+scribbleView.getScript());
                Log.d(TAG,"XGD:"+objects.toString());
            }
        });
        exchangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scribbleView.setDrawingstate(0);
                Toast toast = Toast.makeText(getBaseContext(), R.string.scribble_exchange, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        eraserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scribbleView.setDrawingstate(1);
                Toast toast = Toast.makeText(getBaseContext(), R.string.scribble_exchange, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        penButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scribbleView.setDrawingstate(0);
                Toast toast = Toast.makeText(getBaseContext(), R.string.scribble_exchange, Toast.LENGTH_SHORT);
                toast.show();
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}