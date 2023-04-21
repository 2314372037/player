package com.zhanghao.player

import android.content.Context
import android.net.Uri
import java.io.File

/***
 * Uri转换到文件(content://)
 * file://直接通过File(URI(uri.toString()))
 */
fun Uri.getFile(context: Context, suffix: String = "tmp"): File {
    val contentResolver = context.contentResolver//获取内容解析器
    val inputStream = contentResolver.openInputStream(this)//打开输入流
    val file = File(context.externalCacheDir, "temp_file.${suffix}")//创建临时文件
    var byteArray = ByteArray(inputStream?.available() ?: 0)//初始化inputStream大小的byte数组
    inputStream?.read(byteArray)//读取流 并输出到byte数组
    val outputStream = file.outputStream()//打开file输出流
    outputStream.write(byteArray)//输出byte到file
    //销毁操作
    outputStream.flush()
    outputStream.close()
    inputStream?.close()
    byteArray = ByteArray(0)
    return file
}
