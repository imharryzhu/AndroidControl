package com.yeetor.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.ResponseCache;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

/**
 * Created by harry on 2017/4/17.
 */
public class Constant {

    public static final String PROP_ABI = "ro.product.cpu.abi";
    public static final String PROP_SDK = "ro.build.version.sdk";

    public static File getMinicap(String abi) {

        URI uri = null;
        try {
            uri = ClassLoader.getSystemResource("minicap" + File.separator + "bin" +
                    File.separator + abi + File.separator + "minicap").toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return new File(uri);
    }

    public static File getMinicapSo(String abi, String sdk) {
        URI uri = null;
        try {
            uri = ClassLoader.getSystemResource("minicap" + File.separator + "shared" +
                    File.separator + "android-" + sdk + File.separator + abi + File.separator + "minicap.so").toURI();

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return new File(uri);
    }

    public static File getMinitouchBin(String abi) {
        URI uri = null;
        try {
            uri = ClassLoader.getSystemResource("minitouch" + File.separator +
                    File.separator + abi + File.separator + "minitouch").toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return new File(uri);
    }

}
