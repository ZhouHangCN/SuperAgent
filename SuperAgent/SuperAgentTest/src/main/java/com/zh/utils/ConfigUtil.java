package com.zh.utils;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
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
        //输入框中的白名单，保持输入顺序
        List<String> newIPList = new ArrayList<>();
        if (str != null) {
            for (String line : str.split("\n")) {
                String newIPInfo = line.trim();
                if (InterfaceUtil.isIPInfo(newIPInfo)) {
                    newIPList.add(newIPInfo);
                }
            }
        }
        //本地文件中原有的行，用于保留注释和被移除的白名单记录
        List<String> oldLines = new ArrayList<>();
        File file = new File(getPathname());
        if (file.exists()) {
            try (FileReader reader = new FileReader(file);
                 BufferedReader br = new BufferedReader(reader)
            ) {
                String line;
                while ((line = br.readLine()) != null) {
                    oldLines.add(line);
                }
            } catch (Exception e) {
                logger.error("读取白名单配置失败", e);
                return "error";
            }
        }

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
        StringBuilder sb = new StringBuilder();
        //按输入框中的顺序写入白名单
        for (String newIPInfo : newIPList) {
            sb.append(newIPInfo).append("\n");
        }
        //保留原有注释，并把已删除的白名单记录注释后保留在末尾
        for (String oldLine : oldLines) {
            String oldIPInfo = oldLine.trim();
            if (oldIPInfo.length() == 0) {
                continue;
            }
            if (oldIPInfo.startsWith("#")) {
                sb.append(oldIPInfo).append("\n");
            } else if (InterfaceUtil.isIPInfo(oldIPInfo)) {
                if (!newIPList.contains(oldIPInfo)) {
                    sb.append("#").append(oldIPInfo).append("\n");
                }
            } else {
                sb.append(oldIPInfo).append("\n");
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
