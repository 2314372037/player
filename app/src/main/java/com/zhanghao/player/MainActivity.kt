package com.zhanghao.player

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.*
import android.hardware.Camera
import android.media.MediaPlayer
import android.os.*
import android.telephony.*
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.yancy.yuvutils.ImageUtils
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.*


class MainActivity : AppCompatActivity() {
    lateinit var tvInfo: TextView
    lateinit var edit_url: EditText
    lateinit var button_play: Button
    lateinit var button_stop: Button
    lateinit var button_camera: Button
    lateinit var textureView: TextureView
    lateinit var mediaPlayer:MediaPlayer
    lateinit var ijkMediaPlayer: IjkMediaPlayer
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

//        edit_url.setText("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")
        //edit_url.setText("http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8")
//        edit_url.setText("rtmp://ns8.indexforce.com/home/mystream")
        //edit_url.setText("rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mp4")
        edit_url.setText("/sdcard/b.mp4")
        button_play.setOnClickListener {
            ijkMediaPlayer = IjkMediaPlayer()
            ijkMediaPlayer.isLooping = true
//            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
//            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);//自动旋转方向
//            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
//            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_YV12.toLong());
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
            val runLooper = Looper.myLooper()
            ijkMediaPlayer.setOnVideoFrameUpdateListener { iMediaPlayer, width, height, bytes ->
                val nv21 = ImageUtils.rgb565ToNV21(bytes,width,height,180)//degree设置180后(旋转后)可解决图像颜色失真
//                val nv21_1 = ImageUtils.nv21Scale(nv21!!,width,height,height,width)
                val nv21_2 = ImageUtils.nv21Rotate(nv21!!,width,height,180)//degree设置180后(旋转后)可解决图像颜色失真


                val image = YuvImage(nv21_2, ImageFormat.NV21, width, height, null)
                val jpegOutputStream = ByteArrayOutputStream(nv21_2!!.size)
                if (!image.compressToJpeg(Rect(0, 0, width, height), 80, jpegOutputStream)) {
                    return@setOnVideoFrameUpdateListener
                }
                val tmp = jpegOutputStream.toByteArray()

                //val bitmap = ImageUtils.rgb565ToBitmap565(bytes,width,height)
                val bitmap = BitmapFactory.decodeByteArray(tmp, 0, tmp.size)

                if (handler == null) {
                    handler = Handler(runLooper!!)
                }
                handler?.post { findViewById<ImageView>(R.id.ivPreview).setImageBitmap(bitmap) }
            }
            ijkMediaPlayer.setSurface(Surface(textureView.surfaceTexture))
            ijkMediaPlayer.setDataSource(edit_url.text.toString())
            ijkMediaPlayer.prepareAsync()

//            JavaTest.test(edit_url.text.toString(),findViewById<ImageView>(R.id.ivPreview))
        }
        button_stop.setOnClickListener {
            JavaTest.stop()
        }
        button_camera.setOnClickListener {
            camera1()
        }
    }

    private fun camera1(){
        val camera = Camera.open()
        camera.setPreviewTexture(textureView.surfaceTexture)
        camera.setPreviewCallback(object : Camera.PreviewCallback{
            override fun onPreviewFrame(data: ByteArray, camera: Camera?) {
                val size: Camera.Size = camera?.getParameters()!!.getPreviewSize() //获取预览大小
                val width: Int = size.width //宽度
                val height: Int = size.height
                val image = YuvImage(data, ImageFormat.NV21, width, height, null)
                val os = ByteArrayOutputStream(data.size)
                if (!image.compressToJpeg(Rect(0, 0, width, height), 100, os)) {
                    return
                }
                val tmp: ByteArray = os.toByteArray()
                val bitmap = BitmapFactory.decodeByteArray(tmp, 0, tmp.size)
                runOnUiThread {
                    findViewById<ImageView>(R.id.ivPreview).setImageBitmap(bitmap)
                }
            }
        })
        camera.startPreview()
    }
}