package com.yeetor;

import com.android.ddmlib.IDevice;
import com.yeetor.adb.AdbServer;
import com.yeetor.minicap.Minicap;
import com.yeetor.minicap.MinicapInstallException;

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


        try {
            Minicap.installMinicap(device);
        } catch (MinicapInstallException e) {
            e.printStackTrace();
        }



    }
}
