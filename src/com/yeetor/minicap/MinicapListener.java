package com.yeetor.minicap;

/**
 * Created by harry on 2017/4/18.
 */
public interface MinicapListener {
    // minicap启动完毕后
    public void onStartup(Minicap minicap, boolean success);
    // banner信息读取完毕
    public void onBanner(Minicap minicap, Banner banner);
    // 读取到图片信息
    public void onJPG(Minicap minicap, byte[] data);
}
