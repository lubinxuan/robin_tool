import javax.script.ScriptException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xuanlubin on 2016/6/29.
 */
public class TestTB {
    public static void main(String[] args) throws UnsupportedEncodingException, ScriptException {

        Map<String, Object> params = new HashMap<>();
        params.put("id", "521393476281");
        params.put("page", 1);

        String rateApi = TBSignUtil.api("rate", params, "0374c03019471d04b38ced80c23f07bc");


    }
}
