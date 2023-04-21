package com.zhanghao.player

import android.content.Context
import android.util.AttributeSet
import android.view.Surface
import android.view.SurfaceView
import kotlin.concurrent.thread

class VideoPlayer(context: Context, attributeSet: AttributeSet) :
    SurfaceView(context, attributeSet) {
    fun startPlay(string: String) {
        thread {
            start(string, holder.surface)
        }
    }

    fun stopPlay() {
        stop()
    }

    fun getFFmpegVersion(): String {
        return getVersion()
    }

    private external fun getVersion(): String
    private external fun start(string: String, surface: Surface)
    private external fun stop()

    companion object {
        init {
            System.loadLibrary("media_player")
        }
    }
}