package com.example.libisbest;

import java.io.OutputStream;

public class RootShellCmd {

    private OutputStream os;

    // 构造方法，初始化输出流
    public RootShellCmd() {
        try {
            os = Runtime.getRuntime().exec("su").getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行shell指令
     *
     * @param cmd 指令
     */
    public final void exec(String cmd) {
        try {
            if (os != null) {
                os.write(cmd.getBytes());
                os.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行模拟点击操作
     *
     * @param x 点击位置的 x 坐标
     * @param y 点击位置的 y 坐标
     */
    public void SimuClick(int x, int y) {
        // 构建点击指令
        String clickCmd = "input tap " + x + " " + y + "\n";
        // 执行点击指令
        exec(clickCmd);
    }
}

