package com.zhanghao.player

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.zhanghao.Camera2Activity
import com.zhanghao.player.hhplayer.ImageUtils_hh


class MainActivity : AppCompatActivity() {
    lateinit var tvInfo: TextView
    lateinit var edit_url: EditText
    lateinit var button_play: Button
    lateinit var button_stop: Button
    lateinit var button_camera: Button
    lateinit var button_camera2: Button
    lateinit var button_camera3: Button
    lateinit var textureView: TextureView
    lateinit var surfaceView: SurfaceView
    lateinit var ivPreview: ImageView
    private val playerTest by lazy { PlayerTest() }
    private val playerTest2 by lazy { PlayerTest2() }
    private lateinit var camera:Camera

    fun getPseudoID(): String {
        val MODULUS = 10
        val sb = StringBuilder()
        sb.append(Build.BOARD.length % MODULUS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sb.append(Build.SUPPORTED_ABIS.contentDeepToString().length % MODULUS)
        } else {
            // noinspection deprecation
            sb.append(Build.CPU_ABI.length % MODULUS)
        }
        sb.append(Build.DEVICE.length % MODULUS)
        sb.append(Build.DISPLAY.length % MODULUS)
        sb.append(Build.HOST.length % MODULUS)
        sb.append(Build.ID.length % MODULUS)
        sb.append(Build.MANUFACTURER.length % MODULUS)
        sb.append(Build.BRAND.length % MODULUS)
        sb.append(Build.MODEL.length % MODULUS)
        sb.append(Build.PRODUCT.length % MODULUS)
        sb.append(Build.BOOTLOADER.length % MODULUS)
        sb.append(Build.HARDWARE.length % MODULUS)
        sb.append(Build.TAGS.length % MODULUS)
        sb.append(Build.TYPE.length % MODULUS)
        sb.append(Build.USER.length % MODULUS)
        return sb.toString()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val tm = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        Log.d("调试","id:${Settings.Secure.getString(contentResolver,Settings.Secure.ANDROID_ID)}")
        Log.d("调试","id:${Settings.System.getString(contentResolver,Settings.System.ANDROID_ID)}")
        getPseudoID()



        val permission = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        ActivityCompat.requestPermissions(this, permission, 1)

        edit_url = findViewById<EditText>(R.id.edit_url)
        button_play = findViewById<Button>(R.id.button_play)
        button_stop = findViewById<Button>(R.id.button_stop)
        button_camera = findViewById<Button>(R.id.button_camera)
        button_camera2 = findViewById<Button>(R.id.button_camera2)
        button_camera3 = findViewById<Button>(R.id.button_camera3)
        textureView = findViewById<TextureView>(R.id.textureView)
        surfaceView = findViewById<SurfaceView>(R.id.surfaceView)
        ivPreview = findViewById<ImageView>(R.id.ivPreview)
        textureView.layoutParams?.let {
            it.width = 800
            it.height = 600
            textureView.layoutParams = it
        }
        surfaceView.layoutParams?.let {
            it.width = 800
            it.height = 600
            surfaceView.layoutParams = it
        }
        ivPreview.layoutParams?.let {
            it.width = 800
            it.height = 600
            ivPreview.layoutParams = it
        }

//        edit_url.setText("http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8")
//        edit_url.setText("rtmp://ns8.indexforce.com/home/mystream")
//        edit_url.setText("rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mp4")
        edit_url.setText("rtmp://192.168.10.34/live/livestream")
        button_play.setOnClickListener {
//            if (true){
//                mediaPlayer = MediaPlayer()
//                mediaPlayer.setSurface(Surface(textureView.surfaceTexture))
//                mediaPlayer.setDataSource(edit_url.text.toString())
//                mediaPlayer.prepare()
//                mediaPlayer.start()
//                return@setOnClickListener
//            }
//            playerTest.ori_holder = surfaceView.holder
//            playerTest.mSurfacetexture = textureView.surfaceTexture
//            playerTest.start(edit_url.text.toString(),this,textureView.width,textureView.height)
            playerTest2.context = this.applicationContext
            playerTest2.ori_holder = surfaceView.holder
            playerTest2.textureView = textureView
            playerTest2.start(edit_url.text.toString())
        }
        button_stop.setOnClickListener {
//            playerTest.stop()
            playerTest2.stop()
        }
        button_camera.setOnClickListener {
            camera1()
        }
        button_camera2.setOnClickListener {
            camera2()
        }
        button_camera3.setOnClickListener {
            val intent = Intent(this, Camera2Activity::class.java)
            startActivity(intent)
        }
    }

    private fun camera1(){
        Log.d("调试","走camera1()方法")
        camera = Camera.open()
        camera.setPreviewTexture(textureView.surfaceTexture)
        camera.parameters?.let {
            it.setPreviewSize(640,480)
            camera.parameters = it
        }
        val par = camera.getParameters()
        val size: Camera.Size = par!!.getPreviewSize() //获取预览大小
        val width: Int = size.width //宽度
        val height: Int = size.height
        camera.setPreviewCallback(object : Camera.PreviewCallback{
            override fun onPreviewFrame(data: ByteArray, camera: Camera?) {
                val bitmap2 = ImageUtils_hh.nv21ToBitmap(data,width, height)
                runOnUiThread {
                    ivPreview.setImageBitmap(bitmap2)
                }
            }
        })
        camera.startPreview()
    }

    private fun camera2(){
        Log.d("调试","走camera2()方法")
        camera = Camera.open()
        camera.setPreviewDisplay(surfaceView.holder)
        camera.parameters?.let {
            it.setPreviewSize(640,480)
            camera.parameters = it
        }
        val par = camera.getParameters()
        val size: Camera.Size = par!!.getPreviewSize() //获取预览大小
        val width: Int = size.width //宽度
        val height: Int = size.height
        val aaa = ByteArray(width*height*3/2)
        camera.addCallbackBuffer(aaa)
        camera.setPreviewCallbackWithBuffer(object : Camera.PreviewCallback{
            override fun onPreviewFrame(data: ByteArray, camera: Camera?) {
                val bitmap2 = ImageUtils_hh.nv21ToBitmap(data,width, height)
                runOnUiThread {
                    ivPreview.setImageBitmap(bitmap2)
                }
                camera?.addCallbackBuffer(aaa)
            }
        })
        camera.startPreview()
    }
}