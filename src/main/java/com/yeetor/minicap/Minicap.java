package com.yeetor.minicap;

import com.android.ddmlib.*;
import com.yeetor.adb.AdbForward;
import com.yeetor.adb.AdbServer;
import com.yeetor.util.Constant;
import com.yeetor.util.Util;
import org.apache.commons.lang3.StringUtils;
import sun.misc.BASE64Decoder;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by harry on 2017/4/17.
 */
public class Minicap {

    private static final String MINICAP_BIN = "minicap";
    private static final String MINICAP_SO = "minicap.so";
    private static final String REMOTE_PATH = "/data/local/tmp";

    private IDevice device;

    // 物理屏幕宽高
    private Size deviceSize;

    private boolean running = false;

    // 启动minicap的线程
    private Thread minicapThread, minicapInitialThread, dataReaderThread, imageParserThread;

    private AdbForward forward;

    // listener
    private List<MinicapListener> listenerList = new ArrayList<MinicapListener>();

    private BlockingQueue<byte[]> dataQueue = new LinkedBlockingQueue<byte[]>();

    private Banner banner;

    private Socket minicapSocket;


    public static void installMinicap(IDevice device) throws MinicapInstallException {
        if (device == null) {
            throw new MinicapInstallException("device can't be null");
        }

        String sdk = device.getProperty(Constant.PROP_SDK);
        String abi = device.getProperty(Constant.PROP_ABI);

        if (StringUtils.isEmpty(sdk) || StringUtils.isEmpty(abi)) {
            throw new MinicapInstallException("cant not get device info. please check device is connected");
        }

        sdk = sdk.trim();
        abi = abi.trim();
        // minicap
        File minicap_bin = Constant.getMinicap(abi);
        if (!minicap_bin.exists()) {
            throw new MinicapInstallException("File: " + minicap_bin.getAbsolutePath() + " not exists!");
        }
        try {
            device.pushFile(minicap_bin.getAbsolutePath(), REMOTE_PATH + "/" + MINICAP_BIN);
        } catch (Exception e) {
            throw new MinicapInstallException(e.getMessage());
        }

        AdbServer.executeShellCommand(device, "chmod 777 " + REMOTE_PATH + "/" + MINICAP_BIN);

        // minicap.so
        File minicap_so = Constant.getMinicapSo(abi, sdk);
        if (!minicap_so.exists()) {
            throw new MinicapInstallException("File: " + minicap_so.getAbsolutePath() + " not exists!");
        }

        try {
            device.pushFile(minicap_so.getAbsolutePath(), REMOTE_PATH + "/" + MINICAP_SO);
        } catch (Exception e) {
            throw new MinicapInstallException(e.getMessage());
        }
    }

    public Minicap(IDevice device) {
        this.device = device;

        try {
            installMinicap(device);
        } catch (MinicapInstallException e) {
            e.printStackTrace();
        }

        // init size
        String str = AdbServer.executeShellCommand(device, "wm size");
        if (str != null && !str.isEmpty()) {
            String sizeStr = str.split(":")[1];
            int screenWidth = Integer.parseInt(sizeStr.split("x")[0].trim());
            int screenHeight = Integer.parseInt(sizeStr.split("x")[1].trim());
            deviceSize = new Size(screenWidth, screenHeight);
        }
    }

    public Minicap(String serialNumber) {
        this(AdbServer.server().getDevice(serialNumber));
    }

    public Minicap() {
        this(AdbServer.server().getFirstDevice());
    }

    public void addEventListener(MinicapListener listener) {
        if (listener != null) {
            this.listenerList.add(listener);
        }
    }

    /*
    Usage: /data/local/tmp/minicap [-h] [-n <name>]
      -d <id>:       Display ID. (0)
      -n <name>:     Change the name of the abtract unix domain socket. (minicap)
      -P <value>:    Display projection (<w>x<h>@<w>x<h>/{0|90|180|270}).
      -Q <value>:    JPEG quality (0-100).
      -s:            Take a screenshot and output it to stdout. Needs -P.
      -S:            Skip frames when they cannot be consumed quickly enough.
      -t:            Attempt to get the capture method running, then exit.
      -i:            Get display information in JSON format. May segfault.
     */
    public String getMinicapCommand(int ow, int oh, int dw, int dh, int rotate, boolean shipFrame, String name, String[] args) {
        ArrayList<String> commands = new ArrayList<String>();
        commands.add(String.format("LD_LIBRARY_PATH=%s", REMOTE_PATH));
        commands.add(REMOTE_PATH + "/" + MINICAP_BIN);
        commands.add("-P");
        commands.add(String.format("%dx%d@%dx%d/%d", ow, oh, dw, dh, rotate));
        commands.add("-n");
        commands.add(name);
        if (shipFrame)
            commands.add("-S");
        if (args != null) {
            for (String s : args) {
                commands.add(s);
            }
        }
        String command = StringUtils.join(commands, " ");
        return command;
    }

    public AdbForward createForward() {
        forward = generateForwardInfo();
        try {
            device.createForward(forward.getPort(), forward.getLocalabstract(), IDevice.DeviceUnixSocketNamespace.ABSTRACT);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("create forward failed");
        }
        return forward;
    }

    private void removeForward(AdbForward forward) {
        if (forward == null || !forward.isForward()) {
            return;
        }
        try {
            device.removeForward(forward.getPort(), forward.getLocalabstract(), IDevice.DeviceUnixSocketNamespace.ABSTRACT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 屏幕截图
     *
     * 由于个平台的换行符导致二进制流输出有问题，二进制数据将先base64编码后传输
     *
     * @return
     */
    public byte[] takeScreenShot() {
        String command = getMinicapCommand(deviceSize.w, deviceSize.h, deviceSize.w, deviceSize.h, 0, false, "minicap", new String[] {"-s -b"});
        System.out.println("takeScreenShot:" + command);
        BinaryOutputReceiver receiver = new BinaryOutputReceiver();
        try {
            device.executeShellCommand(command, receiver, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // remove text output
        byte[] bytes = receiver.getOutput();
        do {
            String dataStr = new String(bytes);
            int jpgStart = dataStr.indexOf("/9j/");

            if (jpgStart >= 0) {
                dataStr =  dataStr.substring(jpgStart) ; // jpg特征码base64
            } else {
                break;
            }

            try {
                bytes = new BASE64Decoder().decodeBuffer(dataStr);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            if (bytes[0] != -1 && bytes[1] != -40) {
                System.out.println("not a jpg file!!");
                break;
            }

            return bytes;
        } while(false);

        return new byte[0];
    }

    public void start(int ow, int oh, int dw, int dh, int rotate, boolean shipFrame, String[] args) {
        AdbForward forward = createForward();
        String command = getMinicapCommand(ow, oh, dw, dh ,rotate, shipFrame, forward.getLocalabstract(), args);

        minicapThread = startMinicapThread(command);
        minicapInitialThread = startInitialThread("127.0.0.1", forward.getPort());
    }

    public void start(final float scale, final int rotate) {
        start(deviceSize.w, deviceSize.h, (int)(deviceSize.w * scale), (int)(deviceSize.h * scale), rotate,true, null);
    }

    public void reStart(final float scale, final int rotate) {
        running = false;
        if (minicapThread != null) {
            minicapThread.stop();
        }

        if (dataReaderThread != null) {
            try {
                dataReaderThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (imageParserThread != null) {
            try {
                imageParserThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        start(scale, rotate);
    }

    public void kill() {
        onClose();

        running = false;
        if (minicapThread != null) {
            minicapThread.stop();
        }

        // 关闭socket
        if (minicapSocket != null && minicapSocket.isConnected()) {
            try {
                minicapSocket.close();
            } catch (IOException e) {
            }
            minicapSocket = null;
        }

        if (dataReaderThread != null) {
            try {
                dataReaderThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (imageParserThread != null) {
            try {
                imageParserThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 启动线程开启 minicap
     * @param shellCommand
     * @return 线程
     */
    private Thread startMinicapThread(final String shellCommand) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    device.executeShellCommand(shellCommand, new IShellOutputReceiver() {
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
                try {
                    byte[] bytes = null;
                    int tryTime = 20;
                    while (true) {
                        // 连接minicap启动的服务
                        minicapSocket = new Socket(host, port);
                        InputStream inputStream = minicapSocket.getInputStream();
                        bytes = new byte[4096];
                        int n = inputStream.read(bytes);

                        if (n == -1) {
                            Thread.sleep(10);
                            minicapSocket.close();
                        } else {
                            // bytes内包含有信息，需要给Dataparser处理
                            dataQueue.add(Arrays.copyOfRange(bytes, 0, n));
                            running = true;
                            onStartup(true);

                            // 启动 DataReader  ImageParser
                            dataReaderThread = startDataReaderThread(minicapSocket);
                            imageParserThread = startImageParserThread();
                            break;
                        }

                        tryTime--;
                        if (tryTime == 0) {
                            onStartup(false);
                            break;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        thread.start();
        return thread;
    }

    /**
     * 生成forward信息
     */
    private AdbForward generateForwardInfo() {
        AdbForward[] forwards = AdbServer.server().getForwardList();
        // serial_cap_number
        int maxNumber = 0;
        if (forwards.length > 0) {
            for (AdbForward forward : forwards) {
                if (forward.getSerialNumber().equals(device.getSerialNumber())) {
                    String l = forward.getLocalabstract();
                    String[] s = l.split("_");
                    if (s.length == 3) {
                        int n = Integer.parseInt(s[2]);
                        if (n > maxNumber) maxNumber = n;
                    }
                }
            }
        }
        maxNumber += 1;

        String forwardStr = String.format("%s_cap_%d", device.getSerialNumber(), maxNumber);
        int freePort = Util.getFreePort();
        AdbForward forward = new AdbForward(device.getSerialNumber(), freePort, forwardStr);
        return forward;
    }

    private Thread startDataReaderThread(Socket minicapSocket) {
        Thread thread = new Thread(new DataReader(minicapSocket));
        thread.start();
        return thread;
    }

    private Thread startImageParserThread() {
        Thread thread = new Thread(new ImageParser());
        thread.start();
        return thread;
    }

    private void onStartup(boolean success) {
        for (MinicapListener listener : listenerList) {
            listener.onStartup(this, success);
        }
    }

    private void onClose() {
        for (MinicapListener listener : listenerList) {
            listener.onClose(this);
        }
        removeForward(forward);
    }

    private void onBanner(Banner banner) {
        for (MinicapListener listener : listenerList) {
            listener.onBanner(this, banner);
        }
    }

    private void onJPG(byte[] data) {
        for (MinicapListener listener : listenerList) {
            listener.onJPG(this, data);
        }
    }

    private class DataReader implements Runnable {
        static final int BUFF_SIZ = 4096;
        Socket socket = null;
        InputStream inputStream = null;
        long ts = 0;

        DataReader(Socket minicapSocket) {
            this.socket = minicapSocket;
            try {
                this.inputStream = minicapSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
                onClose();
            }
        }

        @Override
        public void run() {
            try {
                readData();
            } catch (IOException e) {
                System.out.println("lost connection: " + e.getMessage());
                onClose();
            }
        }

        public void readData() throws IOException {
            DataInputStream stream = new DataInputStream(inputStream);
            while (running) {
                byte[] buffer = new byte[BUFF_SIZ];
                ts = System.currentTimeMillis();
                int len = stream.read(buffer);
                if (len == -1) {
                    return;
                }
                if (len == BUFF_SIZ) {
                    dataQueue.add(buffer);
                } else {
                    dataQueue.add(Util.subArray(buffer, 0, len));
                }
            }
        }

    }

    private class ImageParser implements Runnable {
        int readn = 0; // 已读大小
        int bannerLen = 2; // banner信息大小
        int readFrameBytes = 0;
        int frameBodyLength = 0;
        byte[] frameBody = new byte[0];
        long t = 0;

        @Override
        public void run() {
            while (running) {
                try {
                    banner = new Banner();
                    readData();
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                    onClose();
                }
            }
        }

        void readData() throws InterruptedException {
            byte[] buffer = dataQueue.poll(5000, TimeUnit.MILLISECONDS);
            if (buffer == null) { // TODO 使用阻塞队列就不用判断了
                return;
            }
            int length = buffer.length;
            for (int cursor = 0; cursor < length;) {
                int ch = buffer[cursor] & 0xff;
                if (readn < bannerLen) {
                    cursor = parserBanner(cursor, ch);
                } else if(readFrameBytes < 4) { // frame length
                    frameBodyLength += (ch << (readFrameBytes * 8));
                    cursor += 1;
                    readFrameBytes += 1;
                    if (readFrameBytes == 4) {
                        t = System.currentTimeMillis();
                    }
                } else {
                    if (length - cursor >= frameBodyLength) {
                        byte[] subByte = Arrays.copyOfRange(buffer, cursor,
                                cursor + frameBodyLength);
                        frameBody = Util.mergeArray(frameBody, subByte);
                        if ((frameBody[0] != -1) || frameBody[1] != -40) {
                            System.out.println("Frame body does not start with JPG header");
                            return;
                        }
                        byte[] finalBytes = Arrays.copyOfRange(frameBody, 0, frameBody.length);

                        onJPG(finalBytes);

                        cursor += frameBodyLength;
                        frameBodyLength = 0;
                        readFrameBytes = 0;
                        frameBody = new byte[0];

                        long timeused = (System.currentTimeMillis() - t);
                        timeused = timeused == 0 ? 1 : timeused;
                        String log = String.format("jpg: %d timeused: %dms  fps: %d", finalBytes.length, (int)timeused, 1000 / timeused);
                    } else {
                        byte[] subByte = Arrays.copyOfRange(buffer, cursor, length);
                        frameBody = Util.mergeArray(frameBody, subByte);
                        frameBodyLength -= (length - cursor);
                        readFrameBytes += (length - cursor);
                        cursor = length;
                    }
                }
            }
        }

        ////// banner
        int pid = 0;
        int realWidth = 0;
        int realHeight = 0;
        int virtualWidth = 0;
        int virtualHeight = 0;
        int orientation = 0;
        int quirks = 0;
        int parserBanner(int cursor, int ch) {
            switch (cursor) {
                case 0:
                    banner.setVersion(ch);
                    break;
                case 1:
                    bannerLen = ch;
                    banner.setLength(bannerLen);
                    break;
                case 2:
                case 3:
                case 4:
                case 5: {
                    pid += (ch << ((readn - 2) * 8));
                    if (cursor == 5)
                        banner.setPid(pid);
                    break;
                }
                case 6:
                case 7:
                case 8:
                case 9:
                {
                    realWidth += (ch << ((readn - 6) * 8));
                    if (cursor == 9)
                        banner.setReadWidth(realWidth);
                    break;
                }

                case 10:
                case 11:
                case 12:
                case 13:
                    realHeight += (ch << ((readn - 10) * 8));
                    if (cursor == 13)
                        banner.setReadHeight(realHeight);
                    break;
                case 14:
                case 15:
                case 16:
                case 17:
                    virtualWidth += (ch << ((readn - 14) * 8));
                    if (cursor == 17)
                        banner.setVirtualWidth(virtualWidth);
                    break;
                case 18:
                case 19:
                case 20:
                case 21:
                    virtualHeight += (ch << ((readn - 18) * 8));
                    if (cursor == 21)
                        banner.setVirtualHeight(virtualHeight);
                    break;
                case 22:
                    orientation = ch * 90;
                    banner.setOrientation(orientation);
                    break;
                case 23:
                    quirks = ch;
                    banner.setQuirks(quirks);
                    onBanner(banner);
                    break;
            }
            ++readn;
            ++cursor;
            return cursor;
        }
    }
}
