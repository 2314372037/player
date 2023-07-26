#include <jni.h>
#include <string>
#include <android/native_window_jni.h>
#include <android/native_window.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <unistd.h>
#include <pthread.h>

extern "C" {
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavfilter/avfilter.h>
#include <libavutil/avutil.h>
#include <libavutil/imgutils.h>
#include <libswscale/swscale.h>
#include <libavutil/time.h>
}

#define TAG "media_frame_get"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)

namespace mediaPlayer {
    char *jstringToChar(JNIEnv *env, jstring jstr) {
        return (char *) env->GetStringUTFChars(jstr, nullptr);
    }

    jstring charToJString(JNIEnv *env, const char *str) {
        return env->NewStringUTF(str);
    }

    bool isRun = true;
    //当前播放时间
    long m_CurTimeStamp = 0;
    //播放的起始时间
    long m_StartTimeStamp = -1;
    JavaVM *g_vm;
    jobject g_obj;
    jobject surface;
    //本地网络路径
    char *path;
    bool isLoop;

    void updateTimeStamp(AVFormatContext *avFormatContext, AVFrame *avFrame, int video_index) {
        //参照 ffplay
        if (avFrame->pkt_dts != AV_NOPTS_VALUE) {
            m_CurTimeStamp = avFrame->pkt_dts;
        } else if (avFrame->pts != AV_NOPTS_VALUE) {
            m_CurTimeStamp = avFrame->pts;
        } else {
            m_CurTimeStamp = 0;
        }
        m_CurTimeStamp = (int64_t) (
                (m_CurTimeStamp * av_q2d(avFormatContext->streams[video_index]->time_base)) * 1000);
    }

    void *runStart(void *arg) {
        JNIEnv *env;
        int mNeedDetach = JNI_FALSE;//线程是否需要分离jvm
        //获取当前native线程是否有没有被附加到jvm环境中
        int envStatus = g_vm->GetEnv((void **) &env, JNI_VERSION_1_6);
        if (envStatus == JNI_EDETACHED) {
            //附加到jvm环境中
            int code = g_vm->AttachCurrentThread(&env, nullptr);
            if (code != 0) {
                LOGD("线程附加失败");
                return nullptr;
            }
            mNeedDetach = JNI_TRUE;
        }


        LOGD("开始执行");
        AVFormatContext *avFormatContext = avformat_alloc_context();
        int a1 = avformat_open_input(&avFormatContext, path, nullptr, nullptr);
        if (a1 != 0) {
            LOGD("失败:%d", a1);
            return nullptr;
        }
        avformat_find_stream_info(avFormatContext, nullptr);
        int video_index = -1;
        int audio_index = -1;
        for (int i = 0; i < avFormatContext->nb_streams; ++i) {
            if (avFormatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
                video_index = i;
            } else if (avFormatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
                audio_index = i;
            }
        }
        AVCodecParameters *avCodecParameters = avFormatContext->streams[video_index]->codecpar;
        //获取解码器
        const AVCodec *avCodec = avcodec_find_decoder(avCodecParameters->codec_id);
        AVCodecContext *avCodecContext = avcodec_alloc_context3(avCodec);
        avcodec_parameters_to_context(avCodecContext, avCodecParameters);
        if (avcodec_open2(avCodecContext, avCodec, nullptr) < 0) {
            LOGD("获取解码器失败");
            return nullptr;
        }
        //申请AVPacket
        auto *avPacket = av_packet_alloc();
        //av_init_packet(avPacket);
        //申请AVPacket
        AVFrame *avFrame = av_frame_alloc();//存放yuv数据
        AVFrame *rgb_avFrame = av_frame_alloc();//存放rgb数据

        auto *outBuffer = (uint8_t *) av_malloc(
                av_image_get_buffer_size(AV_PIX_FMT_RGBA, avCodecContext->width,
                                         avCodecContext->height,
                                         1));
        av_image_fill_arrays(rgb_avFrame->data, rgb_avFrame->linesize, outBuffer, AV_PIX_FMT_RGBA,
                             avCodecContext->width,
                             avCodecContext->height, 1);


        jclass VideoPlayerClass = env->GetObjectClass(g_obj);
        if (VideoPlayerClass == nullptr) {
            LOGD("不能找到类");
            g_vm->DetachCurrentThread();
            return nullptr;
        }

        jmethodID javaCallBack = env->GetMethodID(VideoPlayerClass, "frameDataCallBack", "([BII)V");
        if (javaCallBack == nullptr) {
            LOGD("未找到回调函数");
            g_vm->DetachCurrentThread();
            return nullptr;
        }

        ANativeWindow *aNativeWindow = ANativeWindow_fromSurface(env, surface);
        if (aNativeWindow == nullptr) {
            LOGD("无法获取NativeWindow");
            return nullptr;
        }

        //AVRational avRational = avFormatContext->streams[video_index]->avg_frame_rate;
        //int frame_rate = avRational.num / avRational.den;

        SwsContext *sws_context = sws_getContext(
                avCodecContext->width,
                avCodecContext->height,
                avCodecContext->pix_fmt,
                avCodecContext->width,
                avCodecContext->height,
                AV_PIX_FMT_RGBA,
                SWS_POINT,
                nullptr,
                nullptr,
                nullptr);

        ANativeWindow_Buffer aNativeWindowBuffer;
        ANativeWindow_setBuffersGeometry(aNativeWindow, avCodecContext->width,
                                         avCodecContext->height,
                                         WINDOW_FORMAT_RGBA_8888);

        m_StartTimeStamp = (av_gettime() / 1000) - m_CurTimeStamp;
        isRun = true;
        while (av_read_frame(avFormatContext, avPacket) >= 0 && isRun) {
            if (avPacket->stream_index == video_index) {
                if (avcodec_send_packet(avCodecContext, avPacket) >= 0) {
                    if (avcodec_receive_frame(avCodecContext, avFrame) >= 0) {
                        updateTimeStamp(avFormatContext, avFrame, video_index);

                        jsize data_size = sizeof(avFrame->data);
                        jsize linesize_size = sizeof(avFrame->linesize);
                        jbyteArray dataByteArray = env->NewByteArray(data_size);
                        jintArray lineSizeIntArray = env->NewIntArray(linesize_size);
                        env->SetByteArrayRegion(dataByteArray, 0, data_size,
                                                (jbyte *) avFrame->data);
                        env->SetIntArrayRegion(lineSizeIntArray, 0, linesize_size,
                                               (jint *) avFrame->linesize);
                        env->CallVoidMethod(g_obj, javaCallBack, dataByteArray, avFrame->width,
                                            avFrame->height);
                        env->DeleteLocalRef(dataByteArray);
                        env->DeleteLocalRef(lineSizeIntArray);

                        //下面是处理rgb数据相关
                        ANativeWindow_lock(aNativeWindow, &aNativeWindowBuffer, nullptr);
                        sws_scale(sws_context, (const uint8_t *const *) avFrame->data,
                                  avFrame->linesize, 0,
                                  avFrame->height, rgb_avFrame->data, rgb_avFrame->linesize);
                        auto *dst = (uint8_t *) aNativeWindowBuffer.bits;
                        //拿到一行有多少个字节 RGBA
                        int destStride = aNativeWindowBuffer.stride * 4;
                        //像素数据的首地址
                        uint8_t *data = rgb_avFrame->data[0];
                        //实际内存一行数量
                        int srcStride = rgb_avFrame->linesize[0];
                        for (int i = 0; i < avCodecContext->height; ++i) {
                            //将rgb_frame中每一行的数据复制给nativewindow
                            memcpy(dst + i * destStride, data + i * srcStride, srcStride);
                        }
                        ANativeWindow_unlockAndPost(aNativeWindow);

                        //做延时处理
                        long currentSystemTime = (av_gettime() / 1000);
                        long timeDiff = currentSystemTime - m_StartTimeStamp;
                        if (m_CurTimeStamp > timeDiff) {
                            auto sleepTime = static_cast<unsigned int>(m_CurTimeStamp -
                                                                       timeDiff);//ms
                            av_usleep(sleepTime * 1000);
                        }
                    }
                }
            }
            av_packet_unref(avPacket);
        }
        if (mNeedDetach) {
            g_vm->DetachCurrentThread();
        }
        av_frame_free(&avFrame);
        av_frame_free(&rgb_avFrame);
        avcodec_close(avCodecContext);
        avcodec_free_context(&avCodecContext);
        avformat_free_context(avFormatContext);
        isRun = false;
        m_CurTimeStamp = 0;
        m_StartTimeStamp = -1;
        LOGD("结束执行");
        return nullptr;
    }

    extern "C" JNIEXPORT void JNICALL
    Java_com_zh_ffmpegsdk_MediaPlayer20_nativeStart(JNIEnv *env, jobject obj, jstring source,
                                                    jobject surface1) {
        pthread_t pthread_ptr;
        //以下是其他线程里回调示例，jni不允许其他线程里直接findClass调回调方法
        //JavaVM是虚拟机在jni里的表示
        env->GetJavaVM(&g_vm);
        g_obj = env->NewGlobalRef(obj);
        surface = env->NewGlobalRef(surface1);
        path = jstringToChar(env, source);
        //测试了，不能用多线程参数传值，需要用全局变量
        pthread_create(&pthread_ptr, nullptr, runStart, nullptr);
        //usleep(1000);//坑爹，用多线程参数传值(结构体),不延迟一段时间，线程内获取参数会为空值
    }

    extern "C" JNIEXPORT void JNICALL
    Java_com_zh_ffmpegsdk_MediaPlayer20_nativeSetLoop(JNIEnv *env, jobject, jboolean isLoop1) {
        isLoop = isLoop1;
    }

    extern "C" JNIEXPORT void JNICALL
    Java_com_zh_ffmpegsdk_MediaPlayer20_nativeReset(JNIEnv *env, jobject) {
        isRun = false;
        isLoop = false;
        m_CurTimeStamp = 0;
        m_StartTimeStamp = -1;
    }

    extern "C" JNIEXPORT jstring JNICALL
    Java_com_zh_ffmpegsdk_MediaPlayer20_nativeGetInfo(JNIEnv *env, jobject) {
        const char *info = avcodec_configuration();
        return charToJString(env, info);
    }
}
