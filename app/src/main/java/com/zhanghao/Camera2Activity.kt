package com.zhanghao

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.SessionConfiguration
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.zhanghao.player.R


class Camera2Activity : AppCompatActivity() {
    private val textureView by lazy { findViewById<TextureView>(R.id.textureView) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findViewById<View>(R.id.button).setOnClickListener {
            camera2()
        }
        findViewById<View>(R.id.button2).setOnClickListener { }
    }

    private fun camera2(){
        // 创建一个JPEG格式的图像读取器
        val mImageReader = ImageReader.newInstance(
            1280,
            720,
            ImageFormat.YUV_420_888,
            10
        )

        // 设置图像读取器的图像可用监听器，一旦捕捉到图像数据就会触发监听器的onImageAvailable方法
        mImageReader.setOnImageAvailableListener(ImageReader.OnImageAvailableListener { reader ->
            val image = reader.acquireNextImage()
            if (image!=null){
                Log.d("调试", "刷新")
                image.close()
            }
        }, Handler())

        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val surface2 = Surface(textureView.surfaceTexture)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        cameraManager.openCamera(cameraManager.cameraIdList[0],object : CameraDevice.StateCallback(){
            override fun onOpened(camera: CameraDevice) {
                val previewRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                previewRequestBuilder.addTarget(surface2)
                previewRequestBuilder.addTarget(mImageReader.surface)
                camera.createCaptureSession(listOf(surface2,mImageReader.surface),object : CameraCaptureSession.StateCallback(){
                    override fun onConfigured(session: CameraCaptureSession) {
                        Log.d("调试","onConfigured")
                        session.setRepeatingRequest(previewRequestBuilder.build(),null,null)
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
}