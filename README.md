# zhao-Websocket
该项目是一个基于 Spring Boot 和 JavaScript 的轻量级快速开发 WebSocket 协议通讯的工具包。
## 简介
本项目提供了一组用于构建 WebSocket 通讯应用的 API和SDK，它们简单易用、轻量快速。通过使用本工具包，开发人员可以方便地构建具有 WebSocket 功能的应用程序。

## 要求

- springboot版本3.0及以上，jdk版本17及以上（自动装配文件与springboot2.0不同，如有需要可以自行修改）


## 快速开始
Server端：
- 将websocket-Server/jar中的jar包导入至项目中，分为两个版本
  WebsocketServer-WebStarterIncluded：内含spring-boot-starter-web依赖，无需spring-boot-starter-web依赖即可运行
  WebsocketServer-WithoutWebStarter：无spring-boot-starter-web依赖，需要导入spring-boot-starter-web依赖才能运行
- 添加依赖：
  在项目目录下新建libs文件夹并放入jar包，配置依赖：

- gradle：
```sh
implementation fileTree(dir: 'libs', includes: ['WebsocketServer.jar'])
```
- meavn

```sh
<dependency>
    <groupId>zhao</groupId>
    <artifactId>zhao</artifactId>
    <version>1.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/lib/WebsocketServer.jar</systemPath>
</dependency>
```
Borwser端：
- 将websocket-Browser/websocketBrowser.js文件保存至前端项目中

使用方法详见demo

## 功能
Server端：
- @WsController注解标注controller层
- @WsMapping注解标注controller层方法
- 监听websocket中close、error、open事件只需要实现WsEvent接口
- 监听请求只需要实现WsAuthentication接口
- 自动回应心跳包
- 可以根据参数及参数名自动注入方法中
- WsUtil中封装通知、下线等方法
- 可同时处理字符数据以及字节数据

Browser端：
- newSocket建立与服务器连接，连接后自动发送心跳包，非正常断开自动重连
- setInit注册连接断开后执行方法
- addNoticeFunction注册通知执行方法
- socket发送请求，字符数据以及字节数据可同时发送，自动分片
- setWebsocketPath设置连接路径
- getCloseType获取连接断开类型，客户端登出或服务端踢出

##开发者信息
- 作者：瞾彧滉
- 邮箱：zwh1350253335@gmail.com
- git小白一个，希望自己做的工具能帮助到更多的人，欢迎大家修改与指正

## 更新日志
0.1.0
- websocketBrowser.js还可以进行优化，目前只可以连接单个服务器，可以改为直接按照路径进行访问，就像axios一样，后端SDK还有很多可修改参数没有暴露给开发者。但是能用。先忙毕业的事情。


