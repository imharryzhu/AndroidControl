package com.yeetor;

import com.yeetor.adb.AdbForward;
import com.yeetor.adb.AdbServer;
import com.yeetor.p2p.WSServer;

/**
 * Created by harry on 2017/4/15.
 */
public class Main {
    public static void main(String[] args) {

        boolean isServer = true;

        if (isServer) {
            // 启动服务器
            try {
                new WSServer(6655).start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
