#include <jni.h>
#include <string>
#include <android/native_window_jni.h>
#include <android/native_window.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <unistd.h>

extern "C" {
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavfilter/avfilter.h>
#include <libavutil/avutil.h>
#include <libavutil/imgutils.h>
#include <libswscale/swscale.h>
#include <libavutil/time.h>
}

#define TAG "VideoPlayer"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)

char *jstringToChar(JNIEnv *env, jstring jstr) {
    return (char *) env->GetStringUTFChars(jstr, nullptr);
}

jstring charToJString(JNIEnv *env,const char *str) {
    return env->NewStringUTF(str);
}

bool isStop = true;
int playRate = 1;
//当前播放时间
long m_CurTimeStamp = 0;
//播放的起始时间
long m_StartTimeStamp = -1;

void updateTimeStamp(AVFormatContext *avFormatContext,AVFrame *avFrame,int video_index) {
    //参照 ffplay
    if(avFrame->pkt_dts != AV_NOPTS_VALUE) {
        m_CurTimeStamp = avFrame->pkt_dts;
    } else if (avFrame->pts != AV_NOPTS_VALUE) {
        m_CurTimeStamp = avFrame->pts;
    } else {
        m_CurTimeStamp = 0;
    }
    m_CurTimeStamp = (int64_t)((m_CurTimeStamp * av_q2d(avFormatContext->streams[video_index]->time_base)) * 1000);
}

extern "C" JNIEXPORT void JNICALL
Java_com_zhanghao_player_VideoPlayer_start(JNIEnv *env, jobject, jstring source, jobject surface) {
    LOGD("开始执行");
    AVFormatContext *avFormatContext = avformat_alloc_context();
    const char *src = jstringToChar(env, source);
    int a1 = avformat_open_input(&avFormatContext, src, nullptr, nullptr);
    if (a1!=0){
        LOGD("失败%d",a1);
        return;
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
        return;
    }
    //申请AVPacket
    auto *avPacket = av_packet_alloc();
//    av_init_packet(avPacket);
    //申请AVPacket
    AVFrame *avFrame = av_frame_alloc();//存放yuv数据
    AVFrame *rgb_avFrame = av_frame_alloc();//存放rgb数据

    auto *outBuffer = (uint8_t *) av_malloc(
            av_image_get_buffer_size(AV_PIX_FMT_RGBA, avCodecContext->width, avCodecContext->height,
                                     1));
    av_image_fill_arrays(rgb_avFrame->data, rgb_avFrame->linesize, outBuffer, AV_PIX_FMT_RGBA,
                         avCodecContext->width,
                         avCodecContext->height, 1);

    ANativeWindow *aNativeWindow = ANativeWindow_fromSurface(env, surface);
    if (aNativeWindow == nullptr) {
        LOGD("无法获取NativeWindow");
        return;
    }

    AVRational avRational = avFormatContext->streams[video_index]->avg_frame_rate;
    int frame_rate = avRational.num / avRational.den;

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
    ANativeWindow_setBuffersGeometry(aNativeWindow, avCodecContext->width, avCodecContext->height,
                                     WINDOW_FORMAT_RGBA_8888);

    isStop = false;
    m_StartTimeStamp = (av_gettime()/1000) - m_CurTimeStamp;
    while (av_read_frame(avFormatContext, avPacket) >= 0 && !isStop) {
        if (avPacket->stream_index == video_index) {
            if (avcodec_send_packet(avCodecContext, avPacket) >= 0) {
                if (avcodec_receive_frame(avCodecContext, avFrame) >= 0) {
                    updateTimeStamp(avFormatContext,avFrame,video_index);

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

                    long currentSystemTime = (av_gettime()/1000);
                    long timeDiff = currentSystemTime - m_StartTimeStamp;
                    if (m_CurTimeStamp>timeDiff){
                        auto sleepTime = static_cast<unsigned int>(m_CurTimeStamp - timeDiff);//ms
                        av_usleep(sleepTime * 1000);
                    }
                }
            }
        }
        av_packet_unref(avPacket);
    }
    ANativeWindow_release(aNativeWindow);
    av_frame_free(&avFrame);
    av_frame_free(&rgb_avFrame);
    avcodec_close(avCodecContext);
    avcodec_free_context(&avCodecContext);
    avformat_free_context(avFormatContext);
    LOGD("结束执行");
}

extern "C" JNIEXPORT void JNICALL
Java_com_zhanghao_player_VideoPlayer_stop(JNIEnv *env, jobject) {
    isStop = true;
    m_CurTimeStamp = 0;
    m_StartTimeStamp = -1;
}

void setVideoPlayRate(jfloat play_rate) {
    playRate = play_rate;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_zhanghao_player_VideoPlayer_getVersion(JNIEnv *env, jobject thiz) {
    const char *info = avcodec_configuration();
    return charToJString(env,info);
}

