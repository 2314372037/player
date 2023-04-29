package com.zhanghao.player

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.zh.ffmpegsdk.VideoPlayer
import java.io.File
import java.io.FileInputStream
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var tvInfo: TextView
    lateinit var edit_url: EditText
    lateinit var button_play: Button
    lateinit var button_stop: Button
    lateinit var button_camera: Button
    lateinit var videoPlayer: VideoPlayer
    lateinit var textureView: TextureView
    lateinit var imageView: ImageView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvInfo = findViewById<TextView>(R.id.tvInfo)
        edit_url = findViewById<EditText>(R.id.edit_url)
        button_play = findViewById<Button>(R.id.button_play)
        button_stop = findViewById<Button>(R.id.button_stop)
        button_camera = findViewById<Button>(R.id.button_camera)
        textureView = findViewById<TextureView>(R.id.textureView)
        imageView = findViewById<ImageView>(R.id.imageView)
        videoPlayer = VideoPlayer()

        val permission = arrayOf(Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(this,permission,1)

        //edit_url.setText("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")
        //edit_url.setText("http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8")
        edit_url.setText("rtmp://ns8.indexforce.com/home/mystream")
        button_play.setOnClickListener {
            if (edit_url.text.toString().isEmpty()) {
                return@setOnClickListener
            }
            var path: String? = "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8"
            try {
                val file = File(Environment.getExternalStorageDirectory().toString() + "/systemConfig.conf")
                val fileInputStream = FileInputStream(file)
                val bytes = ByteArray(fileInputStream.available())
                fileInputStream.read(bytes)
                path = String(bytes)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            videoPlayer.startPlay(path,Surface(textureView.surfaceTexture),null)
        }
        button_stop.setOnClickListener {
            videoPlayer.stopPlay()
        }
        button_camera.setOnClickListener {
            val cameraManager:CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return@setOnClickListener
            }
            val t = JavaTest()
            val surface2 = Surface(textureView.surfaceTexture)
            cameraManager.openCamera(cameraManager.cameraIdList[0],object : CameraDevice.StateCallback(){
                override fun onOpened(camera: CameraDevice) {
                    camera.createCaptureSession(
                        listOf(surface2),object : CameraCaptureSession.StateCallback(){
                            override fun onConfigured(session: CameraCaptureSession) {
                                Log.d("调试","onConfigured")
                                val crBuild: CaptureRequest.Builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                                crBuild.addTarget(surface2)
                                val captureRequest:CaptureRequest = crBuild.build()
                                session.setRepeatingRequest(captureRequest,null,null)
                            }

                            override fun onConfigureFailed(session: CameraCaptureSession) {
                                Log.d("调试","onConfigureFailed")
                            }
                        },null)
                }

                override fun onDisconnected(camera: CameraDevice) {
                    Log.d("调试","onDisconnected")
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.d("调试","onError")
                }
            },null)
        }
        tvInfo.text = videoPlayer.info
    }

    override fun onPause() {
        super.onPause()
        videoPlayer.stopPlay()
    }
}