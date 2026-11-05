package com.zh.ui;

import com.zh.bean.IPInfo;
import com.zh.bean.Interface;
import com.zh.utils.CMDUtil;
import com.zh.utils.ConfigUtil;
import com.zh.utils.InterfaceUtil;
import com.zh.utils.SystemInfoUtil;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//继承JFrame顶层容器类
public class MainUI extends JFrame {
    private static final Logger logger = Logger.getLogger(MainUI.class);
    //定义组件
    //定义面板
    JPanel panelResult, panelName, panelInterface, panelButton, panelWhiteList;
    //定义标签
    JLabel labelInside, labelOutside, labelWhiteList;
    //定义下拉框
    JComboBox<Object> comboBoxInside;
    JComboBox<Object> comboBoxOutside;
    //定义按钮
    JButton buttonAbout, buttonWhiteList, buttonExe, buttonRenew;
    //分割
    JSplitPane splitPaneResult, splitPaneWhiteList, splitPaneInfo;
    JTextArea textAreaWhiteList, textAreaResult;
    JScrollPane scrollPaneWhiteList, scrollPaneResult;
    int insideIndex;
    int outsideIndex;
    String insideGateway, outsideGateway, insideNetmask, insideNetSegment;
    //是否查询到网络接口
    boolean flag = false;
    List<Interface> interfaceList;
    String[] inside;
    String[] outside;
    List<Interface> interfaces = new ArrayList<>();


    public MainUI() {
        ItemListener insideListener = arg0 -> {
            if (flag) {
                if (ItemEvent.SELECTED == arg0.getStateChange()) {
                    String selectedItem = arg0.getItem().toString();
                    insideIndex = InterfaceUtil.getIndex(selectedItem);
                    for (Interface anInterface : interfaceList) {
                        if (anInterface.getIndex() == insideIndex)
                            insideGateway = anInterface.getGateway();
                    }
                    int selectIndex = InterfaceUtil.getIndex(selectedItem);
                    for (Interface inf : interfaceList) {
                        if (inf.getIndex() == selectIndex) {
                            insideNetmask = inf.getNetmask();
                            insideNetSegment = InterfaceUtil.getNetSegment(inf.getIp(), insideNetmask);
                        }
                    }
                }
            }
        };
        ItemListener outsideListener = arg1 -> {
            if (flag) {
                if (ItemEvent.SELECTED == arg1.getStateChange()) {
                    String selectedItem = arg1.getItem().toString();
                    outsideIndex = InterfaceUtil.getIndex(selectedItem);
                    for (Interface anInterface : interfaceList) {
                        if (anInterface.getIndex() == outsideIndex)
                            outsideGateway = anInterface.getGateway();
                    }
                }
            }
        };

        //执行路由
        ActionListener exeActionListener = arg2 -> {
            //进行逻辑处理即可
            String result0 = "";
            if (flag) {
                textAreaResult.setDisabledTextColor(Color.RED);
                if (insideGateway.equals("0.0.0.0")) {
                    textAreaResult.setText("未获取到内网网关信息，无法执行，请尝试刷新，如还结果还是如此，请检查网卡或网线是否正常。");
                } else if (outsideGateway.equals("0.0.0.0")) {
                    textAreaResult.setText("未获取到外网网关信息，无法执行，请尝试刷新，如还结果还是如此，请检查网卡或网线是否正常。");
                } else if (interfaceList.size() < 2) {
                    textAreaResult.setText("当前只检测到有一张网卡，无法执行，请尝试刷新，如还结果还是如此，请检查网卡或网线是否正常。");
                } else if (insideIndex == outsideIndex) {
                    textAreaResult.setText("内外网为同一张网卡，无法执行，请检查网卡配置。");
                } else if (insideNetSegment.equals("0.0.0.0")) {
                    textAreaResult.setText("无法获取到内网网段，请尝试刷新，如还结果还是如此，请检查网卡配置。");
                } else {
                    textAreaResult.setDisabledTextColor(Color.BLACK);
                    String systemName = SystemInfoUtil.getSystemName();
                    String cmd1 = null;
                    String cmd2 = null;
                    String cmd3 = null;
                    if (systemName.contains("Mac OS")) {
                        cmd1 = "sudo route -nv delete -net 0.0.0.0 -gateway " + insideGateway;
                        cmd2 = "sudo route -nv add -net " + insideNetSegment + " -netmask " + insideNetmask + " -gateway " + insideGateway;
                        cmd3 = "sudo route -nv add -net 0.0.0.0 -netmask 0.0.0.0 -gateway " + outsideGateway;
                    } else if (systemName.contains("Windows")) {
                        cmd1 = "route delete 0.0.0.0 mask 0.0.0.0 " + insideGateway;
                        cmd2 = "route add " + insideNetSegment + " mask " + insideNetmask + " " + insideGateway + " if " + insideIndex + " metric " + 10;
                        cmd3 = "route change 0.0.0.0 mask 0.0.0.0 " + outsideGateway + " if " + outsideIndex + " metric " + 30;
                    }

                    assert cmd1 != null;
                    String result1 = CMDUtil.ExeCMD(cmd1);
                    String result2 = CMDUtil.ExeCMD(cmd2);
                    String result3 = CMDUtil.ExeCMD(cmd3);
                    textAreaResult.setDisabledTextColor(Color.BLACK);
                    textAreaResult.setText("删除内网默认路由：" + result1.trim() + "\n添加内网路由：" + result2.trim() + "\n改变外网默认路由：" + result3.trim());

                    String whiteListText = textAreaWhiteList.getText();
                    if (whiteListText.length() != 0) {
                        List<String> results = new ArrayList<>();
                        try {
                            //获取白名单列表
                            List<IPInfo> ipInfoList = InterfaceUtil.getIPInfo(whiteListText);
                            for (int i = 0; i < Objects.requireNonNull(ipInfoList).size(); i++) {
                                String ip = ipInfoList.get(i).getIp();
                                String netmask = ipInfoList.get(i).getNetmask();
                                //白名单格式没有问题则执行白名单
                                if (InterfaceUtil.isIP(ip) && InterfaceUtil.isIP(netmask)) {
                                    if (systemName.contains("Mac OS")) {
                                        results.add(CMDUtil.ExeCMD("sudo route -nv add -net " + ip + " -netmask " + netmask + " -gateway " + insideGateway));
                                    } else if (systemName.contains("Windows")) {
                                        results.add(CMDUtil.ExeCMD("route add " + ip + " mask " + netmask + " " + insideGateway + " if " + insideIndex + " metric " + 20));
                                    }
                                } else {
                                    textAreaResult.setDisabledTextColor(Color.RED);
                                    result0 = "白名单中第" + (i + 1) + "条记录中IP或子网掩码格式错误，请确保每个字段都在0~255之间。\n示例：“192.168.1.0 255.255.255.0”。";
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            logger.error("白名单格式错误", e);
                            textAreaResult.setDisabledTextColor(Color.RED);
                            result0 = "白名单格式错误！IP和子网掩码以空格隔开，多条名单内容则以回车隔开。\n示例：“192.168.1.0 255.255.255.0”。";
                        }
                        if (!result0.equals("")) {
                            textAreaResult.setDisabledTextColor(Color.BLACK);
                            textAreaResult.append("\n添加白名单路由：" + result0.trim());
                        } else {
                            textAreaResult.setDisabledTextColor(Color.BLACK);
                            for (int i = 0; i < results.size(); i++) {
                                logger.info("\n添加第" + (i + 1) + "条白名单路由：" + results.get(i).trim());
                                textAreaResult.append("\n添加第" + (i + 1) + "条白名单路由：" + results.get(i).trim());
                            }
                        }
                    }
//                    else {
//                        textAreaResult.setDisabledTextColor(Color.RED);
//                        logger.warn("未查询到网卡信息，无法执行！");
//                        textAreaResult.setText("未查询到网卡信息，无法执行！");
//                    }
                }
            }
        };

        ActionListener renewActionListener = arg4 -> init();

        ActionListener aboutActionListener = arg5 -> {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI("https://github.com/DreamChaser520/NetworkRouteTool/releases/"));
            } catch (Exception e) {
                logger.error("打开链接失败", e);
                e.printStackTrace();
            }

        };

        //保存白名单
        ActionListener whiteListActionListener = arg6 -> {
            String result = "";
            String whiteListText = textAreaWhiteList.getText();
            //白名单写入成功标记
            boolean whiteListSuccessFlag = false;
            if (whiteListText.length() != 0) {
                try {
                    //获取白名单列表
                    List<IPInfo> ipInfoList = InterfaceUtil.getIPInfo(whiteListText);
                    if (Objects.requireNonNull(ipInfoList).isEmpty()) {
                        result = "白名单记录中IP或子网掩码格式错误，请确保每个字段都在0~255之间。\n示例：“192.168.1.0 255.255.255.0”。";
                    } else {
                        for (int i = 0; i < Objects.requireNonNull(ipInfoList).size(); i++) {
                            String ip = ipInfoList.get(i).getIp();
                            String netmask = ipInfoList.get(i).getNetmask();
                            //白名单格式没有问题则写入白名单
                            if (InterfaceUtil.isIP(ip) && InterfaceUtil.isIP(netmask)) {
                                ConfigUtil.writeWhiteList(whiteListText);
                                whiteListSuccessFlag = true;
                            } else {
                                textAreaResult.setDisabledTextColor(Color.RED);
                                result = "白名单中第" + (i + 1) + "条记录中IP或子网掩码格式错误，请确保每个字段都在0~255之间。\n示例：“192.168.1.0 255.255.255.0”。";
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("白名单格式错误", e);
                    textAreaResult.setDisabledTextColor(Color.RED);
                    result = "白名单格式错误！IP和子网掩码以空格隔开，多条名单内容则以回车隔开。\n示例：“192.168.1.0 255.255.255.0”。";
                }

                if (!result.equals("")) {
                    textAreaResult.setDisabledTextColor(Color.BLACK);
                    textAreaResult.append("\n添加白名单路由：" + result.trim());
                }
            } else {
                ConfigUtil.writeWhiteList(whiteListText);
                whiteListSuccessFlag = true;
            }
            if (whiteListSuccessFlag) {
                textAreaResult.setDisabledTextColor(Color.BLACK);
                textAreaResult.append("\n更新白名单成功。");
            } else {
                textAreaResult.setDisabledTextColor(Color.RED);
                textAreaResult.append("\n更新白名单失败。");
            }
        };

        MouseListener textMouseListener = new MouseListener() {

            public void mouseReleased(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
                MainUI.super.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

            public void mouseEntered(MouseEvent e) {
                MainUI.super.setCursor(new Cursor(Cursor.TEXT_CURSOR));
            }

            public void mouseClicked(MouseEvent e) {
            }
        };

        MouseListener buttonMouseListener = new MouseListener() {

            public void mouseReleased(MouseEvent e) {
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
                MainUI.super.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

            public void mouseEntered(MouseEvent e) {
                MainUI.super.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            public void mouseClicked(MouseEvent e) {
            }
        };


        panelName = new JPanel();
        panelInterface = new JPanel();
        //创建内外网条目容器
        comboBoxInside = new JComboBox<>();
        comboBoxOutside = new JComboBox<>();
        //创建标签
        labelInside = new JLabel("内网网卡：");
        labelOutside = new JLabel("外网网卡：");
        //设置右对齐
        labelInside.setHorizontalAlignment(SwingConstants.RIGHT);
        labelOutside.setHorizontalAlignment(SwingConstants.RIGHT);
        //添加内网网卡面板
        panelName.add(labelInside);
        panelInterface.add(comboBoxInside);
        //添加外网网卡面板
        panelName.add(labelOutside);
        panelInterface.add(comboBoxOutside);

        comboBoxInside.addItemListener(insideListener);
        comboBoxOutside.addItemListener(outsideListener);

        comboBoxInside.addMouseListener(buttonMouseListener);
        comboBoxOutside.addMouseListener(buttonMouseListener);

        comboBoxInside.setRenderer(new CustomComboBoxRenderer());
        comboBoxOutside.setRenderer(new CustomComboBoxRenderer());

        panelName.setLayout(new GridLayout(2, 1, 10, 10));
        panelInterface.setLayout(new GridLayout(2, 1, 10, 10));

        //创建拆分窗格
        splitPaneInfo = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelName, panelInterface);
        splitPaneInfo.setDividerLocation(100);
        splitPaneInfo.setEnabled(false);
        splitPaneInfo.setBorder(new EmptyBorder(5, 10, 5, 10));

        //设置白名单面板
        panelWhiteList = new JPanel();
        labelWhiteList = new JLabel("内网白名单：");
        //设置为右对齐
        labelWhiteList.setHorizontalAlignment(SwingConstants.RIGHT);

        textAreaWhiteList = new JTextArea();
        scrollPaneWhiteList = new JScrollPane();
        textAreaWhiteList.addMouseListener(textMouseListener);

        //自动换行
        textAreaWhiteList.setLineWrap(true);
        //从第一个位置开始
        textAreaWhiteList.setCaretPosition(0);
        //创建滚动窗格
        scrollPaneWhiteList = new JScrollPane(textAreaWhiteList);

        splitPaneWhiteList = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, labelWhiteList, scrollPaneWhiteList);
        splitPaneWhiteList.setDividerLocation(100);
        splitPaneWhiteList.setEnabled(false);
        splitPaneWhiteList.setBorder(new EmptyBorder(5, 10, 5, 10));

        panelResult = new JPanel();
        panelButton = new JPanel();

        textAreaResult = new JTextArea();

        buttonAbout = new JButton("获取最新版本");
        buttonWhiteList = new JButton("保存白名单");
        buttonRenew = new JButton("更新网卡信息");
        buttonExe = new JButton("执行路由");

        buttonExe.setForeground(Color.BLUE);
        buttonAbout.addActionListener(aboutActionListener);
        buttonWhiteList.addActionListener(whiteListActionListener);
        buttonRenew.addActionListener(renewActionListener);
        buttonExe.addActionListener(exeActionListener);

        buttonAbout.addMouseListener(buttonMouseListener);
        buttonWhiteList.addMouseListener(buttonMouseListener);
        buttonRenew.addMouseListener(buttonMouseListener);
        buttonExe.addMouseListener(buttonMouseListener);
        textAreaResult.addMouseListener(textMouseListener);

        panelButton.setLayout(new GridLayout(2, 2, 5, 5));
        panelButton.setBorder(new EmptyBorder(5, 10, 5, 0));
        panelButton.add(buttonAbout);
        panelButton.add(buttonWhiteList);
        panelButton.add(buttonRenew);
        panelButton.add(buttonExe);

        textAreaResult.setEnabled(false);
        //自动换行
        textAreaResult.setLineWrap(true);
        //从第一个位置开始
        textAreaResult.setCaretPosition(0);
        //创建滚动窗格
        scrollPaneResult = new JScrollPane(textAreaResult);
        //创建拆分窗格
        splitPaneResult = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPaneResult, panelButton);
        splitPaneResult.setDividerLocation(400);
        splitPaneResult.setEnabled(false);
        splitPaneResult.setBorder(new EmptyBorder(5, 10, 5, 10));
        panelResult.add(splitPaneResult);


        //设置布局管理器
        //设置网格布局
        this.setLayout(new GridLayout(3, 1));

        //加入组件
        this.add(splitPaneInfo);
        this.add(splitPaneWhiteList);
        this.add(splitPaneResult);

        //设置窗体属性
        //设置界面标题
        this.setTitle("Network Route Tool v1.5 by 周航");
        //设置界面像素
        this.setSize(700, 250);
        //窗口居中
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        //设置虚拟机和界面一同关闭
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //设置界面可视化
        this.setVisible(true);

        init();
    }


    public void init() {
        textAreaResult.setDisabledTextColor(Color.BLACK);
        this.textAreaResult.setText("加载网卡信息中...");
        //接口列表
        interfaceList = InterfaceInfo.getInterfaces(interfaces);
        if (interfaceList != null) {
            int size = interfaceList.size();

            if (size == 0) {
                flag = false;
                inside = new String[1];
                outside = new String[1];
                inside[0] = "没有查询到网络接口";
                outside[0] = "没有查询到网络接口";
            } else {
                //查询到网络接口
                flag = true;
                inside = new String[size];
                outside = new String[size];
                for (int i = 0; i < interfaceList.size(); i++) {
                    //准备加载到GUI的数组
                    inside[i] = interfaceList.get(i).toString();
                    outside[i] = interfaceList.get(i).toString();
                }
                //不作任何选择时的初始值
                insideIndex = InterfaceUtil.getIndex(inside[0]);
                outsideIndex = InterfaceUtil.getIndex(outside[0]);

                insideGateway = interfaceList.get(0).getGateway();
                outsideGateway = interfaceList.get(0).getGateway();

                insideNetmask = interfaceList.get(0).getNetmask();
                insideNetSegment = InterfaceUtil.getNetSegment(interfaceList.get(0).getIp(), insideNetmask);
            }
            if (this.comboBoxInside != null) {
                this.comboBoxInside.removeAllItems();
            }
            if (this.comboBoxOutside != null) {
                this.comboBoxOutside.removeAllItems();
            }
            for (int i = 0; i < inside.length; i++) {
                assert this.comboBoxInside != null;
                assert this.comboBoxOutside != null;
                this.comboBoxInside.addItem(inside[i]);
                this.comboBoxOutside.addItem(outside[i]);
            }
            textAreaResult.setDisabledTextColor(Color.BLACK);
            this.textAreaResult.setText("加载网卡信息成功。");
        } else {
            textAreaResult.setDisabledTextColor(Color.RED);
            this.textAreaResult.setText("加载网卡信息失败。");
        }
        try {
            String whiteList = ConfigUtil.readWhiteList();
            if (whiteList.length() != 0) {
                if (whiteList.equals("error")) {
                    textAreaResult.setDisabledTextColor(Color.ORANGE);
                    this.textAreaResult.append("\n未找到白名单配置，请创建" + new File(".").getCanonicalPath() + File.separator + "WhiteList.config文件，如不需要白名单请忽略。");
                } else {
                    textAreaWhiteList.setText(whiteList);
                    this.textAreaResult.append("\n加载白名单配置信息成功。");
                }
            } else {
                textAreaResult.setDisabledTextColor(Color.ORANGE);
                this.textAreaResult.append("\n加载白名单配置信息成功，白名单配置为空，如此时的白名单面板中存在数据，则为未保存的临时数据，退出程序后将会自动清空。");
            }
        } catch (IOException e) {
            logger.error("加载白名单配置信息失败", e);
            textAreaResult.setDisabledTextColor(Color.RED);
            this.textAreaResult.append("\n加载白名单配置信息失败。");
        }
    }

    static class CustomComboBoxRenderer extends BasicComboBoxRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                if (-1 < index) {
                    list.setToolTipText((value == null) ? null : value.toString());
                }
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setFont(list.getFont());
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }
}
