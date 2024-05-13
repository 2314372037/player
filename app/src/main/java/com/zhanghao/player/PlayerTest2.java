package com.zhanghao.player;

import android.os.Handler;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.ImageView;

import com.yuv.tool.YuvTool;
import com.zhanghao.player.hhplayer.ImageUtils_hh;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class PlayerTest2 {
    private Handler handler = null;
    public SurfaceHolder ori_holder;
    public Surface mSurface;
    public ImageView ivPreview;
    public IjkMediaPlayer ijkMediaPlayer1;
    public IjkMediaPlayer ijkMediaPlayer2;
    public void start(String path){
        try {
            if (ori_holder != null) {
                Surface surface = ori_holder.getSurface();
                if (ijkMediaPlayer1==null){
                    ijkMediaPlayer1 = new IjkMediaPlayer();
                }else{
                    ijkMediaPlayer1.release();
                    ijkMediaPlayer1 = new IjkMediaPlayer();
                }
                ijkMediaPlayer1.setDataSource(path);
                ijkMediaPlayer1.setVolume(0f,0f);
                ijkMediaPlayer1.setLooping(true);
//                ijkMediaPlayer1.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
//                ijkMediaPlayer1.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
//                ijkMediaPlayer1.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);
//                ijkMediaPlayer1.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
//                ijkMediaPlayer1.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
                ijkMediaPlayer1.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
                ijkMediaPlayer1.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1L);
                ijkMediaPlayer1.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);
                ijkMediaPlayer1.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
                ijkMediaPlayer1.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 8);
                ijkMediaPlayer1.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100L);
                ijkMediaPlayer1.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 10240L);
                ijkMediaPlayer1.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1L);
                ijkMediaPlayer1.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0L);
                ijkMediaPlayer1.setSurface(surface);
                ijkMediaPlayer1.prepareAsync();
            }
            if (mSurface != null) {
                if (ijkMediaPlayer2==null){
                    ijkMediaPlayer2 = new IjkMediaPlayer();
                }else{
                    ijkMediaPlayer2.release();
                    ijkMediaPlayer2 = new IjkMediaPlayer();
                }
                ijkMediaPlayer2.setDataSource(path);
                ijkMediaPlayer2.setVolume(0f,0f);
                ijkMediaPlayer2.setLooping(true);
//                ijkMediaPlayer1.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
//                ijkMediaPlayer1.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
//                ijkMediaPlayer1.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);
//                ijkMediaPlayer1.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
//                ijkMediaPlayer1.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
                ijkMediaPlayer2.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
                ijkMediaPlayer2.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1L);
                ijkMediaPlayer2.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);
                ijkMediaPlayer2.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
                ijkMediaPlayer2.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 8);
                ijkMediaPlayer2.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100L);
                ijkMediaPlayer2.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 10240L);
                ijkMediaPlayer2.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1L);
                ijkMediaPlayer2.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0L);
                ijkMediaPlayer2.setSurface(mSurface);
                if (ivPreview!=null){
                    ijkMediaPlayer2.setOnVideoFrameUpdateListener(new IMediaPlayer.OnVideoFrameUpdateListener() {
                        @Override
                        public void onFrameUpdate(IMediaPlayer iMediaPlayer, int w, int h, byte[] rgb565) {
                            byte[] i420 = YuvTool.RGB565ToI420(rgb565,w,h);
                            byte[] i420C = YuvTool.I420Scale(i420,w,h,640,640,3);
                            byte[] nv21 = YuvTool.I420ToNV21(i420C,640,640);
                            ivPreview.setImageBitmap(ImageUtils_hh.nv21ToBitmap(nv21,640,640));
                        }
                    });
                }
                ijkMediaPlayer2.prepareAsync();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (ijkMediaPlayer1!=null){
            ijkMediaPlayer1.release();
            ijkMediaPlayer1 = null;
        }
        if (ijkMediaPlayer2!=null){
            ijkMediaPlayer2.release();
            ijkMediaPlayer2 = null;
        }
    }
}
