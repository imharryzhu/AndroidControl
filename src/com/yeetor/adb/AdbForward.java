package com.yeetor.adb;

/**
 * Created by harry on 2017/4/20.
 */
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
