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

package com.yeetor.androidcontrol;

import com.alibaba.fastjson.annotation.JSONField;
import com.android.ddmlib.IDevice;
import com.google.common.base.Strings;
import com.yeetor.adb.AdbDevice;
import com.yeetor.adb.AdbServer;

/**
 * Created by harry on 2017/4/21.
 *
 * 用于网络传输，会被转换成json
 */
public class DeviceInfo {

    /**
     * serialNumber
     */
    private String sn, brand, model;

    private int width, height;

    public DeviceInfo(AdbDevice device) {

        sn = device.getSerialNumber();
        
        String str = device.findPropertyCahe(AdbDevice.SCREEN_SIZE);
        if (!Strings.isNullOrEmpty(str)) {
            String[] sizeStr = str.split("x");
            width = Integer.parseInt(sizeStr[0].trim());
            height = Integer.parseInt(sizeStr[1].trim());
        }
        
    }

    @JSONField(name = "sn")
    public String getSn() {
        return sn;
    }

    @JSONField(name = "w")
    public int getWidth() {
        return width;
    }

    @JSONField(name = "h")
    public int getHeight() {
        return height;
    }

    @JSONField(name = "brand")
    public String getBrand() {
        return brand;
    }

    @JSONField(name = "model")
    public String getModel() {
        return model;
    }
}
