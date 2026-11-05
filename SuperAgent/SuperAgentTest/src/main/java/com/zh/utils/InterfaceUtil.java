package com.zh.utils;

import com.zh.bean.IPInfo;

import java.util.ArrayList;
import java.util.List;

public class InterfaceUtil {
    public static int getIndex(String string) {
        String[] inf = string.split("，");
        String[] index = inf[0].split("：");
        return Integer.parseInt(index[1]);
    }

    public static String getNetSegment(String ip, String netMask) {
        StringBuilder networkSegment = new StringBuilder();
        int[] ipArray = new int[4];
        int[] netMaskArray = new int[4];
        int[] netSegment = new int[4];
        if (4 != ip.split("\\.").length || "".equals(netMask)) {
            return "0.0.0.0";
        }
        for (int i = 0; i < 4; i++) {
            ipArray[i] = Integer.parseInt(ip.split("\\.")[i]);
            netMaskArray[i] = Integer.parseInt(netMask.split("\\.")[i]);
            if (ipArray[i] > 255 || ipArray[i] < 0 || netMaskArray[i] > 255 || netMaskArray[i] < 0) {
                return "0.0.0.0";
            }
            netSegment[i] = ipArray[i] & netMaskArray[i];
        }
        //构造网段
        for (int i = 0; i < netSegment.length; i++) {
            networkSegment.append(i == 3 ? netSegment[i] : netSegment[i] + ".");
        }
        return networkSegment.toString();
    }

    public static List<IPInfo> getIPInfo(String whiteListText){
        List<IPInfo> ipInfoList = new ArrayList<>();
        if (whiteListText.length() != 0) {
            String[] IPInfos = whiteListText.split("\n");
            for (String ipInfo : IPInfos) {
                String[] info = ipInfo.split(" ");
                ipInfoList.add(new IPInfo(info[0], info[1]));
            }
            return ipInfoList;
        } else {
            return null;
        }
    }

    public static String long2IP(Long IP) {
        int[] b = new int[4];
        b[0] = (int) ((IP >> 24) & 0xff);
        b[1] = (int) ((IP >> 16) & 0xff);
        b[2] = (int) ((IP >> 8) & 0xff);
        b[3] = (int) (IP & 0xff);
        return b[0] + "." + b[1] + "." + b[2] + "." + b[3];
    }

    public static boolean isIP(String string){
        String pattern = "((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}";
        return string.matches(pattern);
    }

    public static boolean isIPInfo(String string){
        String pattern = "((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}\\s((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}";
        return string.matches(pattern);
    }
}
