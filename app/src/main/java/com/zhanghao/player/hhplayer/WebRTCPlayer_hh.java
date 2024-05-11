package com.zhanghao.player.hhplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Surface;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.EglBase;
import org.webrtc.EglRenderer;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpTransceiver;
import org.webrtc.SessionDescription;
import org.webrtc.SoftwareVideoDecoderFactory;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class WebRTCPlayer_hh {
    private String URL = "";
    private String sdpRequestSource = "";
    private PeerConnection peerConnection;
    private PeerConnectionFactory peerConnectionFactory;

    private Surface surface;

    public void setSurface(Surface surface){
        this.surface = surface;
    }

    public void setDataSource(String source) {
        URL = source;
        String s1 = source.substring(source.indexOf("//") + 2);
        String ipString = s1.substring(0, s1.indexOf("/"));
        sdpRequestSource = "http://" + ipString + ":1985/rtc/v1/play/";
    }

    public interface OnVideoFrameUpdateListener {
        void onFrameUpdate(int width, int height, byte[] bytes);
    }

    public void play(Context context, OnVideoFrameUpdateListener onVideoFrameUpdateListener) {
        PeerConnectionFactory.initialize(PeerConnectionFactory
                .InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions());

        //需要在PeerConnectionFactory初始化之后
        DefaultVideoDecoderFactory decoderFactory = new DefaultVideoDecoderFactory(EglBase.create().getEglBaseContext());

        peerConnectionFactory = PeerConnectionFactory.builder()
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(new ArrayList<>());
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, new PeerConnectionObserver_hh() {
            @Override
            public void onAddStream(MediaStream mediaStream) {
                super.onAddStream(mediaStream);
                if (mediaStream != null) {
                    if (!mediaStream.videoTracks.isEmpty()) {
                        mediaStream.videoTracks.get(0).addSink(new VideoSink() {
                            @Override
                            public void onFrame(VideoFrame videoFrame) {
                                if (videoFrame != null) {
                                    try{
                                        VideoFrame.I420Buffer buffer = videoFrame.getBuffer().toI420();
                                        if (buffer == null) {
                                            return;
                                        }
                                        int height = buffer.getHeight();
                                        int width = buffer.getWidth();

                                        ByteBuffer yBuffer = buffer.getDataY();
                                        ByteBuffer uBuffer = buffer.getDataU();
                                        ByteBuffer vBuffer = buffer.getDataV();

                                        int yStride = buffer.getStrideY();
                                        int uStride = buffer.getStrideU();
                                        int vStride = buffer.getStrideV();

                                        byte[] data = new byte[height * width * 3 / 2];
                                        yBuffer.get(data, 0, height * width);
                                        int uOffset = width * height;
                                        int vOffset = width * height * 5 / 4;
                                        for (int i = 0; i < height / 2; i++) {
                                            uBuffer.position(i * uStride);
                                            uBuffer.get(data, uOffset, width / 2);
                                            uOffset += width / 2;
                                            vBuffer.position(i * vStride);
                                            vBuffer.get(data, vOffset, width / 2);
                                            vOffset += width / 2;
                                        }
                                        buffer.release();
                                        onVideoFrameUpdateListener.onFrameUpdate(width, height, data);
                                    }catch (java.lang.OutOfMemoryError oe){
                                        Log.d("debug==","异常:"+oe.getMessage());
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
        if (peerConnection != null) {
            peerConnection.addTransceiver(MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO,
                    new RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.RECV_ONLY));
            //peerConnection.addTransceiver(MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO);
            peerConnection.createOffer(new SdpAdapter_hh("createOffer") {
                @Override
                public void onCreateSuccess(SessionDescription sessionDescription) {
                    super.onCreateSuccess(sessionDescription);
                    if (sessionDescription != null) {
                        if (sessionDescription.type == SessionDescription.Type.OFFER) {
                            String offerSdp = sessionDescription.description;
                            peerConnection.setLocalDescription(new SdpAdapter_hh("setLocalDescription"), sessionDescription);

                            JSONObject requestJson = new JSONObject();
                            try {
                                requestJson.put("sdp", offerSdp);
                                requestJson.put("streamurl", URL);
                            } catch (JSONException e) {
                                return;
                            }
                            String result = MyHttpRequest_hh.sendPost(sdpRequestSource, requestJson.toString());
                            if (result != null && !result.isEmpty()) {
                                String sdp = null;
                                try {
                                    sdp = new JSONObject(result).getString("sdp");
                                } catch (JSONException e) {
                                    return;
                                }
                                SessionDescription remoteSdp = new SessionDescription(SessionDescription.Type.ANSWER, sdp);
                                peerConnection.setRemoteDescription(new SdpAdapter_hh("setRemoteDescription"), remoteSdp);
                            }
                        }
                    }
                }
            }, new MediaConstraints());

        }
    }

    public void release() {
        if (peerConnection!=null){
            peerConnection.dispose();
        }
        if (peerConnectionFactory!=null){
            peerConnectionFactory.dispose();
        }
    }
}
