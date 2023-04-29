package com.zh.ffmpegsdk;

import android.view.Surface;

public class VideoFrame {
    public interface OnFrameDataCallBackListener {
        void call(byte[] data,int[] linesize,int width,int height);
    }

    private OnFrameDataCallBackListener onFrameDataCallBackListener;

    public String getInfo() {
        return getVersion();
    }

    public void startGetFrame(String path,Surface surface, OnFrameDataCallBackListener onFrameDataCallBackListener) {
        this.onFrameDataCallBackListener = onFrameDataCallBackListener;
        start(path,surface);
    }

    public void stopGetFrame() {
        stop();
    }

    //native回调方法
    private void frameDataCallBack(byte[] data,int[] linesize,int width,int height) {
        if (onFrameDataCallBackListener != null) {
            onFrameDataCallBackListener.call(data,linesize,width,height);
        }
    }

    private native String getVersion();

    private native void start(String path, Surface surface);

    private native void stop();

    static {
        System.loadLibrary("media_handler");
    }
}
