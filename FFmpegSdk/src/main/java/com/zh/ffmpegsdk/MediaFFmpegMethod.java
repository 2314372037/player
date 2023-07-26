package com.zh.ffmpegsdk;

public class MediaFFmpegMethod {
    static {
        System.loadLibrary("media_handler");
    }

    public native int init(String source);

    public native long avGetTime();

    public native int avReadFrame();

    public native boolean packetStreamIndexIsVideo();

    public native int avcodecSendPacket();

    public native int avcodecReceiveFrame();

    public native long getTimeStamp();

    public native void avUsleep(long sleepTime);

    public native byte[] getFrameRGB565ByteArray();

    public native int avFrameWidth();

    public native int avFrameHeight();

    public native void destroy();

    public native String getInfo();

    private boolean isRun = false;
    private long curTimeStamp;
    private long startTimeStamp;

    private boolean isLoop;

    public void setLoop(boolean loop) {
        isLoop = loop;
    }

    public interface CallBack {
        void frame(byte[] data, int width, int height);
    }

    public void start(String source, CallBack callBack) {
        if (isRun) {
            return;
        }
        curTimeStamp = 0;
        startTimeStamp = -1;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    do {
                        if (init(source) >= 0) {
                            isRun = true;
                            startTimeStamp = (avGetTime() / 1000) - curTimeStamp;
                            while (avReadFrame() >= 0 && isRun) {
                                if (packetStreamIndexIsVideo()) {
                                    if (avcodecSendPacket() >= 0) {
                                        if (avcodecReceiveFrame() >= 0) {
                                            curTimeStamp = getTimeStamp();
                                            byte[] rgb565 = getFrameRGB565ByteArray();
                                            int width = avFrameWidth();
                                            int height = avFrameHeight();
                                            if (callBack != null) {
                                                callBack.frame(rgb565, width, height);
                                            }

                                            //延时一段时间，不然像快放
                                            long currentSystemTime = (avGetTime() / 1000);
                                            long timeDiff = currentSystemTime - startTimeStamp;
                                            if (curTimeStamp > timeDiff) {
                                                long sleepTime = curTimeStamp - timeDiff;
                                                avUsleep(sleepTime * 1000);
                                            }
                                        }
                                    }
                                }
                            }
                            destroy();
                            isRun = false;
                            curTimeStamp = 0;
                            startTimeStamp = -1;
                        } else {
                            break;
                        }
                    } while (isLoop);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void reset() {
        isRun = false;
        isLoop = false;
        curTimeStamp = 0;
        startTimeStamp = -1;
    }
}
