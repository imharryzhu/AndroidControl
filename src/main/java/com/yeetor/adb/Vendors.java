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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Vendors
{
    /** The fixed list of vendor ids. */
    private static short[] FIXED_VENDOR_IDS = new short[] {
            // Acer's USB Vendor ID
            0x0502,
            // Allwinner's USB Vendor ID
            0x1F3A,
            // Amlogic's USB Vendor ID
            0x1b8e,
            // AnyDATA's USB Vendor ID
            0x16D5,
            // Archos's USB Vendor ID
            0x0E79,
            // Asus's USB Vendor ID
            0x0b05,
            // BYD's USB Vendor ID
            0x1D91,
            // Compal's USB Vendor ID
            0x1219,
            // Dell's USB Vendor ID
            0x413c,
            // ECS's USB Vendor ID
            0x03fc,
            // EMERGING_TECH's USB Vendor ID
            0x297F,
            // Emerson's USB Vendor ID
            0x2207,
            // Foxconn's USB Vendor ID
            0x0489,
            // Fujitsu's USB Vendor ID
            0x04C5,
            // Funai's USB Vendor ID
            0x0F1C,
            // Garmin-Asus's USB Vendor ID
            0x091E,
            // Gigabyte's USB Vendor ID
            0x0414,
            // Gigaset's USB Vendor ID
            0x1E85,
            // Google's USB Vendor ID
            0x18d1,
            // Haier's USB Vendor ID
            0x201E,
            // Harris's USB Vendor ID
            0x19A5,
            // Hisense's USB Vendor ID
            0x109b,
            // HP's USB Vendor ID
            0x03f0,
            // HTC's USB Vendor ID
            0x0bb4,
            // Huawei's USB Vendor ID
            0x12D1,
            // INQ Mobile's USB Vendor ID
            0x2314,
            // Intel's USB Vendor ID
            (short) 0x8087,
            // IRiver's USB Vendor ID
            0x2420,
            // K-Touch's USB Vendor ID
            0x24E3,
            // KT Tech's USB Vendor ID
            0x2116,
            // Kobo's USB Vendor ID
            0x2237,
            // Kyocera's USB Vendor ID
            0x0482,
            // Lab126's USB Vendor ID
            0x1949,
            // Lenovo's USB Vendor ID
            0x17EF,
            // LenovoMobile's USB Vendor ID
            0x2006,
            // LG's USB Vendor ID
            0x1004,
            // Lumigon's USB Vendor ID
            0x25E3,
            // Motorola's USB Vendor ID
            0x22b8,
            // MSI's USB Vendor ID
            0x0DB0,
            // MTK's USB Vendor ID
            0x0e8d,
            // NEC's USB Vendor ID
            0x0409,
            // B&N Nook's USB Vendor ID
            0x2080,
            // Nvidia's USB Vendor ID
            0x0955,
            // OPPO's USB Vendor ID
            0x22D9,
            // On-The-Go-Video's USB Vendor ID
            0x2257,
            // OUYA's USB Vendor ID
            0x2836,
            // Pantech's USB Vendor ID
            0x10A9,
            // Pegatron's USB Vendor ID
            0x1D4D,
            // Philips's USB Vendor ID
            0x0471,
            // Panasonic Mobile Communication's USB Vendor ID
            0x04DA,
            // Positivo's USB Vendor ID
            0x1662,
            // Qisda's USB Vendor ID
            0x1D45,
            // Qualcomm's USB Vendor ID
            0x05c6,
            // Quanta's USB Vendor ID
            0x0408,
            // Rockchip's USB Vendor ID
            0x2207,
            // Samsung's USB Vendor ID
            0x04e8,
            // Sharp's USB Vendor ID
            0x04dd,
            // SK Telesys's USB Vendor ID
            0x1F53,
            // Sony's USB Vendor ID
            0x054C,
            // Sony Ericsson's USB Vendor ID
            0x0FCE,
            // T & A Mobile Phones' USB Vendor ID
            0x1BBB,
            // TechFaith's USB Vendor ID
            0x1d09,
            // Teleepoch's USB Vendor ID
            0x2340,
            // Texas Instruments's USB Vendor ID
            0x0451,
            // Toshiba's USB Vendor ID
            0x0930,
            // Vizio's USB Vendor ID
            (short) 0xE040,
            // Wacom's USB Vendor ID
            0x0531,
            // Xiaomi's USB Vendor ID
            0x2717,
            // YotaDevices's USB Vendor ID
            0x2916,
            // Yulong Coolpad's USB Vendor ID
            0x1EBF,
            // ZTE's USB Vendor ID
            0x19D2
    };
    
    /**
     * The effective list of ADB USB vendor IDs (Includiung the ones read from
     * the adb_usb.ini file.
     */
    public static short[] VENDOR_IDS = createVendorIds();
    
    /**
     * Creates the effective list of ADB USB vendor IDs and returns it.
     *
     * @return The effective list of ADB USB vendor IDs.
     */
    private static short[] createVendorIds()
    {
        Set<Short> vendorIds = new HashSet<Short>();
        for (short vendorId: FIXED_VENDOR_IDS)
            vendorIds.add(vendorId);
        File ini = new File(new File(System.getProperty("user.home"),
                ".android"), "adb_usb.ini");
        try
        {
            if (ini.exists())
            {
                BufferedReader reader = new BufferedReader(new FileReader(ini));
                try
                {
                    String line;
                    while ((line = reader.readLine()) != null)
                    {
                        if (line.startsWith("0x"))
                        {
                            vendorIds.add((short) Integer.parseInt(
                                    line.substring(2), 16));
                        }
                    }
                }
                finally
                {
                    reader.close();
                }
            }
        }
        catch (IOException e)
        {
            System.out.println("adb_usb.ini could not be read. Ignoring it.");
        }
        short[] result = new short[vendorIds.size()];
        int i = 0;
        for (Short vendorId: vendorIds)
            result[i++] = vendorId;
        return result;
    }
}
