package com.zhanghao.player;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.yancy.yuvutils.ImageUtils;
import com.zh.ffmpegsdk.MediaFFmpegMethod;

import java.io.ByteArrayOutputStream;


public class JavaTest {
    private static MediaFFmpegMethod mediaFFmpegMethod;
    private static Handler handler;

    public static void test(String path, ImageView imageView) {
        if (mediaFFmpegMethod == null) {
            mediaFFmpegMethod = new MediaFFmpegMethod();
        } else {
            mediaFFmpegMethod.reset();
            mediaFFmpegMethod = new MediaFFmpegMethod();
        }
        mediaFFmpegMethod.getInfo();
        mediaFFmpegMethod.setLoop(true);
        final Looper runLooper = Looper.myLooper();
        mediaFFmpegMethod.start(path, new MediaFFmpegMethod.CallBack() {
            @Override
            public void frame(byte[] bytes, int width, int height) {
                byte[] nv21 = ImageUtils.rgb565ToNV21(bytes,width,height);
                if (nv21!=null){
                    int newWidth = 800;
                    int newHeight = 800;
                    byte[] newNv21 = ImageUtils.nv21Scale(nv21,width,height,newWidth,newHeight);
                    if (newNv21!=null){
                        YuvImage image = new YuvImage(newNv21, ImageFormat.NV21, newWidth, newHeight, null);
                        ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream(newNv21.length);
                        if (!image.compressToJpeg(new Rect(0, 0, newWidth, newHeight), 80, jpegOutputStream)) {
                            return;
                        }
                        byte[] tmp = jpegOutputStream.toByteArray();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);

                        if (handler == null) {
                            assert runLooper != null;
                            handler = new android.os.Handler(runLooper);
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }
            }
        });
    }

    public static void stop() {
        if (mediaFFmpegMethod != null) {
            mediaFFmpegMethod.reset();
        }
    }
}
