# 彩票销售抽奖系统

## 系统概述
这是一个模拟福利彩票36选7的抽奖系统，具有用户注册、彩票购买、抽奖和中奖通知功能。

## 功能特性
1. 用户注册登录
2. 彩票购买（手动选号/随机选号）
3. 抽奖功能
4. 中奖通知
5. Excel数据存储

## 环境要求
- JDK 8+
- Apache POI库（操作Excel）

## 项目结构
lottery-system/
├── README.md
├── pom.xml
├── src/
│ ├── main/java/lottery/
│ │ ├── Main.java
│ │ ├── model/ # 数据模型
│ │ ├── dao/ # 数据访问层
│ │ ├── service/ # 业务逻辑层
│ │ └── ui/ # 用户界面层
│ └── resources/
├── data/ # Excel数据文件
└── bin/ # 启动脚本


## 快速开始
1. 编译项目：`javac -cp "lib/*" src/main/java/lottery/*.java`
2. 运行系统：`java -cp ".;lib/*" lottery.Main`
3. 访问地址：http://localhost:8080

## Excel数据存储
- users.xlsx：用户数据
- tickets.xlsx：彩票数据
- results.xlsx：开奖结果

## 自动测试
运行 `AutoRegisterTest.java` 可以自动注册10万用户并购买彩票进行测试。

## 打包发布
1. 编译打包：`mvn clean package`
2. 运行jar包：`java -jar lottery-system.jar`

## 注意事项
1. 确保data目录有写入权限
2. 首次运行会自动创建Excel文件
3. 抽奖号码范围：01-36