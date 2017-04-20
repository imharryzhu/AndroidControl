package com.yeetor.minitouch;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.yeetor.adb.AdbServer;
import com.yeetor.minicap.MinicapListener;
import com.yeetor.util.Constant;

import javax.naming.ldap.SortKey;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by harry on 2017/4/19.
 */
public class Minitouch {

    private static final String MINITOUCH_BIN_DIR = "resources" + File.separator + "minicap-bin";
    private static final String REMOTE_PATH = "/data/local/tmp";
    private static final String MINITOUCH_BIN = "minitouch";

    private List<MinitouchListener> listenerList = new ArrayList<MinitouchListener>();

    private IDevice device;
    private Thread minitouchThread, minitouchInitialThread;
    private Socket minitouchSocket;
    private OutputStream minitouchOutputStream;

    public static void installMinitouch(IDevice device) throws MinitouchInstallException {
        if (device == null) {
            throw new MinitouchInstallException("device can't be null");
        }

        String sdk = device.getProperty(Constant.PROP_SDK).trim();
        String abi = device.getProperty(Constant.PROP_ABI).trim();

        File minitouch_bin = new File(Constant.getMinitouchBin(), abi + File.separator + MINITOUCH_BIN);
        if (!minitouch_bin.exists()) {
            throw new MinitouchInstallException("File: " + minitouch_bin.getAbsolutePath() + " not exists!");
        }
        try {
            device.pushFile(minitouch_bin.getAbsolutePath(), REMOTE_PATH + "/" + MINITOUCH_BIN);
        } catch (Exception e) {
            throw new MinitouchInstallException(e.getMessage());
        }

        AdbServer.executeShellCommand(device, "chmod 777 " + REMOTE_PATH + "/" + MINITOUCH_BIN);
    }

    public Minitouch(IDevice device) {
        this.device = device;

        try {
            installMinitouch(device);
        } catch (MinitouchInstallException e) {
            e.printStackTrace();
        }
    }

    public Minitouch(String serialNumber) {
        this(AdbServer.server().getDevice(serialNumber));
    }

    public Minitouch() {
        this(AdbServer.server().getFirstDevice());
    }

    public void addEventListener(MinitouchListener listener) {
        if (listener != null) {
            this.listenerList.add(listener);
        }
    }

    public void start() {
        try {
            device.createForward(43255, "minitouch", IDevice.DeviceUnixSocketNamespace.ABSTRACT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String command = "/data/local/tmp/minitouch";
        minitouchThread = startMinitouchThread(command);
        minitouchInitialThread = startInitialThread("127.0.0.1", 43255);
    }

    public void sendEvent(String str) {
        try {
            minitouchOutputStream.write(str.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Thread startMinitouchThread(final String command) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    device.executeShellCommand(command, new IShellOutputReceiver() {
                        @Override
                        public void addOutput(byte[] bytes, int offset, int len) {
                            System.out.println(new String(bytes, offset, len));
                        }
                        @Override
                        public void flush() {}
                        @Override
                        public boolean isCancelled() {
                            return false;
                        }
                    }, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        return thread;
    }

    private Thread startInitialThread(final String host, final int port) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int tryTime = 200;
                while (true) {
                    Socket socket = null;
                    byte[] bytes = new byte[256];
                    try {
                        socket = new Socket(host, port);
                        InputStream inputStream = socket.getInputStream();
                        OutputStream outputStream = socket.getOutputStream();
                        int n = inputStream.read(bytes);

                        if (n == -1) {
                            Thread.sleep(10);
                            socket.close();
                        } else {
                            minitouchSocket = socket;
                            minitouchOutputStream = outputStream;
                            onStartup(true);
                            break;
                        }
                    } catch (Exception ex) {
                        if (socket != null) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        continue;
                    }
                    tryTime--;
                    if (tryTime == 0) {
                        onStartup(false);
                        break;
                    }
                }
            }
        });
        thread.start();
        return thread;
    }

    private void onStartup(boolean success) {
        for (MinitouchListener listener : listenerList) {
            listener.onStartup(this, success);
        }
    }

}
