package com.zh.ffmpegsdk;

import android.view.Surface;

public class VideoPlayer {
    private VideoFrame.OnFrameDataCallBackListener onFrameDataCallBackListener;

    public String getInfo() {
        return getVersion();
    }

    public void startPlay(String path, Surface surface, VideoFrame.OnFrameDataCallBackListener onFrameDataCallBackListener) {
        this.onFrameDataCallBackListener = onFrameDataCallBackListener;
        start(path, surface);
    }

    private void frameDataCallBack(byte[] data, int[] linesize, int width, int height) {
        if (onFrameDataCallBackListener != null) {
            onFrameDataCallBackListener.call(data, linesize, width, height);
        }
    }

    public void stopPlay() {
        stop();
    }

    private native String getVersion();

    private native void start(String path, Surface surface);

    private native void stop();

    static {
        System.loadLibrary("media_handler");
    }
}