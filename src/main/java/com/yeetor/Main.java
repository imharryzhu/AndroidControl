package com.yeetor;

import com.neovisionaries.ws.client.WebSocketException;
import com.yeetor.androidcontrol.client.RemoteClient;
import com.yeetor.androidcontrol.server.LocalServer;
import com.yeetor.androidcontrol.server.RemoteServer;

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
    public static void main(String[] args) {
        try {
            Config config = new Config(args);

            if (config.isClient) {
                new RemoteClient(config.ip, config.port, config.key, config.serialNumber);
            } else {
                if (config.isLocal) {
                    new LocalServer(config.port).start();
                } else {
                    new RemoteServer(config.port).start();
                }
            }
        } catch (InvalidParameterException ex) {
            System.out.println("localserver <port>: 启动本地服务器(p2p)\n remoteserver <port> 启动服务器 \nremoteclient <ip> <port> <key> [serialNumber] 启动客户端");
            System.exit(0);
        } catch (WebSocketException|InterruptedException e) {
            System.out.println("启动服务器失败: " + e.getMessage());
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    static class Config {
        boolean isLocal = true;
        boolean isClient = false;
        String ip = "";
        int port = 6655;
        String serialNumber = "";
        String key = "";

        Config(String[] args) throws InvalidParameterException {
            if (args.length > 0) {
                if ("localserver".equals(args[0])) {
                    isLocal = true;
                    isClient = false;
                } else if ("remoteserver".equals(args[0])) {
                    isLocal = false;
                    isClient = false;
                } else {
                    isLocal = true;
                    isClient = true;
                }
            }

            if (isClient) {
                if (args.length == 4) {
                    ip = args[1];
                    port = Integer.parseInt(args[2]);
                    key = args[3];
                } else if (args.length == 5) {
                    ip = args[1];
                    port = Integer.parseInt(args[2]);
                    key = args[3];
                    serialNumber = args[4];
                } else {
                    throw new InvalidParameterException("参数不正确");
                }
            } else {
                if (args.length == 2) {
                    port = Integer.parseInt(args[1]);
                } else {
                    throw new InvalidParameterException("参数不正确");
                }
            }
        }
    }

}
