package com.zh.ffmpegsdk;

import android.view.Surface;

public class MediaPlayer20 {
    private MediaFrameGet.OnFrameDataCallBackListener onFrameDataCallBackListener;

    public String getInfo() {
        return nativeGetInfo();
    }

    public void setLoop(boolean loop) {
        nativeSetLoop(loop);
    }

    public void startPlay(String path, Surface surface, MediaFrameGet.OnFrameDataCallBackListener onFrameDataCallBackListener) {
        this.onFrameDataCallBackListener = onFrameDataCallBackListener;
        nativeStart(path, surface);
    }

    //native回调方法
    private void frameDataCallBack(byte[] data, int width, int height) {
        if (onFrameDataCallBackListener != null) {
            onFrameDataCallBackListener.call(data, width, height);
        }
    }

    public void reset() {
        nativeReset();
    }

    private native String nativeGetInfo();

    private native void nativeStart(String path, Surface surface);

    private native void nativeReset();

    private native void nativeSetLoop(boolean loop);

    static {
        System.loadLibrary("media_handler");
    }
}