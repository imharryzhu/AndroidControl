package com.yeetor.androidcontrol.message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import javafx.util.Pair;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by harry on 2017/5/10.
 */
public class BinaryMessage {

    private String type;
    private static Map<String, Class> map = new HashMap<>();

    static {
        map.put("file", FileMessage.class);
    }

    public static BinaryMessage parse(String json) {
        JSONObject jsonObject = (JSONObject) JSONObject.parse(json);
        String type = jsonObject.getString("type");
        Class c = map.get(type);
        BinaryMessage instance = null;
        try {
            instance = (BinaryMessage) JSONObject.toJavaObject(jsonObject, c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
