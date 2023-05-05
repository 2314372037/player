package com.zhanghao.player

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.zh.ffmpegsdk.VideoFrame
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var tvInfo: TextView
    lateinit var edit_url: EditText
    lateinit var button_play: Button
    lateinit var button_stop: Button
    lateinit var button_camera: Button
    lateinit var textureView: TextureView
    var surface: Surface? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val permission = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        ActivityCompat.requestPermissions(this, permission, 1)

        tvInfo = findViewById<TextView>(R.id.tvInfo)
        edit_url = findViewById<EditText>(R.id.edit_url)
        button_play = findViewById<Button>(R.id.button_play)
        button_stop = findViewById<Button>(R.id.button_stop)
        button_camera = findViewById<Button>(R.id.button_camera)
        textureView = findViewById<TextureView>(R.id.textureView)

        //edit_url.setText("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")
        //edit_url.setText("http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8")
        //edit_url.setText("rtmp://ns8.indexforce.com/home/mystream")
        //edit_url.setText("rtmp://liteavapp.qcloud.com/live/liteavdemoplayerstreamid")
        //edit_url.setText("rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mp4")
        edit_url.setText("/sdcard/hevc.mp4")
        button_play.setOnClickListener {

        }
        button_stop.setOnClickListener {

        }
        button_camera.setOnClickListener {
            val cameraManager: CameraManager =
                getSystemService(Context.CAMERA_SERVICE) as CameraManager
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@setOnClickListener
            }
            val surface2 = Surface(textureView.surfaceTexture)
            cameraManager.openCamera(
                cameraManager.cameraIdList[0],
                object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice) {
                        camera.createCaptureSession(SessionConfiguration(
                            SessionConfiguration.SESSION_REGULAR,
                            listOf(OutputConfiguration(surface2)),
                            this@MainActivity.mainExecutor,
                            object : CameraCaptureSession.StateCallback(){
                                override fun onConfigured(session: CameraCaptureSession) {
                                    Log.d("调试", "onConfigured")
                                    val crBuild: CaptureRequest.Builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                                    crBuild.addTarget(surface2)
                                    val captureRequest: CaptureRequest = crBuild.build()
                                    session.setRepeatingRequest(captureRequest, null, null)
                                }

                                override fun onConfigureFailed(session: CameraCaptureSession) {
                                    Log.d("调试", "onConfigureFailed")
                                }
                            }
                        ))
                    }

                    override fun onDisconnected(camera: CameraDevice) {
                        Log.d("调试", "onDisconnected")
                    }

                    override fun onError(camera: CameraDevice, error: Int) {
                        Log.d("调试", "onError")
                    }
                },
                null
            )
        }
        tvInfo.text = VideoFrame().info
    }

    override fun onPause() {
        super.onPause()
    }
}