package com.zh.ui;

import com.zh.bean.Interface;
import com.zh.utils.CMDUtil;
import com.zh.utils.InterfaceUtil;
import com.zh.utils.SystemInfoUtil;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InterfaceInfo {
    private static final Logger logger = Logger.getLogger(InterfaceInfo.class);
    static String interfacesInfo = initInterFaceInfo();
    static String systemName = null;

    public static String initInterFaceInfo() {
        systemName = SystemInfoUtil.getSystemName();
        if (systemName.contains("Mac OS")) {
            String result = CMDUtil.ExeCMD("ifconfig").toUpperCase(Locale.ROOT);
            logger.info("系统名为：" + systemName + "，系统信息为：" + result);
            return result;
        } else if (systemName.contains("Windows")) {
            String result = CMDUtil.ExeCMD("ipconfig").toUpperCase(Locale.ROOT);
            logger.info("系统名为：" + systemName + "，系统信息为：" + result);
            return result;
        } else {
            logger.error("系统名为：" + systemName + "暂不支持此系统");
            return null;
        }
    }

    public static List<Interface> getInterfaces(List<Interface> interfaces) {
        if (!interfaces.isEmpty()) {
            interfaces.clear();
            interfacesInfo = initInterFaceInfo();
        }
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                //网卡名称
                String name = networkInterface.getDisplayName();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    //网卡索引
                    int index = networkInterface.getIndex();
                    //网卡ip地址
                    String ip = inetAddress.getHostAddress();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getHostAddress().indexOf(':') == -1) {
                        systemName = SystemInfoUtil.getSystemName();
                        if (systemName.contains("Mac OS")) {
                            String gateway = getParamByName(name);
                            String netmask = getParamByIP(ip, "NETMASK", "掩码");
                            Interface inf = new Interface(index, name, ip, gateway, netmask);
                            interfaces.add(inf);
                        } else if (systemName.contains("Windows")) {
                            String gateway = getParamByIP(ip, "GATEWAY", "网关");
                            String netmask = getParamByIP(ip, "MASK", "掩码");
                            Interface inf = new Interface(index, name, ip, gateway, netmask);
                            interfaces.add(inf);
                        }
                    }
                }
            }
            return interfaces;
        } catch (Exception e) {
            logger.error(e);
            return Collections.emptyList();
        }
    }

    private static String getParamByIP(String ip, String EnglishName, String ChineseName) {
        String[] infos = null;
        String pattern = null;
        systemName = SystemInfoUtil.getSystemName();

        if (systemName.contains("Mac OS")) {
            infos = interfacesInfo.split("\n\t");
            pattern = "[0][xX][0-9a-fA-F]{8}";
        } else if (systemName.contains("Windows")) {
            infos = interfacesInfo.split("\n\n");
            pattern = "((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}";
        }
//        String[] infos = interfacesInfo.split("\n\n");

        for (String inf : infos) {
            if (inf.contains(ip)) {
                String matcherStr = "";
                if (inf.contains(EnglishName)) {
                    matcherStr = inf.substring(inf.indexOf(EnglishName));
                } else if (inf.contains(ChineseName)) {
                    matcherStr = inf.substring(inf.indexOf(ChineseName));
                }
                Pattern r = Pattern.compile(pattern);
                Matcher matcher = r.matcher(matcherStr);
                if (matcher.find()) {
                    if (InterfaceUtil.isIP(matcher.group())) {
                        return matcher.group();
                    } else {
                        return InterfaceUtil.long2IP(Long.decode(matcher.group()));
                    }
                }
            }
        }
        return "0.0.0.0";
    }

    private static String getParamByName(String interfaceName) {
        String[] infos = null;
        systemName = SystemInfoUtil.getSystemName();
        if (systemName.contains("Mac OS")) {
            StringBuilder sb = new StringBuilder();
            sb.append("netstat -rn");
            String result = CMDUtil.ExeCMD(sb.toString());
            infos = result.split("\n");
        } else if (systemName.contains("Windows")) {
            infos = interfacesInfo.split("\n");
        }
        assert infos != null;
        for (String inf : infos) {

            if (inf.contains(interfaceName) && inf.contains("default")) {
                String pattern = "((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}";
                Pattern r = Pattern.compile(pattern);
                Matcher matcher = r.matcher(inf);
                if (matcher.find()) {
                    return matcher.group();
                }
            }
        }
        return "0.0.0.0";
    }
}
