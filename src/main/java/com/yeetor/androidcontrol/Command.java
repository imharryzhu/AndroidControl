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

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.security.InvalidParameterException;

/**
 * Created by harry on 2017/4/26.
 */
public class Command {

    public enum Schem {
        WAIT("wait"),
        OPEN("open"),
        START("start"),
        WAITTING("waitting"),
        TOUCH("touch"),
        DEVICES("devices"),
        KEYEVENT("keyevent"),
        INPUT("input"),
        SHOT("shot"),
        MINICAP("minicap"),
        MINITOUCH("minitouch"),
        PUSH("push"),
        MESSAGE("message");

        private String schemStr;

        public String getSchemString() {
            return schemStr;
        }

        Schem(String str) {
            schemStr = str;
        }
    }

    private Schem schem;
    private Object content;

    public Command(String command) throws InvalidParameterException {
        // 截取schem
        int splitIndex = -1;
        if ((splitIndex = command.indexOf("://")) == -1) {
            throw new InvalidParameterException(command + " 不是一个合法的Command");
        }

        String schemStr = command.substring(0, splitIndex);
        switch (schemStr) {
            case "wait":
                schem = Schem.WAIT;
                break;
            case "open":
                schem = Schem.OPEN;
                break;
            case "start":
                schem = Schem.START;
                break;
            case "waitting":
                schem = Schem.WAITTING;
                break;
            case "touch":
                schem = Schem.TOUCH;
                break;
            case "devices":
                schem = Schem.DEVICES;
                break;
            case "keyevent":
                schem = Schem.KEYEVENT;
                break;
            case "shot":
                schem = Schem.SHOT;
                break;
            case "input":
                schem = Schem.INPUT;
                break;
            case "minicap":
                schem = Schem.MINICAP;
                break;
            case "minitouch":
                schem = Schem.MINITOUCH;
                break;
            case "push":
                schem = Schem.PUSH;
                break;
            case "message":
                schem = Schem.MESSAGE;
                break;
            default:
                throw new InvalidParameterException(command + " 未知的schem");
        }

        String contentStr = command.substring(splitIndex + 3);

        // 此消息不是json格式。其他都为json键值对
        if (!schem.equals(Schem.TOUCH) && !schem.equals(Schem.KEYEVENT) && !schem.equals(Schem.INPUT) && !schem.equals(Schem.MINICAP) && !schem.equals(Schem.MINITOUCH) && !schem.equals(Schem.MESSAGE)) {
            try {
                this.content = parseContentJson(contentStr);
            } catch (JSONException e) {
                throw new InvalidParameterException(e.getMessage());
            }
        } else {
            this.content = contentStr;
        }
    }

    private Object parseContentJson(String content) throws JSONException {
        if (content == null || content.isEmpty()) {
            return JSONObject.parse("{}");
        }

        Object jsonObj = JSONObject.parse(content);
        if (jsonObj == null) {
            throw new JSONException(content + " 无法解析该json");
        }
        return jsonObj;
    }

    public Schem getSchem() {
        return schem;
    }

    public String getCommandString() {
        return schem.getSchemString() + "://" + getContent();
    }

    public String getContent() {
        if (content != null) {
            if (content instanceof String) {
                return (String) content;
            } else if (content instanceof JSONObject){
                return JSONObject.toJSONString(content);
            }
        }
        return "";
    }

    public String getString(String key, String defVal) {
        if (content != null && content instanceof  JSONObject) {
            String s = ((JSONObject) content).getString(key);
            return s == null ? defVal : s;
        }
        return "";
    }

    public Object get(String key) {
        if (content != null && content instanceof JSONObject) {
            return ((JSONObject) content).get(key);
        }
        return null;
    }

    public static Command ParseCommand(String command) {
        try {
            Command cmd = new Command(command);
            return cmd;
        } catch (InvalidParameterException ex) {
            return null;
        }
    }

}
