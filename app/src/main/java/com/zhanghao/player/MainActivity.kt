package com.zhanghao.player

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ZPermission.get(this)?.req(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )?.listener({
            edit_url.setText("https://media.w3.org/2010/05/sintel/trailer.mp4")
            //edit_url.setText("http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8")
            //edit_url.setText("https://www.apple.com/105/media/us/iphone-x/2017/01df5b43-28e4-4848-bf20-490c34a926a7/films/feature/iphone-x-feature-tpl-cc-us-20170912_1920x1080h.mp4")
            edit_url.setText("rtmp://ns8.indexforce.com/home/mystream")
            button_play.setOnClickListener {
                if (edit_url.text.toString().isEmpty()) {
                    return@setOnClickListener
                }
                videoPlayer.startPlay(edit_url.text.toString())
            }
            button_stop.setOnClickListener {
                videoPlayer?.stopPlay()
            }
        }, {
            finish()
        })
        tvInfo.text = videoPlayer?.getFFmpegVersion()
    }

    override fun onPause() {
        super.onPause()
        videoPlayer?.stopPlay()
    }
}