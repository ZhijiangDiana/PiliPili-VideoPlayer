package utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JsonUtils {

    /**
     * 根据json数据解析返回一个List<HashMap<String, Object>>集合
     * @param json  json数据
     * @return
     */
    public static List<HashMap<String, Object>> getJsonList(String json) throws JSONException {
        List<HashMap<String, Object>> dataList;
        dataList = new ArrayList<>();
        try {
            JSONObject rootObject = new JSONObject(json);
            JSONObject paramzObject = rootObject.getJSONObject("paramz");
            JSONArray feedsArray = paramzObject.getJSONArray("feeds");
            for (int i = 0; i < feedsArray.length(); i++) {
                JSONObject sonObject = feedsArray.getJSONObject(i);
                JSONObject dataObject = sonObject.getJSONObject("data");
                String subjectStr = dataObject.getString("subject");
                String summaryStr = dataObject.getString("summary");
                String coverStr = dataObject.getString("cover");
                HashMap<String, Object> map = new HashMap<>();
                map.put("subject", subjectStr);
                map.put("summary", summaryStr);
                map.put("cover", coverStr);
                dataList.add(map);
            }
            return dataList;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}

