package com.zh.utils;


public class SystemInfoUtil {
    public static String getSystemName() {
        return System.getProperty("os.name");
    }
    public static String getUserDir() {
        return System.getProperty("user.dir");
    }
}
