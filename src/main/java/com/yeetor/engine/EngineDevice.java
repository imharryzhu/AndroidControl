/*
 * MIT License
 *
 * Copyright (c) 2017 朱辉
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.yeetor.engine;

import com.android.ddmlib.IDevice;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.yeetor.adb.AdbDevice;
import com.yeetor.adb.AdbServer;
import com.yeetor.minitouch.Minitouch;
import com.yeetor.minitouch.MinitouchListener;

import java.util.concurrent.ExecutionException;

/**
 * Created by harry on 2017/5/11.
 */
public class EngineDevice {

    private AdbDevice device;
    private Minitouch minitouch;
    private boolean minitouchOpen = false;

    public static EngineDevice getDevice(String serialNumber) {
        AdbDevice iDevice = AdbServer.server().getDevice(serialNumber);
        if (iDevice != null) {
            EngineDevice device = new EngineDevice(iDevice);
            if (device.isMinitouchOpen()) {
                return device;
            }
        }
        return null;
    }

    public EngineDevice(AdbDevice iDevice) {
        this.device = iDevice;
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
        return AdbServer.executeShellCommand(device.getIDevice(), command);
    }

    public void startApp(String str) {
        AdbServer.executeShellCommand(device.getIDevice(), "am start " + str);
    }

}
