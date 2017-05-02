package com.yeetor;

import com.yeetor.minicap.MinicapInstallException;
import com.yeetor.p2p.WSServer;
import com.yeetor.util.Constant;


/**
 * Created by harry on 2017/4/15.
 */
public class Main {
    public static void main(String[] args) throws MinicapInstallException {

        boolean isServer = true;

        if (isServer) {
            // 启动服务器
            try {
                new WSServer(6655).start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
