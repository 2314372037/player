package com.zh.hhplayer

import android.content.Context
import com.yancy.yuvutils.ImageUtils
import tv.danmaku.ijk.media.player.IjkMediaPlayer

class HHPlayer {
    private var ijkMediaPlayer: IjkMediaPlayer? = null
    private var webRTCPlayer: WebRTCPlayer? = null
    private var onDataListener:OnDataListener? = null
    private var isWebrtc = false
    interface OnDataListener{
        fun callBack(nv21:ByteArray,width:Int,height:Int)
    }

    fun setDataSource(path:String?,onDataListener:OnDataListener?){
        if (path.isNullOrEmpty()){
            return
        }
        this.onDataListener = onDataListener
        if (path.startsWith("webrtc://")){
            isWebrtc = true
            if (webRTCPlayer == null) {
                webRTCPlayer = WebRTCPlayer()
            } else {
                webRTCPlayer?.release()
                webRTCPlayer = WebRTCPlayer()
            }
            webRTCPlayer?.setDataSource(path)
        }else{
            isWebrtc = false
            ijkMediaPlayer = IjkMediaPlayer()
            ijkMediaPlayer?.isLooping = true
//            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
//            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);//自动旋转方向
//            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
            ijkMediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
//            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_YV12.toLong());
            ijkMediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
            ijkMediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);
            ijkMediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
            ijkMediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
            ijkMediaPlayer?.setOnVideoFrameUpdateListener { iMediaPlayer, width, height, bytes ->
                val nv21 = ImageUtils.rgb565ToNV21(bytes, width, height, 180)//先旋转180，在旋转回去
                if (nv21!=null){
                    val nv21_2 = ImageUtils.nv21Rotate(nv21, width, height, 180)//旋转回去，解决图像颜色失真
                    onDataListener?.callBack(nv21_2?: ByteArray(0),width,height)
                }
            }
            ijkMediaPlayer?.dataSource = path
        }
    }

    fun start(context: Context?){
        if (isWebrtc){
            webRTCPlayer?.play(context!!,object : WebRTCPlayer.OnVideoFrameUpdateListener {
                override fun onFrameUpdate(width: Int, height: Int, bytes: ByteArray) {
                    val nv21 = ImageUtils.i420ToNV21(bytes, width, height)
                    if (nv21!=null){
                        onDataListener?.callBack(nv21,width,height)
                    }
                }
            })
        }else{
            ijkMediaPlayer?.prepareAsync()
        }
    }

    fun stop(){
        if (isWebrtc){
            webRTCPlayer?.release()
            webRTCPlayer = null
        }else{
            ijkMediaPlayer?.stop()
            ijkMediaPlayer?.release()
        }
    }
}