//  "com.google.android.material:material:1.4.0"
package com.zhanghao.player

import android.content.Context
import android.graphics.Bitmap
import java.io.FileOutputStream
import android.media.AudioManager
import android.util.Log
import android.widget.ImageView
// import com.google.android.material.slider.Slider
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.io.File
import android.view.TextureView
import android.graphics.SurfaceTexture
import android.view.Surface

// 引用 uts 基础库相关类
// import io.dcloud.uts.UTSAndroid
// import io.dcloud.uts.setInterval
// import io.dcloud.uts.clearInterval

enum class ijkPlayerLoadState {
    UNKNOWN,
    PLAYTHROUGH_OK,
    STALLED,
    PLAYABLE
}

enum class ijkPlayerPlayBackDidFinishState {
    UNKNOWN,
    PLAYBACK_ENDED,
    PLAYBACK_ERROR,
    USER_EXITED
}

enum class ijkPlayerScalingMode {
    ASPECT_FIT,
    ASPECT_FILL,
    FILL
}

class ijkPlayerManager(private val context: Context, private val textureView: TextureView, private val url: String, private val scalingMode: String): LifecycleObserver {
    private var currentVideoSize: IntSize = IntSize(0, 0)
    private var fullScreenCallback: ((Boolean) -> Unit)? = null
    private var videoSizeCallback: ((IntSize) -> Unit)? = null
    private var loadStateCallback: ((ijkPlayerLoadState) -> Unit)? = null
    private var playBackDidFinishCallback: ((ijkPlayerPlayBackDidFinishState) -> Unit)? = null
    private var errorReconnect: Boolean = true
    private var reconnectBackImageView: ImageView? = null
    private var skipLiveHead: Boolean = false
    private var pauseInBackground: Boolean = false
    private var recreatePlayer: Boolean = false
    private var scalingModeEnum: ijkPlayerScalingMode = ijkPlayerScalingMode.ASPECT_FIT
    // private var volumeSlider: Slider? = null
	private var audioManager: AudioManager? = null
	private var mediaPlayer: IjkMediaPlayer? = null
	private var surface: Surface? = null
    init {
		IjkMediaPlayer.loadLibrariesOnce(null)
        scalingModeEnum = getPlayerScalingModeWithString(scalingMode)
        createReconnectBackImageView()
		setupVolumeControl()
		textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
		    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
		    }
			override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
			}
			override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
			}
			override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {  
			    surface?.release()  // 释放 Surface  
			    return true         // 由系统回收 SurfaceTexture  
			}
		}
		// Surface 就绪时初始化播放器并绑定 Surface
		createPlayer()
    }

    private fun setupVolumeControl() {
  //       audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
  //       volumeSlider = Slider(context)
  //       volumeSlider?.addOnChangeListener { _, value, _ ->
		//     if (audioManager != null) {
		// 		val maxVolume = audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
		// 		// 直接转换并限制范围
		// 		val targetVolume: Int = (value * maxVolume).toInt().coerceIn(0, maxVolume)
		// 		audioManager?.setStreamVolume(
		// 		    AudioManager.STREAM_MUSIC,
		// 		    targetVolume,
		// 		    AudioManager.FLAG_SHOW_UI // 显示系统音量UI
		// 		)
		// 	}
  //       }
  //       val maxVolume = audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()  
  //       val currentVolume = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()  
  //       volumeSlider?.value = currentVolume / maxVolume  
		// handleRouteChange(audioManager!!)
	}

    private fun createReconnectBackImageView() {
  //       reconnectBackImageView = ImageView(context)
  //       reconnectBackImageView?.visibility = View.GONE
		// val parent = textureView.parent as ViewGroup
		// parent.addView(reconnectBackImageView)
    }

    private fun createPlayer() {
		// textureView.setVisibility(View.GONE)
		// textureView.setVisibility(View.VISIBLE)
		if (surface == null) {
			val surfaceTexture = textureView.getSurfaceTexture()
			if (surfaceTexture != null) {
			    surface = Surface(surfaceTexture)
			} else {
				return
			    // 处理未初始化的情况（如延迟重试或提示错误）
			}
		}
		if (mediaPlayer == null){
			mediaPlayer = IjkMediaPlayer()
		} else {
			mediaPlayer?.release();
			mediaPlayer = IjkMediaPlayer()
		}
		mediaPlayer?.setSurface(surface!!)
		mediaPlayer?.setDataSource(url)
		// mediaPlayer?.setVolume(0f,0f)
		// mediaPlayer?.setLooping(true)
		mediaPlayer?.setLogEnabled(true)
		mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 1024 * 10)
		mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 100)
		mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 0)
		mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_frame", 0) // 不跳过任何帧‌
        mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1)
		mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0)
		mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "vol", 256)
		mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-fps", 30)
		mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1)
        mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 5 * 1000)
		mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1)
        mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_cached_duration", 300)
		mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "live_reduce_delay", 1)
		mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "live_reduce_delay_dropframe", 1)
		mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "lr_sample_correction_percent", 3)
        mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "hr_sample_correction_percent", 1)
		mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "fps", 30)
		mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 1024 * 50)
		mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "nobuffer")
        mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "tart-on-prepared", 1)
	    mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flags", "low_delay")
		mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtmp_buffer", "1000")
		mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "videotoolbox", 0)
		mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync", "vfr")
        mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "avsync", 1)
        mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync_threshold", 100)

        mediaPlayer?.prepareAsync()
		mediaPlayer?.setOnPreparedListener {mp -> 
		   //  mp.start() // 准备完成后自动播放‌
		   Log.d("ijkPlayerManager", "准备播放")
		}
        setVideoScalingMode(getPlayerScalingMode(scalingModeEnum))
        mediaPlayer?.setOnCompletionListener {
            playBackDidFinishCallback?.invoke(ijkPlayerPlayBackDidFinishState.PLAYBACK_ENDED)
        }
        mediaPlayer?.setOnErrorListener { _, what, extra ->
            if (errorReconnect && url.isNotEmpty()) {
                restartPlayer()
            } else {
                playBackDidFinishCallback?.invoke(ijkPlayerPlayBackDidFinishState.PLAYBACK_ERROR)
            }
            true
        }
		// mediaPlayer?.setOnBufferingUpdateListener(object : IMediaPlayer.OnBufferingUpdateListener {
		//     override fun onBufferingUpdate(mp: IMediaPlayer?, percent: Int) {
		//         // 处理缓冲进度逻辑‌
		//     }
		// })
		// 获取渲染开始、缓冲开始等事件
        mediaPlayer?.setOnInfoListener { _, what, extra ->
            when (what) {
                IjkMediaPlayer.MEDIA_INFO_BUFFERING_START -> loadStateCallback?.invoke(
                    ijkPlayerLoadState.STALLED
                )
                IjkMediaPlayer.MEDIA_INFO_BUFFERING_END -> loadStateCallback?.invoke(
                    ijkPlayerLoadState.PLAYABLE
                )
                else -> loadStateCallback?.invoke(ijkPlayerLoadState.UNKNOWN)
            }
            false
        }
		// 处理seek操作完成事件
        mediaPlayer?.setOnSeekCompleteListener {
            loadStateCallback?.invoke(ijkPlayerLoadState.PLAYABLE)
        }
		mediaPlayer?.setOnVideoSizeChangedListener { mp, width, height, sarNum, sarDen ->
		    if (currentVideoSize.width != width || currentVideoSize.height != height) {
		        currentVideoSize = IntSize(width, height)
		        videoSizeCallback?.invoke(currentVideoSize)
		    }
		}
    }
	
	fun setMuted(isMuted: Boolean) {
		if (isMuted) {
			mediaPlayer?.setVolume(0f, 0f)
		} else {
			mediaPlayer?.setVolume(1f, 1f)
		}
	}
	
	fun setVideoScalingMode(scalingMode: String) {
	    when (scalingMode) {
			// 按比例填充
			"aspectFit" -> mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "videoview", 1)
			// 拉伸填充
			"aspectFill" -> mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "videoview", 2)
			// 全屏拉伸
			"fill" -> mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "videoview", 3)
			else -> mediaPlayer?.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "videoview", 1)
		}
	}

    private fun getPlayerScalingModeWithString(scalingMode: String): ijkPlayerScalingMode {
        return when (scalingMode) {
            "aspectFit" -> ijkPlayerScalingMode.ASPECT_FIT
            "aspectFill" -> ijkPlayerScalingMode.ASPECT_FILL
            "fill" -> ijkPlayerScalingMode.FILL
            else -> ijkPlayerScalingMode.ASPECT_FIT
        }
    }

    private fun getPlayerScalingMode(scalingMode: ijkPlayerScalingMode): String {
        return when (scalingMode) {
            ijkPlayerScalingMode.ASPECT_FIT -> "aspectFit"
            ijkPlayerScalingMode.ASPECT_FILL -> "aspectFill"
            ijkPlayerScalingMode.FILL -> "fill"
        }
    }

    @OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_START)
    fun onStart() {
        // mediaPlayer?.start()
    }

    @OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_STOP)
    fun onStop() {
        // mediaPlayer?.stop()
    }

    @OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        // mediaPlayer?.release()
    }

    fun setupFullScreenCallback(callback: ((Boolean) -> Unit)?) {
        fullScreenCallback = callback
    }

    fun setupVideoSizeCallback(callback: ((IntSize) -> Unit)?) {
        videoSizeCallback = callback
    }

    fun setupLoadStateCallback(callback: ((ijkPlayerLoadState) -> Unit)?) {
        loadStateCallback = callback
    }

    fun setupPlayBackDidFinishCallback(callback: ((ijkPlayerPlayBackDidFinishState) -> Unit)?) {
        playBackDidFinishCallback = callback
    }

    fun play() {
        mediaPlayer?.start()
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun stop() {
        mediaPlayer?.stop()
    }

    fun setVolume(volume: Float) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (volume * maxVolume).toInt(), AudioManager.FLAG_SHOW_UI)
    }

    fun getVolume(): Float {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
    }

    fun captureVideoImageToFile(completion: (String?, Boolean, String?) -> Unit) {
        val bitmap = textureView.getBitmap()
        if (bitmap != null) {
            saveImageToFile(bitmap, completion)
        } else {
            completion(null, false, "截屏失败")
        }
    }

    private fun saveImageToFile(bitmap: Bitmap, completion: (String?, Boolean, String?) -> Unit) {
        val directory = File(context.getExternalFilesDir(null), "snapshot")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, "${System.currentTimeMillis()}.jpg")
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
            fos.flush()
            fos.close()
            completion(file.absolutePath, true, null)
        } catch (e: Exception) {
            completion(null, false, e.localizedMessage)
        }
    }

    fun restartPlayer() {
		mediaPlayer?.setSurface(null)
        mediaPlayer?.stop()
        mediaPlayer?.release()
        recreatePlayer = true
        // reconnectBackImageView?.visibility = View.VISIBLE
        createPlayer()
    }
	
	fun destoryPlayer() { // TODO
		mediaPlayer?.setSurface(null)
		mediaPlayer?.stop()
		mediaPlayer?.release()
		surface?.release() // 释放 Surface
		videoSizeCallback = null
		fullScreenCallback = null
		// 使用安全调用操作符 ?.let 替代强制解包
		audioManager?.let { 
		    releaseAudioFocus(it)  // 当 audioManager 非空时自动传入非空对象
		}
		audioManager = null
	}
    private fun handleRouteChange(audioManager: AudioManager) {
        // val result = audioManager.requestAudioFocus(
        //     focusListener,
        //     AudioManager.STREAM_MUSIC,  
        //     AudioManager.AUDIOFOCUS_GAIN  
        // )
        // if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
        //     mediaPlayer?.start()
        // }
    }
    // private val focusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
    //     when (focusChange) {
    //         AudioManager.AUDIOFOCUS_LOSS -> mediaPlayer?.pause()
    //         AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> mediaPlayer?.pause()
    //         AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
    //             // 降低音量至50%‌:ml-citation{ref="2,3" data="citationList"}
    //             mediaPlayer?.setVolume(0.5f)
    //         }
    //         AudioManager.AUDIOFOCUS_GAIN -> {
    //             mediaPlayer?.setVolume(1.0f)
    //             mediaPlayer?.start()
    //         }
    //     }
    // }
    private fun releaseAudioFocus(audioManager: AudioManager) {
       // audioManager.abandonAudioFocus(focusListener)
    }
    fun playFullScreen(fullScreen: Boolean) {
        fullScreenCallback?.invoke(fullScreen)
    }
}

data class IntSize(val width: Int, val height: Int)
