package com.yeetor.util;

import java.io.File;

/**
 * Created by harry on 2017/4/17.
 */
public class Constant {

    public static final String PROP_ABI = "ro.product.cpu.abi";
    public static final String PROP_SDK = "ro.build.version.sdk";

    public static File getMinicap() {
        return new File("resources" + File.separator + "minicap" + File.separator + "bin");
    }

    public static File getMinicapSo() {
        return new File("resources" + File.separator + "minicap" + File.separator + "shared");
    }

    public static File getMinitouchBin() {
        return new File("resources" + File.separator + "minitouch");
    }

}
