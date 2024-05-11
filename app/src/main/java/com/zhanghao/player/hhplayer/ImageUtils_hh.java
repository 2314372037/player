package com.zhanghao.player.hhplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;

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
        ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
        if (!image.compressToJpeg(new Rect(0, 0, width, height), 80, jpegOutputStream)) {
            return null;
        }
        byte[] tmp = jpegOutputStream.toByteArray();
        jpegOutputStream.reset();
        return BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
    }

    public static byte[] nv21Scale(byte[] nv21, int nv21Width, int nv21Height, int newWidth, int newHeight){
        Bitmap bitmap = nv21ToBitmap(nv21,nv21Width,nv21Height);
        if (bitmap!=null){
            bitmap = Bitmap.createScaledBitmap(bitmap,newWidth,newHeight,false);//变换
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            //2.bitmap.getPixels()获取图片所有像素点数据，可以得到RGB数据的数组
            int[] argb = new int[width * height];
            bitmap.getPixels(argb, 0, width, 0, 0, width, height);
            //3.根据RGB数组采样分别获取Y，U，V数组，并存储为NV21格式的数组
            byte[] yuv = new byte[newWidth * newHeight * 3 / 2];

            int yIndex = 0;
            int uvIndex = newWidth * newHeight;
            int R, G, B, Y, U, V;
            int index = 0;
            for (int i = 0; i < newHeight; i++) {
                for (int j = 0; j < newWidth; j++) {
                    R = (argb[index] & 0xff0000) >> 16;
                    G = (argb[index] & 0xff00) >> 8;
                    B = argb[index] & 0xff;

                    Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                    U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                    V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                    yuv[yIndex++] = (byte) (Y);

                    //偶数行取U，基数行取V，并存储
                    if (i % 2 == 0 && index % 2 == 0) {
                        if (uvIndex+1<yuv.length){
                            yuv[uvIndex++] = (byte) (V);
                            yuv[uvIndex++] = (byte) (U);
                        }
                    }
                    index++;
                }
            }
            return yuv;
        }
        return null;
    }

    public static byte[] generateSolidColorNV21(int width, int height, int yValue, int uValue, int vValue) {
        // 确保宽度和高度是偶数，因为UV分量是隔行交错存储的
        width = width % 2 == 0 ? width : width - 1;
        height = height % 2 == 0 ? height : height - 1;

        // Y、U、V 数据的总大小
        int yuvSize = width * height + (width / 2) * (height / 2) * 2;
        byte[] nv21Data = new byte[yuvSize];

        // 设置 Y 分量
        for (int i = 0; i < width * height; i++) {
            nv21Data[i] = (byte) yValue;
        }

        // 设置 U 和 V 分量
        for (int i = 0; i < (width / 2) * (height / 2); i++) {
            nv21Data[width * height + i * 2] = (byte) uValue;
            nv21Data[width * height + i * 2 + 1] = (byte) vValue;
        }

        return nv21Data;
    }
}
