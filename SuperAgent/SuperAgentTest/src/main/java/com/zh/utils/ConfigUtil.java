package com.zh.utils;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class ConfigUtil {
    private static final Logger logger = Logger.getLogger(ConfigUtil.class);

    public static String getPathname() {
//        String FilePath = SystemInfoUtil.getUserDir();
//        File directory = new File(".");
//        try {
//            FilePath = directory.getCanonicalPath();
//        } catch (IOException e) {
//            logger.error("获取白名单配置路径失败", e);
//        }
        return SystemInfoUtil.getUserDir() + File.separator + "WhiteList.config";
    }

    public static String readWhiteList() {
        File file = new File(getPathname());
        if (file.exists()) {
            StringBuilder sb = new StringBuilder();
            try (FileReader reader = new FileReader(getPathname());
                 BufferedReader br = new BufferedReader(reader)
            ) {
                String line;
                while ((line = br.readLine()) != null) {
                    // 一次读入一行数据
                    if (!line.startsWith("#") && line.length() > 0)
                        sb.append(line.trim()).append("\n");
                }
            } catch (Exception e) {
                logger.error("读取白名单配置失败",e);
                return "error";
            }
            return sb.toString();
        }
        logger.error("读取白名单配置失败，白名单不存在");
        return "error";
    }

    public static String writeWhiteList(String str) {
        //本地文件中的配置列表
        List<String> oldIPList = null;
        File file = new File(getPathname());
        if (file.exists()) {
            oldIPList = Arrays.asList(readWhiteList().split("\n"));
        }
        //List<String> oldIPList = new ArrayList(oldList);
        //临时写入的配置列表
        List<String> newIPList = Arrays.asList(str.split("\n"));
        //List<String> newIPList = new ArrayList(newList);

        File whiteListFile = new File(getPathname());
        File fileParent = whiteListFile.getParentFile();
        // 能创建多级目录
        if (!fileParent.exists()) {
            fileParent.mkdirs();
        }
        // 创建文件
        if (!whiteListFile.exists()) {
            try {
                whiteListFile.createNewFile();
            } catch (IOException e) {
                logger.error("创建白名单配置文件失败", e);
            }
        }
        //替换已经删除的ip信息
        StringBuilder sb = new StringBuilder();
        try (FileReader reader = new FileReader(getPathname());
             BufferedReader br = new BufferedReader(reader)
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                if (!newIPList.contains(line) && InterfaceUtil.isIPInfo(line)) {
                    sb.append("#").append(line.trim()).append("\n");
                } else {
                    sb.append(line.trim()).append("\n");
                }
            }
        } catch (Exception e) {
            logger.error("读取白名单配置失败", e);
            return "error";
        }
        //添加原本没有的ip信息
        for (String newIPInfo : newIPList) {
            if ((null == oldIPList || !oldIPList.contains(newIPInfo)) && InterfaceUtil.isIPInfo(newIPInfo)) {
                sb.append(newIPInfo).append("\n");
            }
        }
        //创建字符输出流
        Writer fw = null;
        try {
            fw = new FileWriter(getPathname(), false);
            fw.write(sb.toString());
        } catch (Exception e) {
            logger.error("写入白名单配置失败", e);
            return "error";
        } finally {
            try {
                fw.close();
            } catch (Exception e) {
                logger.error("关闭字节输出流失败", e);
                return "error";
            }
        }
        return "success";
    }
}
