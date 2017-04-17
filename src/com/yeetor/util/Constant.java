package com.yeetor.util;

import java.io.File;

/**
 * Created by harry on 2017/4/17.
 */
public class Constant {

    public static File getMinicap() {
        return new File("resources/minicap" + File.separator + "bin");
    }

    public static File getMinicapSo() {
        return new File("resources/minicap" + File.separator + "shared");
    }


}
