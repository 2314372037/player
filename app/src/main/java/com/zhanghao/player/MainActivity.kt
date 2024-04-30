package com.zhanghao.player

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.Camera
import android.media.MediaPlayer
import android.os.*
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.yancy.yuvutils.ImageUtils
import com.yancy.yuvutils.ImageUtils.i420ToBitmap565
import com.zh.hhplayer.HHPlayer
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {
    lateinit var tvInfo: TextView
    lateinit var edit_url: EditText
    lateinit var button_play: Button
    lateinit var button_stop: Button
    lateinit var button_camera: Button
    lateinit var textureView: TextureView
    lateinit var surfaceView: SurfaceView
    lateinit var mediaPlayer:MediaPlayer
    lateinit var playerTest: PlayerTest
    private var handler: Handler? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val permission = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        ActivityCompat.requestPermissions(this, permission, 1)

        tvInfo = findViewById<TextView>(R.id.tvInfo)
        edit_url = findViewById<EditText>(R.id.edit_url)
        button_play = findViewById<Button>(R.id.button_play)
        button_stop = findViewById<Button>(R.id.button_stop)
        button_camera = findViewById<Button>(R.id.button_camera)
        textureView = findViewById<TextureView>(R.id.textureView)
        surfaceView = findViewById<SurfaceView>(R.id.surfaceView)
        val ivPreview = findViewById<ImageView>(R.id.ivPreview)
//        edit_url.setText("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")
        //edit_url.setText("http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8")
//        edit_url.setText("rtmp://ns8.indexforce.com/home/mystream")
        //edit_url.setText("rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mp4")
        edit_url.setText("webrtc://192.168.10.80/live/livestream")
        button_play.setOnClickListener {
//            if (true){
//                mediaPlayer = MediaPlayer()
//                mediaPlayer.setSurface(Surface(textureView.surfaceTexture))
//                mediaPlayer.setDataSource(edit_url.text.toString())
//                mediaPlayer.prepare()
//                mediaPlayer.start()
//                return@setOnClickListener
//            }
            playerTest = PlayerTest()
            playerTest.imageView = ivPreview
            playerTest.ori_holder = surfaceView.holder
            playerTest.mSurfacetexture = textureView.surfaceTexture
            playerTest.start(edit_url.text.toString(),this,textureView.width,textureView.height)
        }
        button_stop.setOnClickListener {
            playerTest.stop()
        }
        button_camera.setOnClickListener {
            camera1()
        }
    }

    private fun camera1(){
        val ivPreview = findViewById<ImageView>(R.id.ivPreview)
        val camera = Camera.open()
        camera.setPreviewTexture(textureView.surfaceTexture)
        camera.parameters?.let {
            it.setPreviewSize(640,480)
            camera.parameters = it
        }
        camera.setPreviewCallback(object : Camera.PreviewCallback{
            override fun onPreviewFrame(data: ByteArray, camera: Camera?) {
                val size: Camera.Size = camera?.getParameters()!!.getPreviewSize() //获取预览大小
                val width: Int = size.width //宽度
                val height: Int = size.height
                val bitmap2 = ImageUtils.nv21ToBitmap565(data,width,height)
                runOnUiThread {
                    ivPreview.setImageBitmap(bitmap2)
                }
            }
        })
        camera.startPreview()
    }
}