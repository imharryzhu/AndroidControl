package com.yeetor.util;

import com.android.ddmlib.*;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by harry on 2017/4/17.
 */
public class Util {
    public static byte[] subArray(byte[] byte1, int start, int end) {
        byte[] byte2 = new byte[end - start];
        System.arraycopy(byte1, start, byte2, 0, end - start);
        return byte2;
    }

    public static byte[] mergeArray(byte[] a1, byte[] a2) {
        byte[] arr = Arrays.copyOf(a1, a1.length + a2.length);
        System.arraycopy(a2, 0, arr, a1.length, a2.length);
        return arr;
    }

}
