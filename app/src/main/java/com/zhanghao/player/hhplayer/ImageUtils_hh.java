package com.zhanghao.player.hhplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

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

    public static Bitmap nv21ToBitmap2(Context context, byte[] nv21, int width, int height) {
        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
        Type.Builder yuvType = null;
        yuvType = (new Type.Builder(rs, Element.U8(rs))).setX(nv21.length);
        Allocation in = Allocation.createTyped(rs, yuvType.create(), 1);
        Type.Builder rgbaType = (new Type.Builder(rs, Element.RGBA_8888(rs))).setX(width).setY(height);
        Allocation out = Allocation.createTyped(rs, rgbaType.create(), 1);
        in.copyFrom(nv21);
        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);
        Bitmap bmpout = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        out.copyTo(bmpout);
        return bmpout;
    }

    /**
     * 根据给定的宽和高进行拉伸
     *
     * @param origin 原图
     * @param newWidth 新图的宽
     * @param newHeight 新图的高
     * @return new Bitmap
     */
    private static Bitmap scaleBitmap(Bitmap origin, int newWidth, int newHeight,int rotation) {
        if (origin == null) {
            return null;
        }
        int height = origin.getHeight();
        int width = origin.getWidth();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.preRotate(rotation);
        matrix.preScale(scaleWidth, scaleHeight);// 使用后乘
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (!origin.isRecycled()) {
            origin.recycle();
        }
        return newBM;
    }


    public static byte[] nv21Scale(byte[] nv21, int nv21Width, int nv21Height, int newWidth, int newHeight,int rotation){
        Bitmap bitmap = nv21ToBitmap(nv21,nv21Width,nv21Height);
        if (bitmap!=null){
            bitmap = scaleBitmap(bitmap,newWidth,newHeight,rotation);
//            bitmap = Bitmap.createScaledBitmap(bitmap,newWidth,newHeight,false);//变换
            if (bitmap!=null){
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                //2.bitmap.getPixels()获取图片所有像素点数据，可以得到RGB数据的数组
                int[] argb = new int[width * height];
                bitmap.getPixels(argb, 0, width, 0, 0, width, height);
                //3.根据RGB数组采样分别获取Y，U，V数组，并存储为NV21格式的数组
                byte[] yuv = new byte[width * height * 3 / 2];

                int yIndex = 0;
                int uvIndex = width * height;
                int R, G, B, Y, U, V;
                int index = 0;
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
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
