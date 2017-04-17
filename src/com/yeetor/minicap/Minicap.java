package com.yeetor.minicap;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import com.yeetor.util.Constant;

import java.io.File;
import java.io.IOException;

/**
 * Created by harry on 2017/4/17.
 */
public class Minicap {

    private static final String MINICAP_BIN_DIR = "resources" + File.separator + "minicap-bin";
    private static final String MINICAP_SO_DIR = "resources" + File.separator + "minicap-so";
    private static final String MINICAP_BIN = "minicap";
    private static final String MINICAP_SO = "minicap.so";
    private static final String REMOTE_PATH = "/data/local/tmp";

    private static final String PROP_ABI = "ro.product.cpu.abi";
    private static final String PROP_SDK = "ro.build.version.sdk";

    public static void installMinicap(IDevice device) throws MinicapInstallException {
        if (device == null) {
            throw new MinicapInstallException("device can't be null");
        }

        String sdk = device.getProperty(PROP_SDK).trim();
        String abi = device.getProperty(PROP_ABI).trim();

        // minicap
        File minicap_bin = new File(Constant.getMinicap(), abi + File.separator + MINICAP_BIN);
        if (!minicap_bin.exists()) {
            throw new MinicapInstallException("File: " + minicap_bin.getAbsolutePath() + " not exists!");
        }
        try {
            device.pushFile(minicap_bin.getAbsolutePath(), REMOTE_PATH + "/" + MINICAP_BIN);
        } catch (Exception e) {
            throw new MinicapInstallException(e.getMessage());
        }

        // minicap.so
        File minicap_so = new File(Constant.getMinicapSo(), "android-" + sdk + File.separator + abi + File.separator + MINICAP_SO);
        if (!minicap_so.exists()) {
            throw new MinicapInstallException("File: " + minicap_so.getAbsolutePath() + " not exists!");
        }

        try {
            device.pushFile(minicap_so.getAbsolutePath(), REMOTE_PATH + "/" + MINICAP_SO);
        } catch (Exception e) {
            throw new MinicapInstallException(e.getMessage());
        }
    }


}
