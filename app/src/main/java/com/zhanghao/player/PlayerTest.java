package com.zhanghao.player;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import com.zhanghao.player.hhplayer.ImageUtils_hh;
import com.zhanghao.player.hhplayer.WebRTCPlayer_hh;


public class PlayerTest {
    private WebRTCPlayer_hh webRTCPlayer;
    private Handler handler = null;
    public SurfaceHolder ori_holder;
    public SurfaceTexture mSurfacetexture;
    public Surface mSurface;

    public void start(String path, Context context, int finalNewWidth, int finalNewHeight) {
        if (webRTCPlayer == null) {
            webRTCPlayer = new WebRTCPlayer_hh();
        } else {
            webRTCPlayer.release();
            webRTCPlayer = new WebRTCPlayer_hh();
        }
        Looper runLooper = Looper.myLooper();
        if (runLooper == null) {
            Log.d("调试", "runLooper为空");
            return;
        }
        if (handler == null) {
            handler = new Handler(runLooper);
        }
        if (mSurfacetexture != null) {
            if (mSurface == null) {
                mSurface = new Surface(mSurfacetexture);
            } else {
                mSurface.release();
                mSurface = new Surface(mSurfacetexture);
            }
        }
        if (!path.startsWith("webrtc://")) {
            Log.d("调试", "path不是一个webrtc://地址");
            return;
        }
        webRTCPlayer.setDataSource(path);
        WebRTCPlayer_hh.OnVideoFrameUpdateListener listener = new WebRTCPlayer_hh.OnVideoFrameUpdateListener() {
            final Paint paint = new Paint();

            @Override
            public void onFrameUpdate(int width, int height, @NonNull byte[] bytes) {
                final byte[] nv21 = ImageUtils_hh.I420Tonv21(bytes, width, height);
                //这里可能需要处理图像大小转换
                final byte[] newNv21 = ImageUtils_hh.nv21Scale(nv21,width,height,finalNewWidth,finalNewHeight);
                if (ori_holder != null) {
                    Bitmap bitmap = ImageUtils_hh.nv21ToBitmap(newNv21,finalNewWidth,finalNewHeight);
                    if (bitmap != null) {
                        Canvas canvas = ori_holder.lockHardwareCanvas();
                        canvas.drawBitmap(bitmap, 0, 0, paint);
                        ori_holder.unlockCanvasAndPost(canvas);
                    }
                }
                if (mSurface != null) {
                    Bitmap bitmap = ImageUtils_hh.nv21ToBitmap(newNv21,finalNewWidth,finalNewHeight);
                    if (bitmap != null) {
                        Canvas canvas = mSurface.lockHardwareCanvas();
                        canvas.drawBitmap(bitmap, 0, 0, paint);
                        mSurface.unlockCanvasAndPost(canvas);
                    }
                }
            }
        };
        webRTCPlayer.play(context, listener);
    }

    public void stop() {
        if (webRTCPlayer!=null){
            webRTCPlayer.release();
            webRTCPlayer = null;
        }
    }
}
