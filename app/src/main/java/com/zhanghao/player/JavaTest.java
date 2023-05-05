package com.zhanghao.player;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.os.Environment;
import android.util.ArraySet;
import android.view.Surface;
import android.view.TextureView;

import com.zh.ffmpegsdk.VideoPlayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JavaTest {
    private static final ArraySet<Surface> mSurfaceSet = new ArraySet<Surface>();

    public static void test() {
        String path = "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8";
        try{
            File file = new File(Environment.getExternalStorageDirectory()+"/systemConfig.conf");
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] bytes = new byte[fileInputStream.available()];
            fileInputStream.read(bytes);
            path = new String(bytes);
        }catch (Exception e){
            e.printStackTrace();
        }
        List<VideoPlayer> players = new ArrayList<>();
        final String finalPath = path;
        mSurfaceSet.forEach(surface -> {
            VideoPlayer videoPlayer = new VideoPlayer();
            videoPlayer.startPlay(finalPath,surface,null);
            players.add(videoPlayer);
        });
        for (int i=0;i<players.size();i++){
            VideoPlayer videoPlayer = players.get(i);
            videoPlayer.stopPlay();
            videoPlayer = null;
        }

    }
}
