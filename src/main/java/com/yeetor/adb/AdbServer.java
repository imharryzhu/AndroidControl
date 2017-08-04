/*
 *
 * MIT License
 *
 * Copyright (c) 2017 朱辉 https://blog.yeetor.com
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
 *
 */

package com.yeetor.adb;

import com.android.ddmlib.*;
import com.android.ddmlib.TimeoutException;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.log4j.Logger;

import javax.usb.*;
import javax.usb.event.UsbServicesEvent;
import javax.usb.event.UsbServicesListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

public class AdbServer {
    private static Logger logger = Logger.getLogger(AdbServer.class);
    private static AdbServer server;
    private String adbPath = null;
    private String adbPlatformTools = "platform-tools";
    
    List<AdbDevice> adbDeviceList = null;
    List<IAdbServerListener> listeners = null;

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
    
    /**
     * 监听USB设备的状态
     */
    public void listenUSB() {
        adbDeviceList = new ArrayList<>();
        
        UsbServices services = null;
        try {
            services = UsbHostManager.getUsbServices();
        } catch (UsbException e) {
            e.printStackTrace(); // TODO: LOG
        }
        services.addUsbServicesListener(new MyUSBListener());
        System.out.println("AdbServer.listenUSB: 已开启USB设备监听...");
    }
    
    /**
     * 监听ADB
     */
    public void listenADB() {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    refreshAdbDeviceList();
                }
            }
        }.start();
    }
     
    /**
     * USB设备连接时调用
     */
    private void onUsbDeviceConnected(UsbDevice usbDevice) {
        logger.info(String.format("USB设备连接：idProduct(0x%x) idVendor(0x%x)", usbDevice.getUsbDeviceDescriptor().idProduct(), usbDevice.getUsbDeviceDescriptor().idVendor()));
        List<AdbDevice> devices = checkAdbDevices(usbDevice);
        devices.forEach(adbDevice -> onAdbDeviceConnected(adbDevice));
    }
    
    /**
     * USB设备断开时调用
     */
    private void onUsbDeviceDisConnected(UsbDevice usbDevice) {
        logger.info(String.format("USB设备断开：idProduct(0x%x) idVendor(0x%x)", usbDevice.getUsbDeviceDescriptor().idProduct(), usbDevice.getUsbDeviceDescriptor().idVendor()));
        List<AdbDevice> devices = checkAdbDevices(usbDevice);
        devices.forEach(adbDevice -> onAdbDeviceDisConnected(adbDevice));
    }
    
    /**
     * 发现安卓设备时调用
     */
    private void onAdbDeviceConnected(AdbDevice adbDevice) {
        /*  
        这部分代码是收到ADB设备连接事件时，将连接的adb设备加入到列表
        但这样会与listenADB线程冲突，所以选择了后面的那个方案。仅仅将这个事件当做一个"立即刷新"的通知
        这样带来的缺点就是，无法知道哪些设备是usb连接的
        // 初始化IDevice
        // TODO: 这一步，adb可能没能获取到连接的设备
        IDevice[] iDevice = adb.getDevices();
        for (IDevice device : iDevice) {
            if (device.getSerialNumber().equals(adbDevice.getSerialNumber())) {
                adbDevice.setIDevice(device);
                break;
            }
        }
        logger.info("Android设备连接：" + adbDevice.getSerialNumber());
        // 添加在usb中检测到的设备
        adbDeviceList.add(adbDevice);
        */
        
        refreshAdbDeviceList();
    }
    
    /**
     * 发现安卓设备断开时调用
     */
    private void onAdbDeviceDisConnected(AdbDevice adbDevice) {
        for (Iterator it = adbDeviceList.iterator(); it.hasNext(); ) {
            AdbDevice device = (AdbDevice) it.next();
            
            if (adbDevice.getUsbDevice() == device.getUsbDevice()) {
                logger.info("Android设备断开：" + adbDevice.getSerialNumber());
                it.remove();
                listeners.forEach(l -> l.onAdbDeviceDisConnected(device));
            }
        }
    }
    
    /**
     * 检测该UsbDevice是否是安卓设备
     * @param usbDevice
     * @return 检测到的安卓设备
     */
    private List<AdbDevice> checkAdbDevices(UsbDevice usbDevice) {
        
        List<AdbDevice> adbDevices = new ArrayList<>();
        
        UsbDeviceDescriptor deviceDesc = usbDevice.getUsbDeviceDescriptor();
        
        // Ignore devices from Non-ADB vendors
        // 这步不要，要不然杂牌手机就没法检测到
        // if (!AdbDevice.isAdbVendor(deviceDesc.idVendor())) return adbDevices;
        
        // Check interfaces of device
        UsbConfiguration config = usbDevice.getActiveUsbConfiguration();
        for (UsbInterface iface: (List<UsbInterface>) config.getUsbInterfaces())
        {
            List<UsbEndpoint> endpoints = iface.getUsbEndpoints();
        
            // Ignore interface if it does not have two endpoints
            if (endpoints.size() != 2) continue;
        
            // Ignore interface if it does not match the ADB specs
            if (!AdbDevice.isAdbInterface(iface)) continue;
        
            UsbEndpointDescriptor ed1 =
                    endpoints.get(0).getUsbEndpointDescriptor();
            UsbEndpointDescriptor ed2 =
                    endpoints.get(1).getUsbEndpointDescriptor();
        
            // Ignore interface if endpoints are not bulk endpoints
            if (((ed1.bmAttributes() & UsbConst.ENDPOINT_TYPE_BULK) == 0) ||
                    ((ed2.bmAttributes() & UsbConst.ENDPOINT_TYPE_BULK) == 0))
                continue;
        
            // Determine which endpoint is in and which is out. If both
            // endpoints are in or out then ignore the interface
            byte a1 = ed1.bEndpointAddress();
            byte a2 = ed2.bEndpointAddress();
            byte in, out;
            if (((a1 & UsbConst.ENDPOINT_DIRECTION_IN) != 0) &&
                    ((a2 & UsbConst.ENDPOINT_DIRECTION_IN) == 0)) {
                in = a1;
                out = a2;
            } else if (((a2 & UsbConst.ENDPOINT_DIRECTION_IN) != 0) &&
                    ((a1 & UsbConst.ENDPOINT_DIRECTION_IN) == 0)) {
                out = a1;
                in = a2;
            } else { 
                continue;
            }
            
            adbDevices.add(new AdbDevice(usbDevice, iface, in, out));
        }
        return adbDevices;
    }
    
    /**
     * 与adb同步设备状态
     * why？有可能设备是通过wifi或bt连接，这样usb接口是检测不到的
     */
    private void refreshAdbDeviceList() {
        List<AdbDevice> tmpAdbDeviceList = new ArrayList<>(this.adbDeviceList);
        IDevice[] iDevices = getIDevices();
        // 添加新的adb设备
        for (IDevice iDevice : iDevices) {
            boolean exists = false;
            for (AdbDevice adbDev : tmpAdbDeviceList) {
                if (adbDev.getIDevice().getSerialNumber().equals(iDevice.getSerialNumber())) {
                    exists = true;
                    break;
                }
            }
            // 如果在已有列表不存在，添加到已有列表
            if (!exists) {
                AdbDevice device = new AdbDevice(iDevice);
                logger.info("Android设备连接：" + device.getSerialNumber());
                this.adbDeviceList.add(device);
                listeners.forEach(l -> l.onAdbDeviceConnected(device));
            }
        }
        // 移除已断开的设备
        for (AdbDevice adbDev : tmpAdbDeviceList) {
            boolean exists = false;
            for (IDevice iDevice : iDevices) {
                if (adbDev.getIDevice().getSerialNumber().equals(iDevice.getSerialNumber())) {
                    exists = true;
                    break;
                }
            }
            // 如果在已有列表不存在，添加到已有列表
            if (!exists) {
                onAdbDeviceDisConnected(adbDev);
            }
        }
    }
    
    private String getADBPath(){
        if (adbPath == null){
            adbPath = System.getenv("ANDROID_SDK_ROOT");
            if(adbPath != null){
                adbPath += File.separator + adbPlatformTools;
            }else {
                adbPath = "adb";
                return adbPath;
            }
        }
        adbPath += File.separator + "adb";
        return adbPath;
    }

    private void init() {
        AndroidDebugBridge.init(false);
        adb = AndroidDebugBridge.createBridge(getADBPath(), true);
        listeners = new ArrayList<>();
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
    
    /**
     * 获取ADB命令返回的设备列表
     * @return IDevices
     */
    public IDevice[] getIDevices() {
        return adb.getDevices();
    }
    
    public List<AdbDevice> getDevices() {
        return this.adbDeviceList;
    }
    
    public AdbDevice getDevice(String serialNumber) {
        for (AdbDevice device : adbDeviceList) {
            if (device.getSerialNumber().equals(serialNumber)) {
                return device;
            }
        }
        return null;
    }

    public AdbDevice getFirstDevice() {
        if (adbDeviceList.size() > 0) {
            return adbDeviceList.get(0);
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

    /**
     * TODO: 添加自定义adb命令，原因是安卓手表的传输速度太慢，导致adb push超时错误
     * @param device
     * @return
     */
    public String executePushFile(IDevice device, String src, String dst) {
        final File adbFile = new File(AdbServer.server().adbPath);
        final SettableFuture future = SettableFuture.create();
        (new Thread(new Runnable() {
            public void run() {
                ProcessBuilder pb = new ProcessBuilder(new String[]{adbFile.getPath(), "-s", device.getSerialNumber(), "push", src, dst});
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
                        while((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        future.set(sb.toString());
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

        String s = "";

        try {
            s = (String) future.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            return null;
        }
        return s;
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
    
    public void addListener(IAdbServerListener listener) {
        this.listeners.add(listener);
    }
    
    class MyUSBListener implements UsbServicesListener {
        
        @Override
        public void usbDeviceAttached(UsbServicesEvent usbServicesEvent) {
            UsbDevice device = usbServicesEvent.getUsbDevice();
            if (!device.isUsbHub()) {
                onUsbDeviceConnected(device);
            }
        }
        
        @Override
        public void usbDeviceDetached(UsbServicesEvent usbServicesEvent) {
            UsbDevice device = usbServicesEvent.getUsbDevice();
            if (!device.isUsbHub()) {
                onUsbDeviceDisConnected(device);
            }
        }
    }
}
