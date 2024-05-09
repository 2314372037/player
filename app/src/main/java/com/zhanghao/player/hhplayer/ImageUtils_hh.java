package com.zhanghao.player.hhplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class ImageUtils_hh {
    /**
     * I420转nv21
     */
    public static byte[] I420Tonv21(byte[] data, int width, int height) {
        byte[] ret = new byte[data.length];
        int total = width * height;
        ByteBuffer bufferY = ByteBuffer.wrap(ret, 0, total);
        ByteBuffer bufferVU = ByteBuffer.wrap(ret, total, total / 2);
        bufferY.put(data, 0, total);
        for (int i = 0; i < total / 4; i += 1) {
            bufferVU.put(data[i + total + total / 4]);
            bufferVU.put(data[total + i]);
        }
        return ret;
    }

    //nv21转bitmap
    public static Bitmap nv21ToBitmap(byte[] nv21, int width, int height){
        YuvImage image = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
        if (!image.compressToJpeg(new Rect(0, 0, width, height), 80, jpegOutputStream)) {
            return null;
        }
        byte[] tmp = jpegOutputStream.toByteArray();
        jpegOutputStream.reset();
        return BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
    }

    /**
     * NV21裁剪 by lake 算法效率 11ms
     *
     * @param src    源数据
     * @param width  源宽
     * @param height 源高
     * @param left   顶点坐标
     * @param top    顶点坐标
     * @param clip_w 裁剪后的宽
     * @param clip_h 裁剪后的高
     * @return 裁剪后的数据
     */
    public static byte[] cropNV21(byte[] src, int width, int height, int left, int top, int clip_w, int clip_h) {
        if (left > width || top > height) {
            return null;
        }
        //取偶
        int x = left / 2 * 2 , y = top / 2 * 2 ;
        int w = clip_w / 2 * 2 , h = clip_h / 2 * 2 ;
        int y_unit = w * h;
        int src_unit = width * height;
        int uv = y_unit >> 1;
        byte[] nData = new byte[y_unit + uv];
        for (int i = y, len_i = y + h; i < len_i; i++) {
            for (int j = x, len_j = x + w; j < len_j; j++) {
                nData[(i - y) * w + j - x] = src[i * width + j];
                nData[y_unit + ((i - y) / 2) * w + j - x] = src[src_unit + i / 2 * width + j];
            }
        }
        return nData;
    }

    public static byte[] generateSolidColorNV21(int width, int height, int yValue, int uValue, int vValue) {
        // 确保宽度和高度是偶数，因为UV分量是隔行交错存储的
        width = width % 2 == 0 ? width : width - 1;
        height = height % 2 == 0 ? height : height - 1;

        // Y、U、V 数据的总大小
        int yuvSize = width * height + (width / 2) * (height / 2) * 2;
        byte[] nv21Data = new byte[yuvSize];

        // 设置 Y 分量
        for (int i = 0; i < width * height; i++) {
            nv21Data[i] = (byte) yValue;
        }

        // 设置 U 和 V 分量
        for (int i = 0; i < (width / 2) * (height / 2); i++) {
            nv21Data[width * height + i * 2] = (byte) uValue;
            nv21Data[width * height + i * 2 + 1] = (byte) vValue;
        }

        return nv21Data;
    }

    public static byte[] scaleNV21(byte[] inputNv21, int inputWidth, int inputHeight, int outputWidth, int outputHeight) {
        // 计算输出 NV21 数组的长度
        int newLength = outputWidth * outputHeight + (outputWidth / 2) * (outputHeight / 2) * 2;
        byte[] outputNv21 = new byte[newLength];

        // Y通道缩放
        for (int i = 0; i < outputHeight; i++) {
            for (int j = 0; j < outputWidth; j++) {
                int inputYIndex = (i * inputHeight / outputHeight) * inputWidth + (j * inputWidth / outputWidth);
                // 确保索引在输入数组的界限内
                if (inputYIndex < inputWidth * inputHeight) {
                    outputNv21[i * outputWidth + j] = inputNv21[inputYIndex];
                }
            }
        }

        // U/V通道缩放
        for (int i = 0; i < outputHeight / 2; i++) {
            for (int j = 0; j < outputWidth / 2; j++) {
                int inputUIndex = inputWidth * inputHeight + (i * (inputWidth / 2) / (outputWidth / 2)) * (inputHeight / 2) + (j * 2);
                int inputVIndex = inputUIndex + 1;
                // 确保索引在输入数组的界限内
                if (inputUIndex < inputWidth * inputHeight && inputVIndex < inputWidth * inputHeight + 1) {
                    outputNv21[newLength - 1 - i * (outputWidth / 2) - j * 2] = inputNv21[inputVIndex];
                    outputNv21[newLength - 1 - i * (outputWidth / 2) - j * 2 - 1] = inputNv21[inputUIndex];
                }
            }
        }

        return outputNv21;
    }

    public static byte[] scaleNV21_2(byte[] nv21Data, int width, int height, int scaleWidth, int scaleHeight) {
        // Create a new byte buffer for the scaled NV21 data
        byte[] scaledNV21Data = new byte[scaleWidth * scaleHeight * 3 / 2];

        // Calculate the scaling factors
        float scaleX = (float) width / scaleWidth;
        float scaleY = (float) height / scaleHeight;

        for (int dstY = 0; dstY < scaleHeight; dstY++) {
            for (int dstX = 0; dstX < scaleWidth; dstX++) {
                // Calculate the corresponding source image position
                int srcX = (int) (dstX * scaleX);
                int srcY = (int) (dstY * scaleY);

                // Interpolate the pixel values
                byte y = nv21Data[srcX + srcY * width];
                int uvIndex = width * height + srcY / 2 * (width / 2) + srcX / 2 * 2;
                byte u = nv21Data[uvIndex];
                byte v = nv21Data[uvIndex + 1];

                // Write the interpolated pixel values to the scaled NV21 data buffer
                scaledNV21Data[dstX + dstY * scaleWidth] = y;

                // 处理 U/V 值，注意交错存储和正确的索引计算
                if ((srcY % 2 == 0) && (srcX % 2 == 0)) {
                    int uvIndex1 = (dstY / 2) * (scaleWidth / 2) + (dstX / 2) * 2;
                    scaledNV21Data[scaleWidth * scaleHeight + uvIndex1] = u;
                    scaledNV21Data[scaleWidth * scaleHeight + uvIndex1 + 1] = v;
                }
            }
        }

        return scaledNV21Data;
    }

    public static byte[] scaleNV21_3(byte[] nv21Data, int width, int height, int scaleWidth, int scaleHeight) {
        // 创建目标图像数据缓冲区
        byte[] scaledNv21Data = new byte[scaleWidth * scaleHeight + (scaleWidth / 2) * (scaleHeight / 2) * 2];

        // 计算每个像素缩放的比例因子
        float scaleX = (float) width / scaleWidth;
        float scaleY = (float) height / scaleHeight;

        for (int i = 0; i < scaleHeight; i++) {
            for (int j = 0; j < scaleWidth; j++) {
                // 计算原始图像中对应的 Y 值索引
                int srcX = (int) (j * scaleX);
                int srcY = (int) (i * scaleY);
                int yIndex = srcY * width + srcX;
                if (yIndex < width * height) {
                    // 写入缩放后的 Y 值
                    scaledNv21Data[i * scaleWidth + j] = nv21Data[yIndex];
                }
            }
        }

        // 处理 U/V 色度分量
        int outputUvIndex = scaleWidth * scaleHeight;
        for (int i = 0; i < scaleHeight / 2; i++) {
            for (int j = 0; j < scaleWidth / 2; j++) {
                // 计算原始图像中对应的 U/V 值索引
                int srcX = j * 2;
                int srcY = i * 2;
                int uvIndex = srcY * (width / 2) + srcX / 2;

                // 写入缩放后的 U/V 值
                scaledNv21Data[outputUvIndex++] = nv21Data[width * height + uvIndex]; // U 值
                scaledNv21Data[outputUvIndex++] = nv21Data[width * height + 1 + uvIndex]; // V 值
            }
        }

        return scaledNv21Data;
    }

    public static byte[] scaleNV21_5(byte[] nv21Data, int width, int height, int scaleWidth, int scaleHeight) {
        byte[] scaledNv21Data = new byte[scaleWidth * scaleHeight + (scaleWidth / 2) * (scaleHeight / 2) * 2];

        float scaleX = (float) width / scaleWidth;
        float scaleY = (float) height / scaleHeight;

        for (int i = 0; i < scaleHeight; i++) {
            for (int j = 0; j < scaleWidth; j++) {
                int srcX = Math.round(j * scaleX);
                int srcY = Math.round(i * scaleY);

                // 确保 srcX 和 srcY 在原始图像的尺寸范围内
                if (srcX < width && srcY < height) {
                    int yIndex = srcY * width + srcX;
                    scaledNv21Data[i * scaleWidth + j] = nv21Data[yIndex];
                }
            }
        }

        int outputUvIndex = scaleWidth * scaleHeight;
        for (int i = 0; i < scaleHeight / 2; i++) {
            for (int j = 0; j < scaleWidth / 2; j++) {
                int srcX = j * 2;
                int srcY = i * 2;

                // 确保 srcX 和 srcY 在原始图像的尺寸范围内
                if (srcX < width && srcY < height) {
                    int uvIndex = (srcY / 2) * (width / 2) + (srcX / 2);
                    scaledNv21Data[outputUvIndex++] = nv21Data[width * height + uvIndex]; // U 值
                    scaledNv21Data[outputUvIndex++] = nv21Data[width * height + 1 + uvIndex]; // V 值
                }
            }
        }

        return scaledNv21Data;
    }
}
