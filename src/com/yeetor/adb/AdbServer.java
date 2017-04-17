package com.yeetor.adb;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;

import java.io.File;

/**
 * Created by harry on 2017/4/15.
 */
public class AdbServer {

    private String adbPath = null;
    private String adbPlatformTools = "platform-tools";

    AndroidDebugBridge adb = null;
    private boolean success = false;

    public AdbServer() {
        init();
    }

    private String getADBPath(){
        if (adbPath == null){
            adbPath = System.getenv("ANDROID_SDK_ROOT");
            if(adbPath != null){
                adbPath += File.separator + adbPlatformTools;
            }else {
                return null;
            }
        }
        adbPath += File.separator + "adb";
        return adbPath;
    }

    private void init() {
        AndroidDebugBridge.init(false);
        adb = AndroidDebugBridge.createBridge(getADBPath(), true);

        if (adb != null) {
            if (waitForDeviceList()) {
                success = true;
            }
        }
    }

    private boolean waitForDeviceList() {
        int maxWaittingTime = 100;
        int interval = 10;
        while (!adb.hasInitialDeviceList()) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                break;
            }
            maxWaittingTime -= 1;
            if (maxWaittingTime == 0) {
                disconnectAdb();
                return false;
            }
        }
        return true;
    }

    void disconnectAdb() {
        if (adb != null) {
            adb = null;
        }
        success = false;
    }

    public IDevice[] getDevices() {
        return adb.getDevices();
    }

}
