package com.yeetor.minicap;

/**
 * Created by harry on 2017/4/18.
 */
public class Banner {
    @Override
    public String toString() {
        return "Banner [version=" + version + ", length=" + length + ", pid="
                + pid + ", readWidth=" + readWidth + ", readHeight="
                + readHeight + ", virtualWidth=" + virtualWidth
                + ", virtualHeight=" + virtualHeight + ", orientation="
                + orientation + ", quirks=" + quirks + "]";
    }
    public int getVersion() {
        return version;
    }
    public void setVersion(int version) {
        this.version = version;
    }
    public int getLength() {
        return length;
    }
    public void setLength(int length) {
        this.length = length;
    }
    public int getPid() {
        return pid;
    }
    public void setPid(int pid) {
        this.pid = pid;
    }
    public int getReadWidth() {
        return readWidth;
    }
    public void setReadWidth(int readWidth) {
        this.readWidth = readWidth;
    }
    public int getReadHeight() {
        return readHeight;
    }
    public void setReadHeight(int readHeight) {
        this.readHeight = readHeight;
    }
    public int getVirtualWidth() {
        return virtualWidth;
    }
    public void setVirtualWidth(int virtualWidth) {
        this.virtualWidth = virtualWidth;
    }
    public int getVirtualHeight() {
        return virtualHeight;
    }
    public void setVirtualHeight(int virtualHeight) {
        this.virtualHeight = virtualHeight;
    }
    public int getOrientation() {
        return orientation;
    }
    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }
    public int getQuirks() {
        return quirks;
    }
    public void setQuirks(int quirks) {
        this.quirks = quirks;
    }

    private int version;
    private int length;
    private int pid;
    private int readWidth;
    private int readHeight;
    private int virtualWidth;
    private int virtualHeight;
    private int orientation;
    private int quirks;
}
