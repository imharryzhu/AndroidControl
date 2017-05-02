package com.yeetor.util;

import com.android.ddmlib.*;

import java.io.IOException;
import java.net.ServerSocket;
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

    /**
     * 获取闲置端口号
     * @return
     */
    public static int getFreePort() {
        ServerSocket tmp;
        int i = 10000;
        for(; i <= 65535; i++){
            try{
                tmp = new ServerSocket(i);
                tmp.close();
                tmp = null;
                return i;
            }
            catch(Exception e4){
                continue;
            }
        }
        return -1;
    }

}
