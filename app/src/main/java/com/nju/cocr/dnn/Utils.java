package com.nju.cocr.dnn;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.List;

import static com.nju.cocr.dnn.Config.*;


public class Utils {
    /**
     * 把笔迹点缩放到合适尺度
     *
     * @param script List<List<PointF>> 原始笔迹
     * @return List<List < PointF>> 缩放后的笔迹
     */
    static public List<List<PointF>> normalizeScript(List<List<PointF>> script) {
        // TODO: 平移、比例变换
        return script;
    }

    /**
     * 把笔迹点转换为像素图
     *
     * @param script List<List<PointF>> 笔迹
     * @return Bitmap SIZE_FIXED_INPUT x SIZE_FIXED_INPUT x 3
     */
    static public Bitmap convertScriptToBitmap(List<List<PointF>> script) {
        Bitmap bitmap = Bitmap.createBitmap(SIZE_FIXED_INPUT, SIZE_FIXED_INPUT,
                Bitmap.Config.RGB_565, false);
        bitmap.eraseColor(Color.WHITE);
        Canvas canvas = new Canvas(bitmap);
        for (List<PointF> stroke : script) {
            if (stroke.size() == 0) {
                continue;
            }
            canvas.drawLine(stroke.get(0).x, stroke.get(0).y,
                    stroke.get(0).x, stroke.get(0).y, DETECTOR_PAINTER);
            for (int i = 1; i < stroke.size(); i++) {
                canvas.drawLine(stroke.get(i).x, stroke.get(i).y,
                        stroke.get(i - 1).x, stroke.get(i - 1).y, DETECTOR_PAINTER);
            }
        }
        return bitmap;
    }

    /**
     * 把原图转化为模型输入，丢弃右下侧 size % 32 的部分
     *
     * @param bitmap
     * @return ByteBuffer modelInput
     */
    static public ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        final int oldWidth = bitmap.getWidth();
        final int oldHeight = bitmap.getHeight();
        final int newWidth = oldWidth - oldWidth % 32;
        final int newHeight = oldHeight - oldHeight % 32;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(
                SIZE_FLOAT * newWidth * newHeight * SIZE_PER_PIX).order(ByteOrder.nativeOrder());
        int[] intBuffer = new int[newWidth * newHeight];
        bitmap.getPixels(intBuffer, 0, oldWidth, 0, 0, oldWidth, oldHeight);
        for (int i = 0; i < newHeight; i++) {
            for (int j = 0; j < newWidth; j++) {
                final int tmp = intBuffer[i * oldWidth + j];
                byteBuffer.putFloat(((tmp >> 16) & 0xFF) / 255.0f);
                byteBuffer.putFloat(((tmp >> 8) & 0xFF) / 255.0f);
                byteBuffer.putFloat((tmp & 0xFF) / 255.0f);
            }
        }
        return byteBuffer;
    }

    /**
     * 从 assets目录读取整个文件，返回 ByteBuffer
     *
     * @param assetManager getAssets() 方法
     * @param filename     文件名，相对于 assets 目录的路径
     * @return ByteBuffer 文件内容，读取失败返回 null
     */
    static public ByteBuffer readByteBufferFromAssets(
            AssetManager assetManager, final String filename) {
        AssetFileDescriptor fileDescriptor;
        try {
            fileDescriptor = assetManager.openFd(filename);
        } catch (IOException e) {
            return null;
        }
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        ByteBuffer result;
        try {
            result = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        } catch (IOException e) {
            result = null;
        }
        return result;
    }

    static public Bitmap readBitmapFromAssets(
            AssetManager assetManager, final String filename) {
        Bitmap image;
        try {
            InputStream is = assetManager.open(filename);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            image = null;
            e.printStackTrace();
        }
        return image;
    }

}
