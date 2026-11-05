package com.zh.bean;

public class IPInfo {
    private String ip;
    private String netmask;

    public IPInfo(String ip, String netmask) {
        this.ip = ip;
        this.netmask = netmask;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getNetmask() {
        return netmask;
    }

    public void setNetmask(String netmask) {
        this.netmask = netmask;
    }

    @Override
    public String toString() {
        return "ip=" + ip + ", netmask=" + netmask;
    }
}
