package com.zhanghao.player;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.widget.ImageView;

import com.yuv.tool.YuvTool;
import com.zhanghao.player.hhplayer.ImageUtils_hh;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class PlayerTest2 {
    public SurfaceHolder ori_holder;
    public TextureView textureView;
    public IjkMediaPlayer ijkMediaPlayer3;
    public Context context;
    public void start(String path){
        try {
            if (ijkMediaPlayer3==null){
                ijkMediaPlayer3 = new IjkMediaPlayer();
            }else{
                ijkMediaPlayer3.release();
                ijkMediaPlayer3 = new IjkMediaPlayer();
            }
            ijkMediaPlayer3.setDataSource(path);
            ijkMediaPlayer3.setVolume(0f,0f);
            ijkMediaPlayer3.setLooping(true);
            ijkMediaPlayer3.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
            ijkMediaPlayer3.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1L);
            ijkMediaPlayer3.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);
            ijkMediaPlayer3.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
            ijkMediaPlayer3.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 8);
            ijkMediaPlayer3.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100L);
            ijkMediaPlayer3.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 10240L);
            ijkMediaPlayer3.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1L);
            ijkMediaPlayer3.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0L);
            ijkMediaPlayer3.setSurface(new Surface(textureView.getSurfaceTexture()));
            ijkMediaPlayer3.setOnVideoFrameUpdateListener(new IMediaPlayer.OnVideoFrameUpdateListener() {
                @Override
                public void onFrameUpdate(IMediaPlayer iMediaPlayer, int w, int h, byte[] rgb565) {
                    byte[] i420 = YuvTool.RGB565ToI420(rgb565,w,h);
                    byte[] i420C = YuvTool.I420Scale(i420,w,h,textureView.getWidth(),textureView.getHeight(),3);
                    byte[] nv21 = YuvTool.I420ToNV21(i420C,textureView.getWidth(),textureView.getHeight());
                    Bitmap bitmap = ImageUtils_hh.nv21ToBitmap2(context,nv21,textureView.getWidth(),textureView.getHeight());
                    if (bitmap!=null){
                        if (ori_holder!=null){
                            Canvas canvas = ori_holder.lockHardwareCanvas();
                            canvas.drawBitmap(bitmap, 0, 0, null);
                            ori_holder.unlockCanvasAndPost(canvas);
                        }
                    }
                }
            });
            ijkMediaPlayer3.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (ijkMediaPlayer3!=null){
            ijkMediaPlayer3.release();
            ijkMediaPlayer3 = null;
        }
    }
}
