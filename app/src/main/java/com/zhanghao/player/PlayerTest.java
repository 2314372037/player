package com.zhanghao.player;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.yancy.yuvutils.ImageUtils;
import com.zh.hhplayer.HHPlayer;

import java.io.ByteArrayOutputStream;

public class PlayerTest {
    private HHPlayer hhPlayer;
    private Handler handler = null;
    public SurfaceHolder ori_holder;
    public SurfaceTexture mSurfacetexture;
    public Surface mSurface;
    public ImageView imageView;
    public void start(String path, Context context,int finalNewWidth,int finalNewHeight){
        if (hhPlayer == null) {
            hhPlayer = new HHPlayer();
        } else {
            hhPlayer.stop();
            hhPlayer = new HHPlayer();
        }
        Looper runLooper = Looper.myLooper();
        if (handler == null) {
            handler = new Handler(runLooper);
        }

        if (mSurfacetexture!=null){
            if (mSurface == null) {
                mSurface = new Surface(mSurfacetexture);
            } else {
                mSurface.release();
                mSurface = new Surface(mSurfacetexture);
            }
        }
        hhPlayer.setDataSource(path, new HHPlayer.OnDataListener() {
            final Paint paint = new Paint();
            @Override
            public void callBack(@NonNull byte[] nv21, int width, int height) {
                byte[] newNv21 = ImageUtils.nv21Scale(nv21,width,height,finalNewWidth,finalNewHeight);
                if (newNv21!=null){
                    if (ori_holder!=null){
                        Bitmap bitmap = ImageUtils.i420ToBitmap565(newNv21, finalNewWidth, finalNewHeight);
                        if (bitmap!=null){
                            Canvas canvas = ori_holder.lockHardwareCanvas();
                            canvas.drawBitmap(bitmap, 0, 0, paint);
                            ori_holder.unlockCanvasAndPost(canvas);
                        }
                    }
                    if (mSurface!=null){
                        Bitmap bitmap = ImageUtils.i420ToBitmap565(newNv21, finalNewWidth, finalNewHeight);
                        if (bitmap!=null){
                            Canvas canvas = mSurface.lockHardwareCanvas();
                            canvas.drawBitmap(bitmap, 0, 0, paint);
                            mSurface.unlockCanvasAndPost(canvas);
                        }
                    }
                    if (true){
                        YuvImage image = new YuvImage(newNv21, ImageFormat.NV21, finalNewWidth, finalNewHeight, null);
                        ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream(newNv21.length);
                        if (!image.compressToJpeg(new Rect(0, 0, finalNewWidth, finalNewHeight), 80, jpegOutputStream)) {
                            return;
                        }
                        byte[] tmp = jpegOutputStream.toByteArray();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    imageView.setImageBitmap(bitmap);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }
        });
        hhPlayer.start(context);
    }

    public void stop(){
        hhPlayer.stop();
    }
}
