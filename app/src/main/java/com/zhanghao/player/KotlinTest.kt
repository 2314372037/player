package com.zhanghao.player

import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Handler
import android.os.Looper
import android.view.SurfaceView
import android.widget.ImageView
import com.drake.net.Post
import com.drake.net.utils.scopeNetLife
import com.yancy.yuvutils.ImageUtils
import org.json.JSONObject
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.EglBase
import org.webrtc.EglRenderer
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpTransceiver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoFrame
import java.io.ByteArrayOutputStream

object KotlinTest {
    private var handler: Handler? = null
    private val eglBaseContext = EglBase.create().eglBaseContext
    private val URL = "webrtc://192.168.0.103/live/livestream"

    fun test(activity: MainActivity,path: String, imageView: ImageView,surfaceViewRenderer: SurfaceViewRenderer,surfaceView: SurfaceView) {
        surfaceViewRenderer.init(eglBaseContext,null)

        //webrtc
        val runLooper = Looper.myLooper()
        val decoderFactory = DefaultVideoDecoderFactory(eglBaseContext)

        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(
                activity.applicationContext
            ).setEnableInternalTracer(true).createInitializationOptions())
        val peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()

        val rtcConfig = PeerConnection.RTCConfiguration(emptyList())
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        val peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig,object : PeerConnectionObserver(){
            override fun onAddStream(mediaStream: MediaStream?) {
                super.onAddStream(mediaStream)
                mediaStream?.let {
                    if (it.videoTracks.isEmpty().not()) {
                        it.videoTracks[0].addSink(surfaceViewRenderer)
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
                            val nv21 = ImageUtils.i420ToNV21(data,width,height)

//                                val nv21 = ImageUtils.rgb565ToNV21(bytes,width,height,180)//degree设置180后(旋转后)可解决图像颜色
//                                val nv21_1 = ImageUtils.nv21Scale(nv21,width,height,height,width)
//                                val nv21_2 = ImageUtils.nv21Rotate(nv21,width,height,180)//degree设置180后(旋转后)可解决图像颜
                            val image = YuvImage(nv21, ImageFormat.NV21, width, height, null)
                            val jpegOutputStream = ByteArrayOutputStream(nv21!!.size)
                            if (!image.compressToJpeg(Rect(0, 0, width, height), 80, jpegOutputStream)) {
                                return@addSink
                            }
                            val tmp = jpegOutputStream.toByteArray()
                            //val bitmap = ImageUtils.rgb565ToBitmap565(bytes,width,height)
                            val bitmap = BitmapFactory.decodeByteArray(tmp, 0, tmp.size)
                            if (handler == null) {
                                handler = Handler(runLooper!!)
                            }
                            handler?.post { imageView.setImageBitmap(bitmap) }
                        }
                    }
                }
            }
        })?.apply {
            addTransceiver(
                MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO,
                RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)
            )
            addTransceiver(
                MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO,
                RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY)
            )
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

                            activity.apply {
                                scopeNetLife {
                                    val result = Post<String>("http://192.168.0.103:1985/rtc/v1/play/"){
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
                }
            }, MediaConstraints())
        }
    }
}