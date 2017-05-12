package com.yeetor.engine;

import com.yeetor.engine.js.Functions;
import com.yeetor.util.Constant;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.internal.objects.annotations.ScriptClass;
import jdk.nashorn.internal.runtime.ScriptFunction;
import jdk.nashorn.internal.runtime.ScriptRuntime;

import javax.script.*;
import java.io.*;

/**
 * Created by harry on 2017/5/11.
 */
public class JavaScripts {

    public static void test() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        NashornScriptEngine nashornScriptEngine = (NashornScriptEngine) engine;


        try {
            Reader fReader = new InputStreamReader(new FileInputStream(Constant.getResourceFile("init.js")));
            engine.eval(fReader);
        } catch (ScriptException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void show() {
        System.out.println("我是傻逼");
    }


}
