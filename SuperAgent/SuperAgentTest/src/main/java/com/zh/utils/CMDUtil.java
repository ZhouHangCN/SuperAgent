package com.zh.utils;


import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class CMDUtil {
    public static String ExeCMD(String cmd) {
        StringBuilder sb =new StringBuilder();
        try {
            ArrayList<String> commands = new ArrayList<>(Arrays.asList(cmd.split(" ")));

            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.redirectErrorStream(true);
            //启动进程
            Process p = pb.start();

            BufferedReader is = new BufferedReader(new InputStreamReader(p.getInputStream(),"gbk"));
            String line;
            while ((line = is.readLine()) != null) {
                if (line.toLowerCase().startsWith("warning")) {
                    sb.append("\nWARNING: ").append(line);
                } else if (line.toLowerCase().startsWith("error")) {
                    sb.append("\nERROR: ").append(line);
                } else if (line.toLowerCase().startsWith("fatal")) {
                    sb.append("\nFATAL ERROR: ").append(line);
                } else {
                    sb.append("\n").append(line);
                }
            }
            try {
                p.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
