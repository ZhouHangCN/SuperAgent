package com.zh.utils;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ConfigUtil {
    private static final Logger logger = Logger.getLogger(ConfigUtil.class);
    //已删除IP分区说明，便于直接打开配置文件时区分
    private static final String REMOVED_SECTION_HEADER = "# 已删除的IP（注释状态，取消注释即可重新启用）";
    //正在使用的IP分区说明
    private static final String IN_USE_SECTION_HEADER = "# 正在使用的IP（非注释状态）";

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
        //已删除的白名单记录，统一以注释形式放在文件最前面
        List<String> removedIPList = new ArrayList<>();
        //其它需要保留的旧行，例如手动添加的注释
        List<String> keptOldLines = new ArrayList<>();
        for (String oldLine : oldLines) {
            String oldIPInfo = oldLine.trim();
            if (oldIPInfo.length() == 0) {
                continue;
            }
            if (oldIPInfo.startsWith("#")) {
                if (oldIPInfo.equals(REMOVED_SECTION_HEADER)
                        || oldIPInfo.equals(IN_USE_SECTION_HEADER)) {
                    //分区说明在保存时重新生成，避免重复保留
                    continue;
                }
                String commentContent = oldIPInfo.substring(1).trim();
                if (InterfaceUtil.isIPInfo(commentContent)) {
                    //之前保存时注释掉的已删除记录，继续保留在最前面
                    String removedIPInfo = "#" + commentContent;
                    if (!removedIPList.contains(removedIPInfo)) {
                        removedIPList.add(removedIPInfo);
                    }
                } else {
                    keptOldLines.add(oldIPInfo);
                }
            } else if (InterfaceUtil.isIPInfo(oldIPInfo)) {
                if (!newIPList.contains(oldIPInfo)
                        && !removedIPList.contains("#" + oldIPInfo)) {
                    removedIPList.add("#" + oldIPInfo);
                }
            } else {
                keptOldLines.add(oldIPInfo);
            }
        }
        StringBuilder sb = new StringBuilder();
        //被删去的白名单记录以注释形式放在整个列表最前面，并用注释说明该分区
        if (!removedIPList.isEmpty()) {
            sb.append(REMOVED_SECTION_HEADER).append("\n");
            for (String removedIPInfo : removedIPList) {
                sb.append(removedIPInfo).append("\n");
            }
        }
        //正在使用的白名单条目按输入框中的顺序写入，并用注释说明该分区
        if (!newIPList.isEmpty()) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(IN_USE_SECTION_HEADER).append("\n");
            for (String newIPInfo : newIPList) {
                sb.append(newIPInfo).append("\n");
            }
        }
        //保留原有的手动注释
        if (!keptOldLines.isEmpty()) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            for (String oldLine : keptOldLines) {
                sb.append(oldLine).append("\n");
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
