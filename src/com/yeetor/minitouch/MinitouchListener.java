package com.yeetor.minitouch;

import com.yeetor.minicap.Banner;
import com.yeetor.minicap.Minicap;

/**
 * Created by harry on 2017/4/19.
 */
public interface MinitouchListener {
    // minitouch启动完毕后
    public void onStartup(Minitouch minitouch, boolean success);
}
