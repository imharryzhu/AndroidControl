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

package com.yeetor.adb;

import javax.usb.UsbDevice;
import javax.usb.UsbInterface;
import javax.usb.UsbInterfaceDescriptor;

public class AdbDevice {
    
    /** Constant for ADB class. */
    private static final byte ADB_CLASS = (byte) 0xff;
    
    /** Constant for ADB sub class. */
    private static final byte ADB_SUBCLASS = 0x42;
    
    /** Constant for ADB protocol. */
    private static final byte ADB_PROTOCOL = 1;
    
    /** The claimed USB ADB interface. */
    private final UsbInterface iface;
    
    /** The in endpoint address. */
    private final byte inEndpoint;
    
    /** The out endpoint address. */
    private final byte outEndpoint;
    
    /** Reference From UsbDevice **/
    private final UsbDevice usbDevice;
    
    /**
     * Constructs a new ADB interface.
     *
     * @param iface
     *            The USB interface. Must not be null.
     * @param inEndpoint
     *            The in endpoint address.
     * @param outEndpoint
     *            THe out endpoint address.
     */
    public AdbDevice(UsbDevice usbDevice, UsbInterface iface, byte inEndpoint,
                     byte outEndpoint)
    {
        if (iface == null)
            throw new IllegalArgumentException("iface must be set");
        this.usbDevice = usbDevice;
        this.iface = iface;
        this.inEndpoint = inEndpoint;
        this.outEndpoint = outEndpoint;
    }
    
    /**
     * Checks if the specified vendor is an ADB device vendor.
     *
     * @param vendorId
     *            The vendor ID to check.
     * @return True if ADB device vendor, false if not.
     */
    public static boolean isAdbVendor(short vendorId)
    {
        for (short adbVendorId: Vendors.VENDOR_IDS)
            if (adbVendorId == vendorId) return true;
        return false;
    }
    
    /**
     * Checks if the specified USB interface is an ADB interface.
     *
     * @param iface
     *            The interface to check.
     * @return True if interface is an ADB interface, false if not.
     */
    public static boolean isAdbInterface(UsbInterface iface)
    {
        UsbInterfaceDescriptor desc = iface.getUsbInterfaceDescriptor();
        return desc.bInterfaceClass() == ADB_CLASS &&
                desc.bInterfaceSubClass() == ADB_SUBCLASS &&
                desc.bInterfaceProtocol() == ADB_PROTOCOL;
    }
    
    public UsbDevice getUsbDevice() {
        return usbDevice;
    }
    
}
