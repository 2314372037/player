package com.zh.ffmpegsdk;

public class MediaFrameGet {
    static {
        System.loadLibrary("media_handler");
    }

    public interface OnFrameDataCallBackListener {
        void call(byte[] data, int width, int height);
    }

    public interface OnStartListener {
        void start();
    }

    public interface OnStopListener {
        void stop();
    }

    private OnFrameDataCallBackListener onFrameDataCallBackListener;
    private static OnStartListener onStartListener;
    private OnStopListener onStopListener;

    public String getInfo() {
        return getVersion();
    }

    public void startGetFrame(String path, OnFrameDataCallBackListener onFrameDataCallBackListener) {
        this.onFrameDataCallBackListener = onFrameDataCallBackListener;
        start(path);
    }

    public void stopGetFrame() {
        stop();
    }

    public void setOnStartListener(OnStartListener onStartListener) {
        MediaFrameGet.onStartListener = onStartListener;
    }

    public void setOnStopListener(OnStopListener onStopListener) {
        this.onStopListener = onStopListener;
    }

    //native回调方法
    public void frameDataCallBack(byte[] data, int width, int height) {
        if (onFrameDataCallBackListener != null) {
            onFrameDataCallBackListener.call(data, width, height);
        }
    }

    //native回调方法
    public void stopCallBack() {
        if (onStopListener != null) {
            onStopListener.stop();
        }
    }

    //native回调方法
    public static void startCallBack() {
        if (onStartListener != null) {
            onStartListener.start();
        }
    }

    private native String getVersion();

    private native void start(String path);

    private native void stop();
}
