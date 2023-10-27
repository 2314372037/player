package com.zhanghao.player

import android.widget.ImageView
import com.yancy.yuvutils.ImageUtils.i420ToBitmap565
import com.yancy.yuvutils.ImageUtils.i420ToNV21
import org.webrtc.SurfaceViewRenderer

object KotlinTest {
    private var webRTCPlayer:WebRTCPlayer? = null

    fun test(activity: MainActivity,source:String,imgs:List<ImageView>) {
        if (webRTCPlayer == null) {
            webRTCPlayer = WebRTCPlayer()
        } else {
            webRTCPlayer?.release()
            webRTCPlayer = WebRTCPlayer()
        }
        webRTCPlayer?.setDataSource(source)

        webRTCPlayer?.play(activity, object : WebRTCPlayer.OnVideoFrameUpdateListener {
            override fun onFrameUpdate(width: Int, height: Int, bytes: ByteArray) {
                //val nv21 = i420ToNV21(bytes, width, height)
                val bitmap = i420ToBitmap565(bytes,width,height)
                activity.runOnUiThread {
                    for (img in imgs){
                        img.setImageBitmap(bitmap)
                    }
                }
            }
        })
    }
}