# Network Route Tool

双网卡路由设置工具：内网流量走内网网卡，外网流量走外网网卡，不用来回切换网络。完整的使用说明见 [docs/usage.md](../../docs/usage.md)。

## 快速开始

### 前置条件

- **操作系统**：Windows 或 macOS，中文或英文环境均可；Linux 暂不支持。
- **Java**：Java 8 及以上。只运行发布包需要 JRE；从源码构建需要 JDK（含 `javac`）。
- **依赖**：仓库未包含 log4j，从源码构建前需要准备 `log4j-1.2.x.jar`（示例使用 1.2.17）。
- **权限**：修改路由需要管理员权限。Windows 请以管理员身份启动命令行；macOS 请用 `sudo` 启动。
- **网卡**：至少两张已启用的网卡（一内一外），并且都能获取到非 `0.0.0.0` 的默认网关。
- **白名单（可选）**：`WhiteList.config` 放在启动程序时的工作目录（不是 jar 所在目录），每行一条 `IP 子网掩码`，以 `#` 开头的行是注释。

### 从源码构建并运行（示例）

在仓库根目录执行：

**macOS**

```bash
cd SuperAgent/SuperAgentTest

# 1. 下载依赖（仓库不包含 log4j）
mkdir -p lib build/classes
curl -fL -o lib/log4j-1.2.17.jar \
  https://repo1.maven.org/maven2/log4j/log4j/1.2.17/log4j-1.2.17.jar

# 2. 编译（需要 JDK 8+）
javac -encoding UTF-8 -cp lib/log4j-1.2.17.jar \
  -d build/classes $(find src/main/java -name '*.java')

# 3. 加入日志配置
cp src/main/resources/log4j.properties build/classes/

# 4. 以管理员权限启动（macOS 必须用 sudo）
sudo java -cp "build/classes:lib/log4j-1.2.17.jar" com.zh.Run
```

**Windows（以管理员身份打开命令提示符）**

```bat
cd SuperAgent\SuperAgentTest
mkdir lib build\classes
curl -fL -o lib\log4j-1.2.17.jar https://repo1.maven.org/maven2/log4j/log4j/1.2.17/log4j-1.2.17.jar
dir /s /b src\main\java\*.java > sources.txt
javac -encoding UTF-8 -cp lib\log4j-1.2.17.jar -d build\classes @sources.txt
copy src\main\resources\log4j.properties build\classes\
java -cp "build\classes;lib\log4j-1.2.17.jar" com.zh.Run
```

> 仓库中没有发布包；上游 NetworkRouteTool 的 Releases 只提供 Windows exe（v1.2–v1.4）。macOS 或需要使用本仓库源码时，请按上面的步骤从源码构建。没有 `curl` 时，可用浏览器打开依赖链接下载。

启动后按以下顺序操作：

1. 在内网、外网下拉框中各选一张网卡，确认两个网关都不是 `0.0.0.0`。
2. 如需白名单，在“内网白名单”文本框中按 `IP 子网掩码` 填写并点击“保存白名单”；不需要可留空。
3. 点击“执行路由”。路由命令需要管理员权限，权限不足时结果区会给出提示。
4. 需要恢复路由时，拔插对应网线或重启系统。

## 更新记录

#### v1.5

​		1、适配MAC OS。

​		2、BUG修复。



#### v1.4

​		1、UI进行改进。

​		2、重新优化刷新网卡机制。

​		3、BUG修复。



#### v1.3

​		1、增加日志记录。

​		2、新增刷新重新获取网关机制。

​		3、新增自动写入白名单功能。



#### v1.2

​		1、修复有几率无法收到内网广播的BUG。



#### v1.1

​		1、新增白名单模式。

​		2、修复路由控制不够精细问题。

​		3、强制管理员权限运行。



#### V1.0

​		1、实现双网口同时使用。



## 常见问题

#### 	软件运行环境？

​			本软件支持在Windows/MAC OS操作系统运行，中文或英文环境下使用，需jre或jdk支持；从源码构建时需要 JDK 8 及以上版本和 log4j 1.2.x。



#### 	No JVM could be found on your system.Please define EXE4j_JAVA_HOME to point to an installed 64-bit/32-bit JDK or JRE or download a JRE from www.java.com.？

​			本机没有jre或jdk，www.java.com下载jre安装即可，也可使用网盘jre文件夹下的jre-8u231-windows-i586.exe文件安装。



#### 	网卡的默认网关为什么是0.0.0.0？

​			1、IP为手动配置，而没有配置默认网关，进入对应网卡进行设置即可。

​			2、IP为DHCP获取，网线刚刚插上，仅获取到了IP，网关还没来得及获取，等待15秒到1分钟后刷新即可获取。



#### 	为什么执行之后提示 “请求的操作需要提升”或“The requested operation requires elevation”？

​		以管理员权限执行软件即可。



#### 	如何恢复网卡路由至原来的状态？

​		只需拔下需要重置网卡上的网线再插上即可自动恢复，但此时可能会存在多余路由，但不会影响正常使用，如需完全恢复，重启即能完全恢复成最初状态。



#### 	白名单如何设置？

​		配置文件命名：WhiteList.config。

​		文件存放位置：启动程序时的工作目录（不是 jar 所在目录），可在该目录新建或编辑。

​		如何打开：文本编辑器即可直接打开编辑。

​		配置内容：IP和子网掩码以空格隔开，多条名单内容则以回车隔开，#为单行注释。



## 反馈

如遇到其他BUG或问题，欢迎将问题描述和软件工具下存放路径下的info.log邮箱至zhouh1105@163.com进行反馈。