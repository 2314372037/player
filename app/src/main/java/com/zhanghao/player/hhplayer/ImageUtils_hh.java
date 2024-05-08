package com.zhanghao.player.hhplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class ImageUtils_hh {
    /**
     * I420转nv21
     */
    public static byte[] I420Tonv21(byte[] data, int width, int height) {
        byte[] ret = new byte[data.length];
        int total = width * height;
        ByteBuffer bufferY = ByteBuffer.wrap(ret, 0, total);
        ByteBuffer bufferVU = ByteBuffer.wrap(ret, total, total / 2);
        bufferY.put(data, 0, total);
        for (int i = 0; i < total / 4; i += 1) {
            bufferVU.put(data[i + total + total / 4]);
            bufferVU.put(data[total + i]);
        }
        return ret;
    }

    //nv21转bitmap
    public static Bitmap nv21ToBitmap(byte[] nv21, int width, int height){
        YuvImage image = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream(nv21.length);
        if (!image.compressToJpeg(new Rect(0, 0, width, height), 80, jpegOutputStream)) {
            return null;
        }
        byte[] tmp = jpegOutputStream.toByteArray();
        return BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
    }

    /**
     * NV21裁剪 by lake 算法效率 11ms
     *
     * @param src    源数据
     * @param width  源宽
     * @param height 源高
     * @param left   顶点坐标
     * @param top    顶点坐标
     * @param clip_w 裁剪后的宽
     * @param clip_h 裁剪后的高
     * @return 裁剪后的数据
     */
    public static byte[] cropNV21(byte[] src, int width, int height, int left, int top, int clip_w, int clip_h) {
        if (left > width || top > height) {
            return null;
        }
        //取偶
        int x = left / 2 * 2 , y = top / 2 * 2 ;
        int w = clip_w / 2 * 2 , h = clip_h / 2 * 2 ;
        int y_unit = w * h;
        int src_unit = width * height;
        int uv = y_unit >> 1;
        byte[] nData = new byte[y_unit + uv];
        for (int i = y, len_i = y + h; i < len_i; i++) {
            for (int j = x, len_j = x + w; j < len_j; j++) {
                nData[(i - y) * w + j - x] = src[i * width + j];
                nData[y_unit + ((i - y) / 2) * w + j - x] = src[src_unit + i / 2 * width + j];
            }
        }
        return nData;
    }
}
