package com.iflytek.vcp.voice.engine.ist.client;


import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 实时转写引擎客户端
 */
@Slf4j
public class IstClientUtil {
    private String deviceId;
    private String uuid;
    private IstClient client;
    //临时保存转写结果的办法，根据业务情况修改
    private static Map<String,List<String>> resultMap=new HashMap<>();
    
    public IstClientUtil(String deviceId,String serverUrl) {

    	this.deviceId = deviceId;
        this.uuid = UUID.randomUUID().toString().replaceAll("-", "");

         IstSessionParam param = new IstSessionParam(uuid);
         param.setDwa("");
         param.setEos("600000");

         client = new IstClient(serverUrl, param);

         //转写结果回调处理方法
         IstSessionResponse istSessionResponse = new IstSessionResponse() {
             @Override
             public void onCallback(IstSessionResult istSessionResult) {
                 //返回的转写结果原始报文
                 String ansStr=istSessionResult.getAnsStr();

                 List<String> result;
                 //存储结果
                 if (resultMap.containsKey(deviceId)){
                     result=resultMap.get(deviceId);
                 }else{
                     result=new LinkedList<>();
                 }
                 //保存该句转写结果到文件
                 result.add(ansStr);
                 resultMap.put(deviceId,result);
                 log.debug("原始报文转写结果为:{}",ansStr);
                 if (istSessionResult.isEndFlag()) {
                     closeOutFile();
                 }
             }

             @Override
             public void onError(Throwable throwable) {
                 log.error("[onError]: {}" , throwable.getMessage());
                 closeOutFile();
             }

             @Override
             public void onCompleted() {
                 log.info("[onCompleted]");
                 closeOutFile();
             }

             private void closeOutFile() {
                 for (int i = 1; i <= resultMap.get("1234").size(); i++) {
                     String str =  resultMap.get("1234").get(i - 1);
                     log.info(formatSentence(str));
                 }
                 TestAudio.notifyMethod();
                 synchronized (IstClient.class) {
                     IstClient.class.notify();
                 }
             }
         };

         boolean ret = client.connect(istSessionResponse);
         if (!ret) {
             log.error("[sender]: connection error!");
             return ;
         }
    	
    }
    
    public void post(byte[] audio) throws Exception {

       client.post(audio);
      // client.post(ResampleUtil.convert8000To16000(audio));
      
       log.debug("[sender]: audio send complete, waiting...");

   }
    public void end() throws Exception {

        client.end();
        log.debug("[sender]: end!");

    }

    public void closeClient() {
    	 client.close();
         log.info("[sender]: 测试结束!");
    }

    public static Map<String, List<String>> getResultMap() {
        return resultMap;
    }

    public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

    public String getUuid() {
        return uuid;
    }

    public static String formatSentence(String json) {
        if (json == null || json.length() == 0)
            return "";
        StringBuilder result = new StringBuilder();
        try {

            JsonObject jsonObj = new JsonParser().parse(json).getAsJsonObject();
            if (jsonObj.has("cn")) {
                // ist
                JsonObject st = jsonObj.getAsJsonObject("cn").getAsJsonObject("st");
                String rl = st.get("rl") == null ? null : st.get("rl").getAsString();
                JsonArray rtList = st.getAsJsonArray("rt");
                for (int i = 0; i < rtList.size(); i++) {
                    if (null != rl) {
                        result.append(rl).append(":");
                    }
                    result.append(getRT(rtList.get(i).getAsJsonObject()));
                }
            } else if (jsonObj.has("ws")) {
                // iat
                result.append(getRT(jsonObj));
            } else if (jsonObj.has("lattice")) {
                // quark
                if (0 == jsonObj.getAsJsonObject("state").get("ok").getAsInt()) {
                    JsonArray latticeList = jsonObj.getAsJsonArray("lattice");
                    for (int i = 0; i < latticeList.size(); i++) {
                        result.append("\n");
                        String best = latticeList.get(i).getAsJsonObject().get("json_1best").getAsString();
                        JsonObject bestJsonObj = new JsonParser().parse(best).getAsJsonObject();
                        JsonObject st = bestJsonObj.getAsJsonObject("st");
                        String rl = st.get("rl").getAsString();
                        JsonArray rtList = st.getAsJsonArray("rt");
                        for (int j = 0; j < rtList.size(); j++) {
                            if (null != rl) {
                                result.append(rl).append(":");
                            }
                            result.append(getRT(rtList.get(j).getAsJsonObject()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.append("exp: ").append(e.getMessage());
        }
        return result.toString();
    }

    public static String getRT(JsonObject rt) {
        StringBuilder sb = new StringBuilder();
        JsonArray wsArray = rt.getAsJsonArray("ws");
        if (wsArray != null && wsArray.size() != 0) {
            final int size = wsArray.size();
            for (int i = 0; i < size; i++) {
                JsonObject ws = wsArray.get(i).getAsJsonObject();
                JsonArray cwArray = ws.getAsJsonArray("cw");
                final int sizeCw = cwArray.size();
                for (int j = 0; j < sizeCw; j++) {
                    JsonObject cw = cwArray.get(j).getAsJsonObject();
                    String w = cw.get("w").getAsString();
                    sb.append(w);
                }
            }
        }
        return sb.toString();
    }
}