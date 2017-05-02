package com.yeetor.adb;

import com.android.ddmlib.*;
import com.android.ddmlib.TimeoutException;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

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

    private ListenableFuture<List<AdbForward>> executeGetForwardList() {
        final File adbFile = new File(AdbServer.server().adbPath);
        final SettableFuture future = SettableFuture.create();
        (new Thread(new Runnable() {
            public void run() {
                ProcessBuilder pb = new ProcessBuilder(new String[]{adbFile.getPath(), "forward", "--list"});
                pb.redirectErrorStream(true);
                Process p = null;

                try {
                    p = pb.start();
                } catch (IOException e) {
                    future.setException(e);
                    return;
                }

                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

                try {
                    String line;
                    try {
                        List<AdbForward> list = new ArrayList<AdbForward>();
                        while((line = br.readLine()) != null) {
                            //64b2b4d9 tcp:555 localabstract:shit
                            AdbForward forward = new AdbForward(line);
                            if (forward.isForward()) {
                                list.add(forward);
                            }
                        }
                        future.set(list);
                        return;
                    } catch (IOException ex) {
                        future.setException(ex);
                        return;
                    }
                } finally {
                    try {
                        br.close();
                    } catch (IOException ex) {
                        future.setException(ex);
                    }

                }
            }
        }, "Obtaining adb version")).start();
        return future;
    }

    public AdbForward[] getForwardList() {
        ListenableFuture<List<AdbForward>> future = executeGetForwardList();
        try {
            List<AdbForward> s = future.get(1, TimeUnit.SECONDS);
            AdbForward[] ret = new AdbForward[s.size()];
            s.toArray(ret);
            return ret;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new AdbForward[0];
    }

//    public static String[] getForwardList(IDevice device) {
//        CollectingOutputReceiver receiver = new CollectingOutputReceiver();
//
//        try {
//            device.executeShellCommand("forward --list", receiver, 0);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        String output = receiver.getOutput();
//        System.out.println(output);
//        return null;
//    }

}
