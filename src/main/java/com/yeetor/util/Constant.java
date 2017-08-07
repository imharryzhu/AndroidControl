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

package com.yeetor.util;

import io.netty.util.internal.SystemPropertyUtil;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * Created by harry on 2017/4/17.
 */
public class Constant {

    public static final String PROP_ABI = "ro.product.cpu.abi";
    public static final String PROP_SDK = "ro.build.version.sdk";
    public static final String PROPERTIES_FILE = "yeetor.properties";
    
    private static Properties properties = null;
    
    /**
     * 这里初始化配置文件......yeetor.properties
     * 
     * 优先级：同级目录 > 包内部默认文件
     */
    static {
        properties = new Properties();
        if (!loadPropertiesWithFileName(JarTool.getJarDir() + File.separator + PROPERTIES_FILE)) {
            InputStream inputStream = ClassLoader.getSystemResourceAsStream(PROPERTIES_FILE);
            if (!loadPropertiesWithStream(inputStream)) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    static boolean loadPropertiesWithFileName(String fileName) {
        File file = new File(fileName);
        if (file.isHidden() || !file.exists()) {
            return false;
        }
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            loadPropertiesWithStream(inputStream);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
    
    static boolean loadPropertiesWithStream(InputStream inputStream) {
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    
    public static File getResourceDir() {
        File resources = new File(properties.getProperty("resource.root"));
        return resources;
    }

    public static File getResourceFile(String name) {
        File dir = getResourceDir();
        return new File(dir, name);
    }

    public static File getMinicap(String abi) {
        File resources = getResourceDir();
        if (resources.exists()) {
            return new File(resources, "minicap" + File.separator + "bin" +
                    File.separator + abi + File.separator + "minicap");
        }
        return null;
    }

    public static File getMinicapSo(String abi, String sdk) {
        File resources = getResourceDir();
        if (resources.exists()) {
            return new File(resources, "minicap" + File.separator + "shared" +
                    File.separator + "android-" + sdk + File.separator + abi + File.separator + "minicap.so");
        }
        return null;
    }

    public static File getMinitouchBin(String abi) {
        File resources = getResourceDir();
        if (resources.exists()) {
            return new File(resources, "minitouch" + File.separator +
                    File.separator + abi + File.separator + "minitouch");
        }
        return null;
    }

    public static File getTmpFile(String fileName) {
        String tmpdir = System.getProperty("java.io.tmpdir");
        File tmp = new File(tmpdir);
        tmp = new File(tmp, "AndroidControl");
        if (!tmp.exists()) {
            tmp.mkdirs();
        }
        return new File(tmp, fileName);
    }

}
