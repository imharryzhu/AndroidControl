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

package com.yeetor;

import com.neovisionaries.ws.client.WebSocketException;
import com.yeetor.androidcontrol.client.RemoteClient;
import com.yeetor.androidcontrol.server.LocalServer;
import com.yeetor.androidcontrol.server.RemoteServer;
import com.yeetor.usb.USBListener;

import javax.usb.*;
import javax.usb.event.UsbDeviceListener;
import javax.usb.event.UsbServicesEvent;
import javax.usb.event.UsbServicesListener;
import java.security.InvalidParameterException;


/**
 * Created by harry on 2017/4/15.
 */
public class Main {
    /**
     * [server port]
     * [client ip port serialNumber]
     * [client ip port]
     */
    public static void main(String[] args) throws UsbException {

        // 监听USB的变化
        USBListener.listen();
        
        // parse命令行
        
        try {
            Config config = new Config(args);

            if (config.isClient) {
                new RemoteClient(config.ip, config.port, config.key, config.serialNumber);
            } else {
                if (config.isLocal) {
                    new LocalServer(config.port).start();
                } else {                 
                    new RemoteServer(config.port).start();
                }
            }
        } catch (InvalidParameterException ex) {
            System.out.println("localserver <port>: 启动本地服务器(p2p)\n remoteserver <port> 启动服务器 \nremoteclient <ip> <port> <key> [serialNumber] 启动客户端");
            System.exit(0);
        } catch (WebSocketException|InterruptedException e) {
            System.out.println("启动服务器失败: " + e.getMessage());
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    static class Config {
        boolean isLocal = true;
        boolean isClient = false;
        String ip = "";
        int port = 6655;
        String serialNumber = "";
        String key = "";

        Config(String[] args) throws InvalidParameterException {
            if (args.length > 0) {
                if ("localserver".equals(args[0])) {
                    isLocal = true;
                    isClient = false;
                } else if ("remoteserver".equals(args[0])) {
                    isLocal = false;
                    isClient = false;
                } else {
                    isLocal = true;
                    isClient = true;
                }
            }

            if (isClient) {
                if (args.length == 4) {
                    ip = args[1];
                    port = Integer.parseInt(args[2]);
                    key = args[3];
                } else if (args.length == 5) {
                    ip = args[1];
                    port = Integer.parseInt(args[2]);
                    key = args[3];
                    serialNumber = args[4];
                } else {
                    throw new InvalidParameterException("参数不正确");
                }
            } else {
                if (args.length == 2) {
                    port = Integer.parseInt(args[1]);
                } else {
                    throw new InvalidParameterException("参数不正确");
                }
            }
        }
    }

}
