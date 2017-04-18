package com.yeetor;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.yeetor.adb.AdbServer;
import com.yeetor.minicap.Banner;
import com.yeetor.minicap.Minicap;
import com.yeetor.minicap.MinicapInstallException;
import com.yeetor.minicap.MinicapListener;

import java.io.*;

/**
 * Created by harry on 2017/4/15.
 */
public class Main {
    static String sn = "f76ce69";

    public static void main(String[] args) {
        AdbServer server = new AdbServer();

        IDevice[] devices = server.getDevices();
        IDevice device = null;
        for (IDevice d : devices) {
            if (sn.equals(d.getSerialNumber())) {
                device = d;
                break;
            }
        }
        if (device == null && devices.length > 0) {
            device = devices[0];
        }

        try {
            Minicap.installMinicap(device);
        } catch (MinicapInstallException e) {
            e.printStackTrace();
        }


        Minicap cap = new Minicap(device);

        MinicapListener listener = new MinicapListener() {
            public void onStartup(Minicap minicap, boolean success) {
                System.out.println("start up");
            }
            // banner信息读取完毕
            public void onBanner(Minicap minicap, Banner banner) {
                System.out.println(banner);
            }
            // 读取到图片信息
            public void onJPG(Minicap minicap, byte[] data) {
                System.out.println("data:" + data.length);
                File f = new File("screen.jpg");
                try {
                    FileOutputStream fileOutputStream = new FileOutputStream(f);
                    fileOutputStream.write(data);
                    fileOutputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        cap.addEventListener(listener);

        cap.start(1.0f, 0);
    }
}
