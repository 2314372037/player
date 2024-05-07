package com.zhanghao.player.hhplayer;

import android.util.Log;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

public class SdpAdapter_hh implements SdpObserver {
    private String TAG = "SdpObserver";

    public SdpAdapter_hh(String tag) {
        this.TAG = tag;
    }

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.d(TAG,"onCreateSuccess");
    }

    @Override
    public void onSetSuccess() {
        Log.d(TAG,"onSetSuccess");
    }

    @Override
    public void onCreateFailure(String s) {
        Log.d(TAG,"onCreateFailure:"+s);
    }

    @Override
    public void onSetFailure(String s) {
        Log.d(TAG,"onSetFailure:"+s);
    }
}
