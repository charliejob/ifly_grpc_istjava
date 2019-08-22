package com.iflytek.vcp.voice.engine.ist.client;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * 音频重采样
 * Created by dfhuang@iflytek.com on 2018/12/18.
 */
public class ResampleUtil {

    /** 采样点转换
     * 8k16bit -->16k16bit
     * 策略1：将采样点复制一份
     * @param orig
     * @return
     */
    public static byte[] convert8kTo16k(byte[] orig) {
        byte[] dest = new byte[] {};
        for (int j = 0; j < orig.length; j = j + 2) {
            byte[] byte2 = new byte[2];
            byte2[1] = orig[j + 1];
            byte2[0] = orig[j];
            dest = append(dest, byte2);
            dest = append(dest, byte2);
        }
        return dest;
    }

    /**
     * 取两个采样点中间值添加到音频数据，增加转换后效果
     * 8k16bit->16k16bit
     * @param orig
     * @return
     */
    public static byte[] convert8000To16000(byte[] orig){
        byte[] dest = new byte[]{};
        for (int j = 0; j < orig.length; j = j + 2) {
            byte[] byte1 = new byte[2];
            byte1[1] = orig[j + 1];
            byte1[0] = orig[j];

            dest = append(dest, byte1);
            if (j+2>=orig.length){
                dest = append(dest,byte1);
            }else {
                short sample = bytesToShort(byte1);
                byte[] byte2 = new byte[2];
                byte2[0] = orig[j+2];
                byte2[1] = orig[j+3];
                short sample1 = bytesToShort(byte2);
                short sample2 = (short) ((sample+sample1)/2);
                byte[] byte3= toByte(sample2);
                dest = append(dest, byte3);
            }
        }
        return dest;
    }


    /**
     * 采样精度转换
     * 采样精度8bit 可以理解为一个byte 描述了一个采样点，而采样精度为16bit 则可以理解16bit即2个byte 描述一个采样点
     8bit -->16bit
     * @param orig
     * @return
     */
    public static byte[] convert8bitTo16bit(byte[] orig){
        byte[] dest = new byte[]{};
        for (int i=0;i<orig.length;i++){
            // 转无符号整数
//      int sample = orig[i] & 0xff;
//      sample = sample - 128;
//      int s1 = (int) (sample * 1.0 / 256 * Short.MAX_VALUE);
            int s1 = (orig[i]+128)<<8;
            byte[] byte2 = new byte[2];
            byte2[1] = (byte) ((s1 << 16) >> 24);
            byte2[0] = (byte) ((s1 << 24) >> 24);
            dest = append(dest, byte2);
        }
        return dest;
    }

    /**
     * 拼接8k8bit byte[] 转换成16k6bit byte[]
     *
     * @param orig 原始byte[]
     */
    public static byte[] convertTo16k16Bit(byte[] orig) {
        byte[] dest = new byte[] {};
        for (int j = 0; j < orig.length; j++) {
            // 转无符号整数
            int sample = orig[j] & 0xff;
            // 转成正负值
            sample = sample - 128;

            // 等比缩放，转化成16bit
            int s1 = (int) (sample * 1.0 / 256 * Short.MAX_VALUE);
            byte[] byte2 = new byte[2];
            byte2[1] = (byte) ((s1 << 16) >> 24);
            byte2[0] = (byte) ((s1 << 24) >> 24);

            // TODO 采样点 8k->16k,复制一个采样点，可使用其他算法实现（统计学公式，计算趋势）
//      dest = append(dest, byte2);
//      dest = append(dest, byte2);

            dest = append(dest, byte2);
            // TODO 采样点 8k->16k
            int sample2 = (orig[j + 2 > orig.length ? j : j + 1] & 0xff) - 128;
            int s2 = (int) ((sample2 * 1.0 / 256 * Short.MAX_VALUE) + s1) / 2;
            byte2 = new byte[2];
            byte2[1] = (byte) ((s2 << 16) >> 24);
            byte2[0] = (byte) ((s2 << 24) >> 24);
            dest = append(dest, byte2);
        }
        return dest;
    }





    /**
     * short->byte
     * @param s
     * @return
     */
    public static byte[] toByte(short s){
        byte[] byte2 = new byte[2];
        byte2[1] = (byte) ((s << 16) >> 24);
        byte2[0] = (byte) ((s << 24) >> 24);
        return byte2;
    }


    /**
     * 拼接byte[]
     *
     * @param orig 原始byte[]
     * @param dest 需要拼接的数据
     * @return byte[]
     */
    public static byte[] append(byte[] orig, byte[] dest) {

        byte[] newByte = new byte[orig.length + dest.length];

        System.arraycopy(orig, 0, newByte, 0, orig.length);
        System.arraycopy(dest, 0, newByte, orig.length, dest.length);

        return newByte;

    }

    public static short bytesToShort(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    public static void main(String[] args) {
        byte[] bytes = new byte[2];
        bytes[0] =1;
        bytes[1] = 2;
        short temp = bytesToShort(bytes);
        System.out.println(temp);
    }
}
