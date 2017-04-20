package com.yeetor.adb;

import com.android.ddmlib.*;
import com.sun.xml.internal.bind.v2.model.core.ID;
import jdk.internal.org.objectweb.asm.commons.AdviceAdapter;

import java.io.File;
import java.io.IOException;

/**
 * Created by harry on 2017/4/15.
 */
public class AdbServer {
    private static AdbServer server;
    private String adbPath = null;
    private String adbPlatformTools = "platform-tools";

    AndroidDebugBridge adb = null;
    private boolean success = false;

    public static AdbServer server() {
        if (server == null) {
            server = new AdbServer();
        }
        return server;
    }

    private AdbServer() {
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

    public IDevice getDevice(String serialNumber) {
        IDevice[] devices = AdbServer.server().getDevices();
        IDevice device = null;
        for (IDevice d : devices) {
            if (serialNumber.equals(d.getSerialNumber())) {
                device = d;
                break;
            }
        }
        return device;
    }

    public IDevice getFirstDevice() {
        IDevice[] devices = getDevices();
        if (devices.length > 0) {
            return devices[0];
        }
        return null;
    }

    public static String executeShellCommand(IDevice device, String command) {
        CollectingOutputReceiver output = new CollectingOutputReceiver();

        try {
            device.executeShellCommand(command, output, 0);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (AdbCommandRejectedException e) {
            e.printStackTrace();
        } catch (ShellCommandUnresponsiveException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output.getOutput();
    }

}
