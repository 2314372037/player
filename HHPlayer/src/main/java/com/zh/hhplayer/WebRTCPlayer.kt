package com.zh.hhplayer

import android.content.Context
import com.drake.net.Post
import com.drake.net.utils.scope
import org.json.JSONObject
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpTransceiver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoFrame
import java.io.FileOutputStream

class WebRTCPlayer {
    private var URL = ""
    private var sdpRequestSource = ""
    private var peerConnection:PeerConnection? = null
    private var peerConnectionFactory:PeerConnectionFactory? = null

    fun setDataSource(source:String){
        URL = source
        val s1 = source.substring(source.indexOf("//") + 2)
        val ipString = s1.substring(0, s1.indexOf("/"))
        sdpRequestSource = "http://${ipString}:1985/rtc/v1/play/"
    }

    interface OnVideoFrameUpdateListener{
        fun onFrameUpdate(width:Int, height:Int, bytes:ByteArray)
    }

    fun play(context: Context,onVideoFrameUpdateListener: OnVideoFrameUpdateListener){
        val decoderFactory = DefaultVideoDecoderFactory(EglBase.create().eglBaseContext)

        PeerConnectionFactory.initialize(PeerConnectionFactory
                .InitializationOptions.builder(context.applicationContext)
                .setEnableInternalTracer(true)
                .createInitializationOptions())
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()

        val rtcConfig = PeerConnection.RTCConfiguration(emptyList())
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN

        peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig,object : PeerConnectionObserver(){
            override fun onAddStream(mediaStream: MediaStream?) {
                super.onAddStream(mediaStream)
                mediaStream?.let {
                    if (it.videoTracks.isEmpty().not()) {
//                        it.videoTracks[0].addSink(surfaceViewRenderer)
                        it.videoTracks[0].addSink {
                            val buffer: VideoFrame.I420Buffer = it.getBuffer().toI420()!!
                            val height = buffer.height
                            val width = buffer.width

                            val yBuffer = buffer.dataY
                            val uBuffer = buffer.dataU
                            val vBuffer = buffer.dataV

                            val yStride = buffer.strideY
                            val uStride = buffer.strideU
                            val vStride = buffer.strideV

                            val data = ByteArray(height * width * 3 / 2)
                            yBuffer[data, 0, height * width]

                            var uOffset = width * height
                            var vOffset = width * height * 5 / 4
                            for (i in 0 until height / 2) {
                                uBuffer.position(i * uStride)
                                uBuffer[data, uOffset, width / 2]
                                uOffset += width / 2
                                vBuffer.position(i * vStride)
                                vBuffer[data, vOffset, width / 2]
                                vOffset += width / 2
                            }
                            buffer.release()
                            onVideoFrameUpdateListener.onFrameUpdate(width,height,data)
                        }
                    }
                }
            }
        })?.apply {
            addTransceiver(
                MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO,
                RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)
            )
//            addTransceiver(
//                MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO,
//                RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)
//            )
        }
        peerConnection?.let { connection ->
            connection.createOffer(object : SdpAdapter("createOffer") {
                override fun onCreateSuccess(description: SessionDescription?) {
                    super.onCreateSuccess(description)
                    description?.let {
                        if (it.type == SessionDescription.Type.OFFER) {
                            val offerSdp = it.description
                            connection.setLocalDescription(SdpAdapter("setLocalDescription"), it)

                            val json = JSONObject().apply {
                                put("sdp",offerSdp)
                                put("streamurl",URL)
                            }.toString()

                            scope {
                                val result = Post<String>(sdpRequestSource){
                                    json(json)
                                }.await()
                                val sdp = JSONObject(result).getString("sdp")
                                val remoteSdp = SessionDescription(
                                    SessionDescription.Type.ANSWER,
                                    sdp
                                )
                                connection.setRemoteDescription(
                                    SdpAdapter("setRemoteDescription"),
                                    remoteSdp
                                )
                            }
                        }
                    }
                }
            }, MediaConstraints())
        }
    }

    fun release(){
        peerConnection?.dispose()
        peerConnectionFactory?.dispose()
    }
}