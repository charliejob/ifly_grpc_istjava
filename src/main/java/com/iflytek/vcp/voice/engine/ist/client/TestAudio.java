package com.iflytek.vcp.voice.engine.ist.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: mingyanliao
 * Date: 2019-04-19
 * Time: 下午1:51
 */
@Slf4j
public class TestAudio {

    private static String audioFile = "/opt/sounds/8k_20190106161334537_01.wav";
    //服务器地址：IP:端口
//    private static String serverUrl="124.243.226.44:18086";
   // private static String serverUrl = "10.40.7.26:30051";
    //172.16.105.19
    private static String serverUrl = "172.16.105.19:30051";
    //采样率，可支持8（8k）或16（16k）
    private static int sampleRate = 16;

    TestAudio() throws Exception{
        test();
    }


    /**
     * 实时转写引擎测试入口方法
     *
     * @throws Exception
     */
    public void test() throws Exception {

        String deviceId = "1234";

        System.out.println("=============================ccc年会说==========================");
        log.debug("======================abc==============================");

        IstClientUtil client = new IstClientUtil(deviceId, serverUrl);

        File audio = new File(audioFile);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(audio));
        byte[] bits = new byte[1280];
        int len = 0;
        log.debug("开始发送音频流 id:{}", client.getUuid());
        while ((len = bis.read(bits)) != -1) {
            if (len == 1280) {
                if (8 == sampleRate) {
                    client.post(ResampleUtil.convert8kTo16k(bits));
                } else {
                    client.post(bits);
                }
            } else {
                byte[] temp_bits = new byte[len];
                System.arraycopy(bits, 0, temp_bits, 0, len);
                if (8 == sampleRate) {
                    client.post(ResampleUtil.convert8kTo16k(temp_bits));
                } else {
                    client.post(bits);
                }

            }
            log.info("发送音频包...");
            //Thread.sleep(40);
        }
        client.end();
        log.info("音频发送完毕！");
        //服务器端需要根据业务情况调整，主线程需要等待收到转写结束标志才能结束
        List<String> resultJsonsStr = null;
        Boolean falg = new Boolean(true);
        /*while (falg) {
            log.info("获取转写结果！");
            resultJsonsStr = IstClientUtil.getResultMap().get(deviceId);
            if (null == resultJsonsStr || resultJsonsStr.size() <= 0) {
                Thread.sleep(1000 * 10);
            } else {
                falg = false;
            }
        }
        for (int i = 1; i <= resultJsonsStr.size(); i++) {
            String str = resultJsonsStr.get(i - 1);
            log.info(formatSentence(str));
        }*/
        synchronized (TestAudio.class){
            TestAudio.class.wait();
        }
        client.closeClient();


    }

    public static void notifyMethod() {
        synchronized (TestAudio.class) {
            TestAudio.class.notifyAll();
        }
    }


    public void audio8kTo16K() throws Exception {
        long start = System.currentTimeMillis();
        File audio = new File(audioFile);

        File outFile = new File("/Users/jifeng/Downloads/pcmMusic/8k_20190106161334537_0111111.wav");
        FileOutputStream out = new FileOutputStream(outFile);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(audio));
        byte[] bits = new byte[1280];
        int len = 0;

        byte[] new_byte;
        while ((len = bis.read(bits)) != -1) {
            new_byte = ResampleUtil.convert8kTo16k(bits);
            out.write(new_byte, 0, len);
        }

        out.flush();
        long end = System.currentTimeMillis() - start;
        System.out.println(end);
        bis.close();
        out.close();

    }

    /**
     * 转写结果格式化
     *
     * @param json
     * @return
     */
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
