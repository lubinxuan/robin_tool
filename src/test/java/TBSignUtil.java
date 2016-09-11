import com.alibaba.fastjson.JSON;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * Created by xuanlubin on 2016/6/29.
 */
public class TBSignUtil {
    private static final ScriptEngine engine;

    static {
        ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("javascript");
        try {
            InputStreamReader isr = new InputStreamReader(TBSignUtil.class.getClassLoader().getResourceAsStream("TaobaoH5.js"), "utf-8");
            engine.eval(isr);
            isr.close();
        } catch (Throwable r) {
            throw new RuntimeException(r);
        }
    }

    public static String api(String api, Map params, String token) {
        try {
            String p = JSON.toJSONString(params);
            return (String) engine.eval("_tb_api_url(api." + api + "," + p + ",\"" + token + "\")");
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }
}
