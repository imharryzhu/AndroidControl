package com.yeetor.engine;

import com.android.ddmlib.IDevice;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.yeetor.adb.AdbServer;
import com.yeetor.minitouch.Minitouch;
import com.yeetor.minitouch.MinitouchListener;

import java.util.concurrent.ExecutionException;

/**
 * Created by harry on 2017/5/11.
 */
public class EngineDevice {

    private IDevice iDevice;
    private Minitouch minitouch;
    private boolean minitouchOpen = false;

    public static EngineDevice getDevice(String serialNumber) {
        IDevice iDevice = AdbServer.server().getDevice(serialNumber);
        if (iDevice != null) {
            EngineDevice device = new EngineDevice(iDevice);
            if (device.isMinitouchOpen()) {
                return device;
            }
        }
        return null;
    }

    public EngineDevice(IDevice iDevice) {
        this.iDevice = iDevice;
        minitouch = new Minitouch(iDevice);
        SettableFuture future = SettableFuture.create();

        minitouch.addEventListener(new MinitouchListener() {
            @Override
            public void onStartup(Minitouch minitouch, boolean success) {
                future.set(success);
            }

            @Override
            public void onClose(Minitouch minitouch) {
                minitouchOpen = false;
            }
        });
        minitouch.start();

        // 等待，指导Minitouch启动完毕
        try {
            Boolean success = (Boolean)future.get();
            if (success) {
                minitouchOpen = true;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    protected boolean isMinitouchOpen() {
        return minitouchOpen;
    }

    public void touchDown(int x, int y) {
        minitouch.sendEvent("d 0 " + x + " " + y + " 50\nc\n");
    }

    public void touchMove(int x, int y) {
        minitouch.sendEvent("m 0 " + x + " " + y + " 50\nc\n");
    }

    public void touchUp() {
        minitouch.sendEvent("u 0\nc\n");

    }

    public String executeShellAndGetString(String command) {
        return AdbServer.executeShellCommand(iDevice, command);
    }

    public void startApp(String str) {
        AdbServer.executeShellCommand(iDevice, "am start " + str);
    }

}
