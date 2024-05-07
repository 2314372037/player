package com.zhanghao.player.hhplayer;

import android.util.Log;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;

public class PeerConnectionObserver_hh implements PeerConnection.Observer{
    private String TAG = "PeerConnectionObserver";
    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        Log.d(TAG,"onSignalingChange");
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        Log.d(TAG,"onIceConnectionChange");
    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        Log.d(TAG,"onIceConnectionReceivingChange");
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        Log.d(TAG,"onIceGatheringChange");
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        Log.d(TAG,"onIceCandidate");
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        Log.d(TAG,"onIceCandidatesRemoved");
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        Log.d(TAG,"onAddStream");
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        Log.d(TAG,"onRemoveStream");
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        Log.d(TAG,"onDataChannel");
    }

    @Override
    public void onRenegotiationNeeded() {
        Log.d(TAG,"onRenegotiationNeeded");
    }
}
