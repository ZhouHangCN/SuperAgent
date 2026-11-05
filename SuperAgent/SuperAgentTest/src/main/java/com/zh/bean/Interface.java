package com.zh.bean;

public class Interface {
    private int index;
    private String name;
    private String ip;
    private String gateway;
    private String netmask;


    public Interface(int index, String name, String ip, String gateway, String netmask) {
        this.index = index;
        this.name = name;
        this.ip = ip;
        this.gateway = gateway;
        this.netmask = netmask;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getNetmask() {
        return netmask;
    }

    public void setNetmask(String netmask) {
        this.netmask = netmask;
    }

    @Override
    public String toString() {
        return "ID：" + index + "， Name：" + name + "，IP：" + ip + "，Netmask：" + netmask +"，Gateway：" + gateway;
    }
}
