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

public class AdbForward {
    private String serialNumber;
    private int port;
    private String localabstract;

    private boolean isForward = true;

    public AdbForward(String serialNumber, int port, String localabstract) {
        this.serialNumber = serialNumber;
        this.port = port;
        this.localabstract = localabstract;
    }

    public AdbForward(String str) {
        String[] s = str.split(" ");
        if (s.length != 3) {
            isForward = false;
            return;
        }

        serialNumber = s[0];
        String[] portstr = s[1].split(":");
        if (portstr.length == 2) {
            port = Integer.parseInt(portstr[1]);
        } else {
            isForward = false;
            return;
        }

        String[] localabstractStr = s[2].split(":");
        if (localabstractStr.length == 2) {
            localabstract = localabstractStr[1];
        } else {
            isForward = false;
            return;
        }
    }

    public int getPort() {
        return port;
    }

    public String getLocalabstract() {
        return localabstract;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public boolean isForward() {
        return isForward;
    }
}
