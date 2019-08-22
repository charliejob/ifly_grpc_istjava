package com.iflytek.vcp.voice.engine.ist.client.util;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: mingyanliao
 * Date: 2019-04-09
 * Time: 下午5:05
 */
public class PareResult {
    public static String formatSentence(String jsonstr) {
        if (jsonstr.length()==0)
            return "";
        StringBuilder result = new StringBuilder();
        try {
            JSONObject jsonObj =JSONObject.parseObject(jsonstr);
            if (jsonObj.containsKey("cn")) {
                // ist
                JSONArray rtList = jsonObj.getJSONObject("cn").getJSONObject("st").getJSONArray("rt");
                for (int i = 0; i < rtList.size(); i++) {
                    result.append(getRT(rtList.getJSONObject(i)));
                }
            } else if (jsonObj.containsKey("ws")) {
                // iat
                result.append(getRT(jsonObj));
            } else if (jsonObj.containsKey("lattice")) {
                // quark
                if (0 == jsonObj.getJSONObject("state").getInteger("ok")) {
                    JSONArray latticeList = jsonObj.getJSONArray("lattice");
                    for (int i = 0; i < latticeList.size(); i++) {
                        result.append("\n");
                        String best = latticeList.getJSONObject(i).getString("json_1best");
                        JSONObject bestJsonObj =JSONObject.parseObject(best);
                        JSONArray rtList = bestJsonObj.getJSONObject("st").getJSONArray("rt");
                        for (int j = 0; j < rtList.size(); j++) {
                            result.append(getRT(rtList.getJSONObject(j)));
                        }
                    }
                }
            }
        } catch (Exception e) {
            result.append("exp: ").append(e.getMessage());
        }
        return result.toString();
    }

    public static String getRT(JSONObject rt) throws Exception {
        StringBuilder sb = new StringBuilder();
        JSONArray wsArray = rt.getJSONArray("ws");
        if (wsArray != null && wsArray.size() != 0) {
            final int size = wsArray.size();
            for (int i = 0; i < size; i++) {
                JSONObject ws = wsArray.getJSONObject(i);
                JSONArray cwArray = ws.getJSONArray("cw");
                final int sizeCw = cwArray.size();
                for (int j = 0; j < sizeCw; j++) {
                    JSONObject cw = cwArray.getJSONObject(j);
                    String w = cw.getString("w");
                    sb.append(w);
                }
            }
        }
        return sb.toString();
    }
}
