package com.yeetor;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.yeetor.adb.AdbServer;
import com.yeetor.minicap.Banner;
import com.yeetor.minicap.Minicap;
import com.yeetor.minicap.MinicapInstallException;
import com.yeetor.minicap.MinicapListener;
import com.yeetor.p2p.WSServer;
import io.netty.bootstrap.ServerBootstrap;

import java.io.*;

/**
 * Created by harry on 2017/4/15.
 */
public class Main {
    static String sn = "f76ce69";

    public static void main(String[] args) {

        // 启动服务器
        try {
            new WSServer(6655).start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
