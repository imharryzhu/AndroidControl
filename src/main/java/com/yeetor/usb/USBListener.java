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

package com.yeetor.usb;

import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbServices;
import javax.usb.event.UsbServicesEvent;
import javax.usb.event.UsbServicesListener;
import java.io.UnsupportedEncodingException;

public class USBListener {
    
    private static USBListener instance;
    
    private UsbServices services = null;
    
    private USBListener() {}
    
    public static USBListener getInstance() {
        if (instance == null) {
            instance = new USBListener();   
            instance.init();
        }
        return instance;
    }

    /**
     * 启动USB监听服务
     */
    public static void listen() {
        getInstance();
    }
    
    protected void init() {
        try {
            services = UsbHostManager.getUsbServices();
        } catch (UsbException e) {
            e.printStackTrace(); // TODO: LOG
        }

        services.addUsbServicesListener(new MyListener());
    }
    
    private void onDeviceConnected(UsbServicesEvent usbEvent) {
        // 这个方法的作用只是起到通知的作用

        try {
            System.out.println(usbEvent.getUsbDevice().getSerialNumberString());
        } catch (UsbException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    
    private void onDeviceDisConnected(UsbServicesEvent usbEvent) {
        
    }
    
    
    class MyListener implements UsbServicesListener {
        
        @Override
        public void usbDeviceAttached(UsbServicesEvent usbServicesEvent) {
            if (!usbServicesEvent.getUsbDevice().isUsbHub()) {
                onDeviceConnected(usbServicesEvent);
            }
        }

        @Override
        public void usbDeviceDetached(UsbServicesEvent usbServicesEvent) {
            if (!usbServicesEvent.getUsbDevice().isUsbHub()) {
                onDeviceDisConnected(usbServicesEvent);
            }
        }
    }
    
}



