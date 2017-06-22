package com.yeetor.androidcontrol;

import com.alibaba.fastjson.annotation.JSONField;
import com.android.ddmlib.IDevice;
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

    public DeviceInfo(IDevice device) {

        sn = device.getSerialNumber();

        String str = AdbServer.server().executeShellCommand(device, "wm size");
        if (str != null && !str.isEmpty()) {
            String[] sizeStr = str.split(":")[1].split("x");
            width = Integer.parseInt(sizeStr[0].trim());
            height = Integer.parseInt(sizeStr[1].trim());
        }

//        brand = device.getProperty("ro.product.brand");
//        model = device.getProperty("ro.product.model");
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
