package tvvedio.hc.com.tvvedio;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import tvvedio.hc.com.tvvedio.utils.Utils;

/**
 * Created by ly on 2019/6/12.
 */

public class JsonUtils {
    static JsonUtils jsonUtils;

    public static JsonUtils getIncetance() {
        if (jsonUtils == null) {
            jsonUtils = new JsonUtils();
        }
        return jsonUtils;
    }

    public Map<String, String> getURLlist(String response) {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = (JsonObject) parser.parse(response);
        JsonObject jsonData = jsonObject.get("data").getAsJsonObject();
        JsonArray jlist = jsonData.get("list").getAsJsonArray();
        Map<String, String> map = new HashMap<>();

        Log.i("lylog", "urlssss jlist.size()=" + jlist.size());
        if (jlist.size() > 0) {
            JsonObject jsonObject1 = (JsonObject) jlist.get(0);

            JsonElement url = jsonObject1.get("url");
            JsonElement theme = jsonObject1.get("theme");
            map.put("url",url.toString());
            map.put("theme",theme.toString());
        }


        Log.i("lylog", "urlssss =" + map.toString());

        return map;
    }

    public String getElement(String elment, String result) {
        JsonParser parser = new JsonParser();
        String s = null;
        JsonObject jsons = (JsonObject) parser.parse(result);
        String code = jsons.get("code").getAsString();
        if ("0".equals(code)) {
            JsonObject dataJson = jsons.get("data").getAsJsonObject();
            s = dataJson.get(elment).getAsString();

        }
        return s;
    }

    public String getTrueUrl(String result) {
        JsonParser parser = new JsonParser();
        JsonObject jsons = (JsonObject) parser.parse(result);

        return jsons.get("result").getAsString();
    }

    public Map<String, String> getQrImaget(String string) {
        Map<String, String> map = new HashMap<>();
        JsonParser parser = new JsonParser();
        JsonObject jsons = (JsonObject) parser.parse(string);
        JsonObject jsondata = jsons.get("data").getAsJsonObject();
        map.put("base64QrCode", jsondata.get("base64QrCode").getAsString());
        map.put("uuid", jsondata.get("uuid").getAsString());
        map.put("yxq", jsondata.get("yxq").getAsString());
        return map;
    }
}
