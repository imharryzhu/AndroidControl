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
