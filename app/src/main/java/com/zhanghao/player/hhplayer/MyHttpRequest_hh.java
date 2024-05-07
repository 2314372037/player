package com.zhanghao.player.hhplayer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/**
 * Http请求封装类，封装常用的Get，Post请求方法
 * @author 郑立兵
 *
 */
public class MyHttpRequest_hh {
    /**
     * 向指定URL发送GET方法的请求
     *
     * @param url
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            // connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            // for (String key : map.keySet()) {
            // System.out.println(key + "--->" + map.get(key));
            // }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }

            byte[] bresult = result.getBytes();
            result = new String(bresult, "utf-8");
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 向指定URL发送GET方法的请求
     *
     * @param url
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @param encoding
     *            设置响应信息的编码格式，如utf-8
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param, String encoding) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            // for (String key : map.keySet()) {
            // System.out.println(key + "--->" + map.get(key));
            // }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }

            byte[] bresult = result.getBytes();
            result = new String(bresult, encoding);
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url
     *            发送请求的 URL
     * @param jsonData
     *            请求参数，请求参数应该是Json格式字符串的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String jsonData) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection con = realUrl.openConnection();
            HttpURLConnection conn = (HttpURLConnection) con;
            // 设置通用的请求属性
            conn.setRequestMethod("POST"); // 设置Post请求
            conn.setConnectTimeout(5 * 1000);
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded"); // 设置内容类型
            // conn.setRequestProperty("Content-Length",
            // String.valueOf(param.length())); //设置长度
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(new OutputStreamWriter(
                    conn.getOutputStream(), "utf-8"));
            // 发送请求参数
            // out.print(param);
            out.write(jsonData);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            byte[] bresult = result.getBytes();
            result = new String(bresult, "utf-8");
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    public static String sendPost(String url, String jsonData, String encoding, String authorization, String postmanToken)
    {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection con = realUrl.openConnection();
            HttpURLConnection conn = (HttpURLConnection) con;
            // 设置通用的请求属性
            conn.setRequestMethod("POST"); // 设置Post请求
            conn.setConnectTimeout(5 * 1000);
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded"); // 设置内容类型
            conn.setRequestProperty("authorization", authorization);
            conn.setRequestProperty("postman-token", postmanToken);

            // conn.setRequestProperty("Content-Length",
            // String.valueOf(param.length())); //设置长度
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(new OutputStreamWriter(
                    conn.getOutputStream(), encoding));
            // 发送请求参数
            // out.print(param);
            out.write(jsonData);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            byte[] bresult = result.getBytes();
            result = new String(bresult, encoding);
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url
     *            发送请求的 URL
     * @param jsonData
     *            请求参数，请求参数应该是Json格式字符串的形式。
     * @param encoding
     *            设置响应信息的编码格式，如utf-8
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String jsonData, String encoding) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection con = realUrl.openConnection();
            HttpURLConnection conn = (HttpURLConnection) con;
            // 设置通用的请求属性
            conn.setRequestMethod("POST"); // 设置Post请求
            conn.setConnectTimeout(5 * 1000);
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded"); // 设置内容类型
            // conn.setRequestProperty("Content-Length",
            // String.valueOf(param.length())); //设置长度
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(new OutputStreamWriter(
                    conn.getOutputStream(), encoding));
            // 发送请求参数
            // out.print(param);
            out.write(jsonData);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            byte[] bresult = result.getBytes();
            result = new String(bresult, encoding);
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }



    /**
     * 上传媒体文件
     * @param url
     * 				上传媒体文件的微信公众平台接口地址
     * @param file
     * 				要上传的媒体文件对象
     * @return
     * 				返回上传的响应结果，详情参看微信公众平台开发者接口文档
     */
    public static String uploadPost(String url, File file) {
        DataOutputStream dos = null;
        FileInputStream fis = null;
        DataInputStream dis = null;
        BufferedReader in = null;
        String result = "";
        String end = "\r\n";
        String twoHyphens = "--"; // 用于拼接
        String boundary = "*****"; // 用于拼接 可自定义
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection con = realUrl.openConnection();
            HttpURLConnection conn = (HttpURLConnection) con;
            // 设置通用的请求属性
            conn.setRequestMethod("POST"); // 设置Post请求
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setConnectTimeout(5 * 1000);
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + boundary); // 设置内容类型

            // 获取URLConnection对象对应的输出流
            dos = new DataOutputStream(conn.getOutputStream());
            //1、写入媒体头部分
            StringBuilder sb = new StringBuilder();
            sb.append(twoHyphens).append(boundary).append(end);
            sb.append("Content-Disposition: form-data;name=\"file\";filename=\"" + file.getName() + "\"").append(end);
            sb.append("Content-Type:application/octet-stream").append(end).append(end);
            byte[] head = sb.toString().getBytes("utf-8");
            dos.write(head);

            //2、写入媒体正文部分， 对文件进行传输
            fis = new FileInputStream(file);
            dis = new DataInputStream(fis);
            byte[] buffer = new byte[8192]; // 8K
            int count = 0;
            while ((count = dis.read(buffer)) != -1) {
                dos.write(buffer, 0, count);
            }

            //3、写入媒体结尾部分。
            byte[] foot = (end + twoHyphens + boundary + twoHyphens + end).getBytes("utf-8");
            dos.write(foot);
            dos.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            byte[] bresult = result.getBytes();
            result = new String(bresult, "utf-8");
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输出流、输入流
        finally {
            try {
                if (dos != null) {
                    dos.close();
                }
                if (dis != null) {
                    dis.close();
                }
                if (fis != null) {
                    fis.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }
}
