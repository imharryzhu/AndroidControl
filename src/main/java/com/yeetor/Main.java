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

package com.yeetor;

import com.yeetor.adb.AdbServer;
import com.yeetor.server.AndroidControlServer;
import com.yeetor.util.JarTool;
import org.apache.log4j.Logger;

/**
 * Created by harry on 2017/4/15.
 */
public class Main {
    private static Logger logger = Logger.getLogger(Main.class);
    
    /**
     * [server port]
     * [client ip port serialNumber]
     * [client ip port]
     */
    public static void main(String[] args) throws Exception {
        
        // nc 127.0.0.1 4433 进入命令行交互界面
        // Console.getInstance().listenOnTCP(4433);
        
        // 监听USB的变化
        AdbServer.server().listenUSB();
        
        // 同步ADB的设备列表
        AdbServer.server().listenADB();


        AndroidControlServer server = new AndroidControlServer();
        server.listen(6655);
    }
}
