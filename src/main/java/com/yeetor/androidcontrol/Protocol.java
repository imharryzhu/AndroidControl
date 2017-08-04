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

package com.yeetor.androidcontrol;

import com.yeetor.androidcontrol.client.LocalClient;
import com.yeetor.minicap.Minicap;
import com.yeetor.minitouch.Minitouch;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created by harry on 2017/4/18.
 */
public class Protocol {

    public ChannelHandlerContext broswerSocket;
    public ChannelHandlerContext clientSocket;
    public String key;
    public String sn;
    public Minicap minicap;
    public Minitouch minitouch;
    public LocalClient localClient;

    public void broswerDisconnect() {
        if (clientSocket != null) {
            clientSocket.channel().close();
        }
    }

    public void clientDisconnect() {
        if (broswerSocket != null) {
            broswerSocket.channel().close();
        }
    }

    public void setBroswerSocket(ChannelHandlerContext broswerSocket) {
        this.broswerSocket = broswerSocket;
    }

    public void setClientSocket(ChannelHandlerContext clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setMinicap(Minicap minicap) {
        this.minicap = minicap;
    }

    public void setMinitouch(Minitouch minitouch) {
        this.minitouch = minitouch;
    }

    public void setLocalClient(LocalClient localClient) {
        this.localClient = localClient;
    }

    public ChannelHandlerContext getBroswerSocket() {
        return broswerSocket;
    }

    public ChannelHandlerContext getClientSocket() {
        return clientSocket;
    }

    public String getKey() {
        return key;
    }


    public String getSn() {
        return sn;
    }

    public Minicap getMinicap() {
        return minicap;
    }

    public Minitouch getMinitouch() {
        return minitouch;
    }

    public LocalClient getLocalClient() {
        return localClient;
    }

    public void close() {

        if (minicap != null) {
            minicap.kill();
            minicap = null;
        }

        if (minitouch != null) {
            minitouch.kill();
            minitouch = null;
        }

    }
}
