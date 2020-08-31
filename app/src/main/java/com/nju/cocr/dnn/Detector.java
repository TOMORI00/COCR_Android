package com.nju.cocr.dnn;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.nnapi.NnApiDelegate;

import java.nio.ByteBuffer;
import java.util.*;

import static com.nju.cocr.dnn.Config.*;

public class Detector implements DetectorInterface {
    Interpreter interpreter = null;
    int threadNum = 2;
    boolean isNNAPIEnabled = false;
    float iouThresh = 0.5f, confThresh = 0.5f;


    private Detector() {

    }

    public static Detector getInstance() {
        return Holder.instance;
    }

    @Override
    public boolean initialize(AssetManager assetManager) {
        ByteBuffer weights = Utils.readByteBufferFromAssets(assetManager, MODEL_FILE_NAME);
        if (weights == null) {
            return false;
        }
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(getThreadNum());
        if (IsNNAPIEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                options.addDelegate(new NnApiDelegate());
            }
        }
        close();
        interpreter = new Interpreter(weights, options);
        return true;
    }

    @Override
    public int getThreadNum() {
        return threadNum;
    }

    @Override
    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    @Override
    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }

    @Override
    public boolean IsNNAPIEnabled() {
        return isNNAPIEnabled;
    }

    @Override
    public void enableNNAPI(boolean isNNAPIEnabled) {
        this.isNNAPIEnabled = isNNAPIEnabled;
    }

    @Override
    public float getIOUThresh() {
        return iouThresh;
    }

    @Override
    public void setIOUThresh(float thresh) {
        iouThresh = thresh;
    }

    @Override
    public float getConfThresh() {
        return confThresh;
    }

    @Override
    public void setConfThresh(float thresh) {
        confThresh = thresh;
    }

    @Override
    public List<Recognition> getRecognition(Bitmap bitmap) {
        ByteBuffer input = Utils.convertBitmapToByteBuffer(bitmap);

        Map<Integer, Object> outputMap = new HashMap<>();
        int gridNum = input.capacity() / 1024 * (CLASS_NUM + LOC_PARM_NUM) / SIZE_PER_PIX / SIZE_FLOAT;
        outputMap.put(0, new float[1][gridNum][LOC_PARM_NUM]);
        outputMap.put(1, new float[1][gridNum][CLASS_NUM]);
        interpreter.runForMultipleInputsOutputs(new Object[]{input}, outputMap);
        float[][][] bboxes = (float[][][]) outputMap.get(0);
        float[][][] out_score = (float[][][]) outputMap.get(1);

        float maxConf, xx, yy, ww, hh;
        int macConfClassIndex;
        float[] probs = new float[CLASS_NUM];
        List<Recognition> objects = new ArrayList<>();
        for (int i = 0; i < gridNum; i++) {
            maxConf = 0;
            macConfClassIndex = -1;
            for (int c = 0; c < CLASS_NUM; c++) {
                probs[c] = out_score[0][i][c];
            }
            for (int c = 0; c < CLASS_NUM; ++c) {
                if (probs[c] > maxConf) {
                    macConfClassIndex = c;
                    maxConf = probs[c];
                }
            }
            if (maxConf > getConfThresh()) {
                xx = bboxes[0][i][0];
                yy = bboxes[0][i][1];
                ww = bboxes[0][i][2];
                hh = bboxes[0][i][3];
                final RectF rectF = new RectF(
                        Math.max(0, xx - ww / 2),
                        Math.max(0, yy - hh / 2),
                        Math.min(bitmap.getWidth() - 1, xx + ww / 2),
                        Math.min(bitmap.getHeight() - 1, yy + hh / 2));
                objects.add(new Recognition(macConfClassIndex, maxConf, rectF));
            }
        }
        return nms(objects);
    }

    @Override
    public List<Recognition> getRecognition(List<List<PointF>> script) {
        Bitmap bitmap = Utils.convertScriptToBitmap(
                Utils.normalizeScript(script));
        return getRecognition(bitmap);
    }

    public List<Recognition> nms(List<Recognition> inputs) {
        ArrayList<Recognition> nmsList = new ArrayList<>();

        for (int k = 0; k < CLASS_NUM; k++) {
            //1.find max confidence per class
            PriorityQueue<Recognition> pq = new PriorityQueue<Recognition>(
                    inputs.size() / 10,
                    new Comparator<Recognition>() {
                        @Override
                        public int compare(final Recognition lhs, final Recognition rhs) {
                            // Intentionally reversed to put high confidence at the head of the queue.
                            return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                        }
                    });

            for (int i = 0; i < inputs.size(); ++i) {
                if (inputs.get(i).getIndex() == k) {
                    pq.add(inputs.get(i));
                }
            }

            //2.do non maximum suppression
            while (pq.size() > 0) {
                //insert detection with max confidence
                Recognition[] a = new Recognition[pq.size()];
                Recognition[] detections = pq.toArray(a);
                Recognition max = detections[0];
                nmsList.add(max);
                pq.clear();

                for (int j = 1; j < detections.length; j++) {
                    Recognition detection = detections[j];
                    RectF b = detection.getBoundingBox();
                    if (iou(max.getBoundingBox(), b) < getIOUThresh()) {
                        pq.add(detection);
                    }
                }
            }
        }
        return nmsList;
    }

    public float iou(RectF a, RectF b) {
        return box_intersection(a, b) / box_union(a, b);
    }

    protected float box_intersection(RectF a, RectF b) {
        float w = overlap((a.left + a.right) / 2, a.right - a.left,
                (b.left + b.right) / 2, b.right - b.left);
        float h = overlap((a.top + a.bottom) / 2, a.bottom - a.top,
                (b.top + b.bottom) / 2, b.bottom - b.top);
        if (w < 0 || h < 0) return 0;
        float area = w * h;
        return area;
    }

    protected float overlap(float x1, float w1, float x2, float w2) {
        float l1 = x1 - w1 / 2;
        float l2 = x2 - w2 / 2;
        float left = l1 > l2 ? l1 : l2;
        float r1 = x1 + w1 / 2;
        float r2 = x2 + w2 / 2;
        float right = r1 < r2 ? r1 : r2;
        return right - left;
    }

    protected float box_union(RectF a, RectF b) {
        float i = box_intersection(a, b);
        float u = (a.right - a.left) * (a.bottom - a.top) + (b.right - b.left) * (b.bottom - b.top) - i;
        return u;
    }

    static class Holder {
        private static Detector instance = new Detector();
    }

}
