package com.zhanghao.player

import android.util.Log
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

/**
 * @author ShenBen
 * @date 2020/4/1 15:00
 * @email 714081644@qq.com
 */
open class SdpAdapter constructor(private val tag: String) : SdpObserver {

    override fun onSetFailure(str: String?) {
        Log.i(tag,"->onSetFailure:$str")
    }

    override fun onSetSuccess() {
        Log.i(tag,"->onSetSuccess")
    }

    override fun onCreateSuccess(description: SessionDescription?) {
        Log.i(tag,"->onCreateSuccess")
    }

    override fun onCreateFailure(s: String?) {
        Log.i(tag,"->onCreateFailure")
    }
}