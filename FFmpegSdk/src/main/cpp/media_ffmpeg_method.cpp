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

#define TAG "debug==media_ffmpeg_method"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__)

namespace mediaFFmpegMethod {
    char *jstringToChar(JNIEnv *env, jstring jstr) {
        return (char *) env->GetStringUTFChars(jstr, nullptr);
    }

    jstring charToJString(JNIEnv *env, const char *str) {
        return env->NewStringUTF(str);
    }

    int checkExc(JNIEnv *env) {
        if(env->ExceptionCheck()) {
            env->ExceptionDescribe(); // writes to logcat
            env->ExceptionClear();
            return 1;
        }
        return 0;
    }

    //本地网络路径
    char *path;

    int video_index = -1;
    int audio_index = -1;
    AVFormatContext *avFormatContext;
    AVCodecParameters *avCodecParameters;
    AVCodecContext *avCodecContext;
    AVPacket *avPacket;
    AVFrame *avFrame;
    AVFrame *rgb_avFrame;
    SwsContext *swsContext;
    AVFrame *rgbFrame;
    uint8_t *rgbBuffer;

    jbyteArray dataByteArray;

    extern "C" JNIEXPORT jint JNICALL
    Java_com_zh_ffmpegsdk_MediaFFmpegMethod_init(JNIEnv *env, jobject thiz,jstring source) {
        avFormatContext = avformat_alloc_context();
        path = jstringToChar(env, source);
        int a1 = avformat_open_input(&avFormatContext, path, nullptr, nullptr);
        if (a1 != 0) {
            LOGE("失败:%d", a1);
            return -1;
        }
        avformat_find_stream_info(avFormatContext, nullptr);
        for (int i = 0; i < avFormatContext->nb_streams; ++i) {
            if (avFormatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
                video_index = i;
            } else if (avFormatContext->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
                audio_index = i;
            }
        }
        avCodecParameters = avFormatContext->streams[video_index]->codecpar;
        //获取解码器
        const AVCodec *avCodec = avcodec_find_decoder(avCodecParameters->codec_id);
        avCodecContext = avcodec_alloc_context3(avCodec);
        avcodec_parameters_to_context(avCodecContext, avCodecParameters);
        if (avcodec_open2(avCodecContext, avCodec, nullptr) < 0) {
            LOGE("获取解码器失败");
            return -1;
        }
        //申请AVPacket
        avPacket = av_packet_alloc();
        //申请AVPacket
        avFrame = av_frame_alloc();//存放yuv数据
        rgb_avFrame = av_frame_alloc();//存放rgb数据

        // 创建图像转换上下文
        swsContext = sws_getContext(
                avCodecContext->width, avCodecContext->height, AV_PIX_FMT_YUV420P,
                avCodecContext->width, avCodecContext->height, AV_PIX_FMT_RGBA,
                0, nullptr, nullptr, nullptr
        );

        // 分配目标RGB帧
        rgbFrame = av_frame_alloc();
        int rgbBufferSize = av_image_get_buffer_size(AV_PIX_FMT_RGBA, avCodecContext->width,avCodecContext->height, 1);
        rgbBuffer = static_cast<uint8_t *>(av_malloc(rgbBufferSize));
        if (checkExc(env)==1){
            LOGE("native init异常");
        }
        return 0;
    }


    extern "C" JNIEXPORT jint JNICALL
    Java_com_zh_ffmpegsdk_MediaFFmpegMethod_avFrameWidth(JNIEnv *env, jobject thiz) {
        jint i = (jint)avFrame->width;
        return i;
    }

    extern "C" JNIEXPORT jint JNICALL
    Java_com_zh_ffmpegsdk_MediaFFmpegMethod_avFrameHeight(JNIEnv *env, jobject thiz) {
        jint i = (jint)avFrame->height;
        return i;
    }

    extern "C" JNIEXPORT jlong JNICALL
    Java_com_zh_ffmpegsdk_MediaFFmpegMethod_avGetTime(JNIEnv *env, jobject thiz) {
        auto l = (jlong)av_gettime();
        return l;
    }

    extern "C" JNIEXPORT jint JNICALL
    Java_com_zh_ffmpegsdk_MediaFFmpegMethod_avReadFrame(JNIEnv *env, jobject thiz) {
        jint i = (jint)av_read_frame(avFormatContext, avPacket);
        return i;
    }

    extern "C" JNIEXPORT jboolean JNICALL
    Java_com_zh_ffmpegsdk_MediaFFmpegMethod_packetStreamIndexIsVideo(JNIEnv *env, jobject thiz) {
        jboolean b = (jboolean)avPacket->stream_index == video_index;
        return b;
    }

    extern "C" JNIEXPORT jint JNICALL
    Java_com_zh_ffmpegsdk_MediaFFmpegMethod_avcodecSendPacket(JNIEnv *env, jobject thiz) {
        jint i = (jint)avcodec_send_packet(avCodecContext, avPacket);
        return i;
    }

    extern "C" JNIEXPORT jint JNICALL
    Java_com_zh_ffmpegsdk_MediaFFmpegMethod_avcodecReceiveFrame(JNIEnv *env, jobject thiz) {
        jint i = (jint)avcodec_receive_frame(avCodecContext, avFrame);
        return i;
    }

    extern "C" JNIEXPORT void JNICALL
    Java_com_zh_ffmpegsdk_MediaFFmpegMethod_avUsleep(JNIEnv *env, jobject thiz,jlong sleepTime) {
        av_usleep((unsigned int)sleepTime);
    }

    extern "C" JNIEXPORT jlong JNICALL
    Java_com_zh_ffmpegsdk_MediaFFmpegMethod_getTimeStamp(JNIEnv *env, jobject thiz) {
        //当前播放时间
        long m_CurTimeStamp = 0;
        //参照 ffplay
        if (avFrame->pkt_dts != AV_NOPTS_VALUE) {
            m_CurTimeStamp = avFrame->pkt_dts;
        } else if (avFrame->pts != AV_NOPTS_VALUE) {
            m_CurTimeStamp = avFrame->pts;
        } else {
            m_CurTimeStamp = 0;
        }
        m_CurTimeStamp = (int64_t) ((m_CurTimeStamp * av_q2d(avFormatContext->streams[video_index]->time_base)) * 1000);
        if (checkExc(env)==1){
            LOGE("native getTimeStamp异常");
        }
        return (jlong)m_CurTimeStamp;
    }

    extern "C" JNIEXPORT jbyteArray JNICALL
    Java_com_zh_ffmpegsdk_MediaFFmpegMethod_getFrameRGB565ByteArray(JNIEnv *env, jobject thiz) {
        // 分配目标RGB帧
        av_image_fill_arrays(rgbFrame->data, rgbFrame->linesize, rgbBuffer, AV_PIX_FMT_RGBA,avFrame->width, avFrame->height, 1);
        if (checkExc(env)==1){
            LOGE("native getFrameRGB565ByteArray-av_image_fill_arrays异常");
        }

        // 进行颜色空间转换
        sws_scale(swsContext, avFrame->data, avFrame->linesize, 0, avFrame->height, rgbFrame->data,rgbFrame->linesize);
        if (checkExc(env)==1){
            LOGE("native getFrameRGB565ByteArray-sws_scale异常");
        }

        // 在此处可以访问转换后的RGB帧数据 rgbFrame->data
        jsize linesize = rgbFrame->linesize[0];
        jsize data_size = linesize * avFrame->height;
        dataByteArray = env->NewByteArray(data_size);
        if (checkExc(env)==1){
            LOGE("native getFrameRGB565ByteArray-NewByteArray异常");
        }
        env->SetByteArrayRegion(dataByteArray, 0, data_size, (jbyte *) rgbFrame->data[0]);
        if (checkExc(env)==1){
            LOGE("native getFrameRGB565ByteArray-SetByteArrayRegion异常");
        }
        return dataByteArray;
    }

    extern "C" JNIEXPORT void JNICALL
    Java_com_zh_ffmpegsdk_MediaFFmpegMethod_destroy(JNIEnv *env, jobject thiz) {
        //env->DeleteGlobalRef(dataByteArray);
        av_frame_free(&rgbFrame);
        av_free(rgbBuffer);
        sws_freeContext(swsContext);
        av_frame_free(&avFrame);
        av_frame_free(&rgb_avFrame);
        avcodec_close(avCodecContext);
        avcodec_free_context(&avCodecContext);
        avformat_free_context(avFormatContext);
    }

    extern "C" JNIEXPORT jstring JNICALL
    Java_com_zh_ffmpegsdk_MediaFFmpegMethod_getInfo(JNIEnv *env, jobject thiz) {
        const char *info = avcodec_configuration();
        jstring jinfo = env->NewStringUTF(info);
        return jinfo;
    }
}
