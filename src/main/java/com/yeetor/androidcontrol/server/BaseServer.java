package com.yeetor.androidcontrol.server;

import com.alibaba.fastjson.JSON;
import com.android.ddmlib.IDevice;
import com.yeetor.adb.AdbServer;
import com.yeetor.androidcontrol.DeviceInfo;

import java.util.ArrayList;

/**
 * Created by harry on 2017/5/9.
 */
public class BaseServer {

    /**
     * 获取设备信息的JSON数据
     * @return
     */
    public String getDevicesJSON() {
        IDevice[] devices = AdbServer.server().getDevices();
        ArrayList<DeviceInfo> list = new ArrayList<DeviceInfo>();
        for (IDevice device : devices) {
            list.add(new DeviceInfo(device)); // TODO 耗时长，需优化
        }
        return JSON.toJSONString(list);
    }





}
