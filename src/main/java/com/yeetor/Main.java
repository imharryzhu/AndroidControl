package com.yeetor;

import com.neovisionaries.ws.client.WebSocketException;
import com.yeetor.androidcontrol.RemoteClient;
import com.yeetor.minicap.MinicapInstallException;
import com.yeetor.androidcontrol.LocalServer;

import java.io.IOException;
import java.security.InvalidParameterException;


/**
 * Created by harry on 2017/4/15.
 */
public class Main {
    /**
     * [server port]
     * [client ip port serialNumber]
     * [client ip port]
     */
    public static void main(String[] args) throws MinicapInstallException {

        try {
            Config config = new Config(args);
            if (config.isLocal) {
                new LocalServer(config.port).start();
            } else {
                new RemoteClient(config.ip, config.port, config.serialNumber);
            }
        } catch (InvalidParameterException ex) {
            System.out.println("localserver <port>: 启动本地服务器(p2p)\nclient <ip> <port> [serialNumber] 启动客户端");
            System.exit(0);
        } catch (WebSocketException|InterruptedException e) {
            System.out.println("启动服务器失败: " + e.getMessage());
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    static class Config {
        boolean isLocal = true;
        String ip = "";
        int port = 6655;
        String serialNumber = "";

        Config(String[] args) throws InvalidParameterException {
            if (args.length > 0) {
                if ("remoteserver".equals(args[0])) {
                    isLocal = false;
                }
            }

            if (isLocal) {
                if (args.length == 2) {
                    port = Integer.parseInt(args[1]);
                } else {
                    throw new InvalidParameterException("参数不正确");
                }
            } else {
                if (args.length == 3) {
                    ip = args[1];
                    port = Integer.parseInt(args[2]);
                } else if (args.length == 4) {
                    ip = args[1];
                    port = Integer.parseInt(args[2]);
                    serialNumber = args[3];
                } else {
                    throw new InvalidParameterException("参数不正确");
                }
            }
        }
    }

}
