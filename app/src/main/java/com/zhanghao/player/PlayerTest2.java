package com.zhanghao.player;

import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.yancy.yuvutils.ImageUtils;
import com.yancy.yuvutils.YuvUtils;
import com.yuv.tool.YuvTool;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class PlayerTest2 {
    public TextureView textureView;
    public IjkMediaPlayer ijkMediaPlayer3;
    private native String frameInit();
    private native int frameClose();
    private native void frameWrite(byte[] bytes,int width,int height,int length);
    public PlayerTest2(){
        System.loadLibrary("sysconfig");
        frameInit();
    }

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
            ijkMediaPlayer3.setLogEnabled(true);
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
                    //byte[] nv21 = ImageUtils.rgb565ToNV21(rgb565,w,h);//直接转有问题
                    //先转i420再转nv21
//                    byte[] i420 = ImageUtils.rgb565ToI420(rgb565,w,h);
//                    byte[] nv21 = ImageUtils.i420ToNV21(i420,w,h);
//                    byte[] i420 = YuvTool.RGB565ToI420(rgb565,w,h);
//                    byte[] nv21 = YuvTool.I420ToNV21(i420,w,h);
                    frameWrite(rgb565,w,h,rgb565.length);
                    Log.d("debug==","w:"+w+" h:"+h+" length:"+rgb565.length);
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
            frameClose();
        }
    }
}
