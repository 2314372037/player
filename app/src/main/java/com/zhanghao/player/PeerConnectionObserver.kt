package com.zhanghao.player

import android.util.Log
import org.webrtc.*

open class PeerConnectionObserver : PeerConnection.Observer {
    companion object {
        private const val TAG = "PeerConnectionObserver->"
    }

    override fun onIceCandidate(iceCandidate: IceCandidate?) {
        Log.i(TAG,"onIceCandidate")
    }

    override fun onDataChannel(dataChannel: DataChannel?) {
        Log.i(TAG,"onDataChannel")
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        Log.i(TAG,"onIceConnectionReceivingChange:$p0")
    }

    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
        Log.i(TAG,"onIceConnectionChange:${iceConnectionState}")
    }

    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {
        Log.i(TAG,"onConnectionChange:${newState}")
    }

    override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState?) {
        Log.i(TAG,"onIceGatheringChange:${iceGatheringState}")
    }

    override fun onAddStream(mediaStream: MediaStream?) {
        Log.i(TAG,"onAddStream")
    }

    override fun onSignalingChange(signalingState: PeerConnection.SignalingState?) {
        Log.i(TAG,"onSignalingChange:${signalingState}")
    }

    override fun onIceCandidatesRemoved(array: Array<out IceCandidate>?) {
        Log.i(TAG,"onIceCandidatesRemoved")
    }

    override fun onRemoveStream(mediaStream: MediaStream?) {
        Log.i(TAG,"onRemoveStream")
    }

    override fun onRenegotiationNeeded() {
        Log.i(TAG,"onRenegotiationNeeded")
    }

    override fun onAddTrack(rtpReceiver: RtpReceiver?, array: Array<out MediaStream>?) {
        Log.i(TAG,"onDataChannel")
    }
}