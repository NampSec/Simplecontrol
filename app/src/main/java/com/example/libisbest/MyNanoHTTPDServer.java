package com.example.libisbest;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import fi.iki.elonen.NanoHTTPD;

public class MyNanoHTTPDServer extends NanoHTTPD {

    private static final String TAG = "MyNanoHTTPDServer";
    private final File rootDir;
    private final Context context;
    private final  Handler handler;


    public MyNanoHTTPDServer(int port, File rootDir,Context context, Handler handler) throws IOException {
        super(port);
        this.rootDir = rootDir;
        this.context = context;
        this.handler = handler;
        start(SOCKET_READ_TIMEOUT, false);
        Log.i(TAG, "Server started on port " + port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        if ("/".equals(uri)) {
            uri = "/index.html";
        }
        if ("/refresh".equals(uri))
        {
            handler.sendEmptyMessage(0);
            return newFixedLengthResponse(Response.Status.OK, "text/plain", "200 OK");

        }
        if ("/update".equals(uri))
        {
            String query = session.getQueryParameterString();
            if (query != null && !query.isEmpty()) {
                // 解析查询字符串
                String[] params = query.split("&");
                int x = 0;
                int y = 0;
                for (String param : params) {
                    String[] pair = param.split("=");
                    if (pair.length == 2) {
                        String key = pair[0];
                        String value = pair[1];
                        try {
                            // 尝试将值转换为整数
                            if ("x".equals(key)) {
                                x = Integer.parseInt(value);
                            } else if ("y".equals(key)) {
                                y = Integer.parseInt(value);
                            }
                        } catch (NumberFormatException e) {
                            // 处理数字格式异常
                            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/plain", "Invalid number format");
                        }
                    }
                }


                // 创建一个Message对象
                Message message = Message.obtain();
                message.what = 1; // 消息的what属性，可以根据需要自定义
                message.arg1 = x; // 消息的arg1属性，可以携带一个整数参数
                message.arg2 = y; // 消息的arg2属性，可以携带另一个整数参数

                // 发送消息
                handler.sendMessage(message);

                // 返回响应
                return newFixedLengthResponse(Response.Status.OK, "text/plain", "200 OK");
            }
        }
        if ("/back".equals(uri)) {
            handler.sendEmptyMessage(2);
            // 返回响应
            return newFixedLengthResponse(Response.Status.OK, "text/plain", "200 OK");
        }
        if("/scrollup".equals(uri))
        {
            handler.sendEmptyMessage(3);
            // 返回响应
            return newFixedLengthResponse(Response.Status.OK, "text/plain", "200 OK");
        }
        if("/scrolldown".equals(uri))
        {
            handler.sendEmptyMessage(4);
            // 返回响应
            return newFixedLengthResponse(Response.Status.OK, "text/plain", "200 OK");
        }






        if ("/image_screen.png".equals(uri)) {
            // 将 Bitmap 压缩为 PNG 格式并传输给客户端
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MainActivity mainActivity = (MainActivity) context;
            Bitmap mBitmap = mainActivity.mScreenCapture.mBitmap;
            if (mBitmap != null) {
                try {
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 80, out);
                    out.flush();
                    out.close();
                }catch (IOException e) {
                    Log.e(TAG, e.toString());
                    e.printStackTrace();
                }
                byte[] imageBytes = out.toByteArray();
                return newFixedLengthResponse(Response.Status.OK, "image/png", new ByteArrayInputStream(imageBytes), imageBytes.length);
            } else {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "No screenshot available");
            }
        }


        File file = new File(rootDir, uri);
        if (!file.exists() || file.isDirectory()) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 Not Found");
        }

        try {
            String mimeType = getMimeType(uri);
            InputStream inputStream = new FileInputStream(file);
            return newChunkedResponse(Response.Status.OK, mimeType, inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "500 Internal Server Error");
        }
    }

    private String getMimeType(String uri) {
        if (uri.endsWith(".html")) {
            return "text/html";
        } else if (uri.endsWith(".css")) {
            return "text/css";
        } else if (uri.endsWith(".js")) {
            return "application/javascript";
        } else if (uri.endsWith(".jpg") || uri.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (uri.endsWith(".png")) {
            return "image/png";
        } else if (uri.endsWith(".gif")) {
            return "image/gif";
        } else {
            return "application/octet-stream";
        }
    }
}
