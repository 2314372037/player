// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("sysconfig");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("sysconfig")
//      }
//    }
#include <fstream>
#include <unistd.h>
#include <string.h>
#include <stdio.h>
#include <jni.h>
#include <android/log.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <malloc.h>
#include <sys/shm.h>
#include <sys/mman.h>
#include "cjson/cJSON.h"
#include <dlfcn.h>
#include <vector>
#include <cstdint>

#define TAG "sysconfig" // 这个是自定义的LOG的标识
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__) // 定义LOGD类型

#define SMFILE_PATH "/data/local/tmp/smfile"//共享内存文件位置
#define SHARED_MEMORY_SIZE 3110400 // 共享内存大小

typedef struct {
    int width;
    int height;
    int size;
    sem_t write_sem;
    sem_t read_sem;
    uint8_t data[SHARED_MEMORY_SIZE];
} SharedMemory;

SharedMemory* shared_memory;
int fd;

void write_video_data(SharedMemory* shm, uint8_t* data, size_t size,int width,int height) {
    if (!shm || !data || size == 0) {
        LOGD("Invalid parameters in write_video_data");
        return;
    }
    //sem_wait(&shm->write_sem);//等待写入信号量
    shared_memory->width = width;
    shared_memory->height = height;
    shared_memory->size = size;
    memcpy(shm->data, data, size);
    //sem_post(&shm->read_sem);//发送可读信号量
}

int create_shared_memory(const char* name, size_t size) {
    int fd = open(name, O_CREAT | O_RDWR, 0777);
    if (fd < 0) {
        perror("open");
        return -1;
    }

    // 设置共享内存对象的大小
    if (ftruncate(fd, SHARED_MEMORY_SIZE) == -1) {
        perror("ftruncate");
        close(fd);
        return -1;
    }
    return fd;
}

void* map_shared_memory(int fd, size_t size) {
    void* addr = mmap(NULL, size, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
    if (addr == MAP_FAILED) {
        perror("mmap");
        return NULL;
    }
    return addr;
}

void* writer_thread(void* arg) {
    int i = 0;
    char buf[10] = {0};
    const char* name = SMFILE_PATH;
    size_t size = SHARED_MEMORY_SIZE;

    fd = create_shared_memory(name, size);
    if (fd < 0) {
        return NULL;
    }

    // 使用chmod修改文件权限
//    if (chmod(name, 0777) != 0) {
//        LOGD("debug=== %s","Error changing file permissions");
//    }

    shared_memory = (SharedMemory *)map_shared_memory(fd, size);
    if (shared_memory == NULL) {
        close(fd);
        return NULL;
    }

    sem_init(&shared_memory->write_sem, 1, 1); // 初始值为1，表示可以写入
    sem_init(&shared_memory->read_sem, 1, 0);  // 初始值为0，表示不能读取
    return NULL;
}

int mainRun() {
    pthread_t thread;
    pthread_create(&thread, NULL, writer_thread, NULL);
    //pthread_join(thread, NULL);//用于等待指定的线程结束
    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_zhanghao_player_PlayerTest2_frameWrite(JNIEnv *env, jobject thiz,jbyteArray byte_array,jint width,jint height,jint length) {
    jbyte *bytes = env->GetByteArrayElements(byte_array, NULL);
    // 直接使用 bytes 缓冲区，避免动态分配
    auto *audio_data = reinterpret_cast<uint8_t*>(bytes);

    // 写入音频数据
    if (shared_memory!= nullptr){
        write_video_data(shared_memory, audio_data, length,width,height);
    }
    // 释放 jbyteArray 的元素指针
    env->ReleaseByteArrayElements(byte_array, bytes, 0);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_zhanghao_player_PlayerTest2_frameInit(JNIEnv *env, jobject object) {
    mainRun();
    return env->NewStringUTF("完成");//要保证有返回值，不然崩溃
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_zhanghao_player_PlayerTest2_frameClose(JNIEnv *env, jobject thiz) {
    sem_destroy(&shared_memory->read_sem);
    sem_destroy(&shared_memory->write_sem);
    munmap(shared_memory, SHARED_MEMORY_SIZE);
    close(fd);
    //remove(SMFILE_PATH);//删除可能导致系统异常
    return 0;
}


extern "C"
JNIEXPORT jstring JNICALL
Java_com_zhanghao_player_Camera2Activity_hello(JNIEnv *env, jobject object) {
//    FILE* file = fopen("/dev/__properties__/u:object_r:exported_default_prop:s0","r");

    __android_log_print(ANDROID_LOG_DEBUG, "jni", "%s","完成");
    return env->NewStringUTF("完成");//要保证有返回值，不然崩溃
}
