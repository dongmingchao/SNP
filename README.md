# 简化JAVA-Socket使用的一个接口

1. Get Started

直接下载源码即可，服务器是Server,java，客户端是Client.java

2. Usage

如果要建立一个服务器

```java
new Server([使用的端口号],[你的输入流],[你的输出流]).start();
```

如果要建立一个客户端

```java
new Client([{String}服务器IP], [服务器端口号], [你的输入流], [你的输出流]).start();
```
3. Issue

- 原先点对点制作完成后，注释了原来的代码，想要改成多对一的
- ~~现状：服务器在收到第二个链接请求并建立后，原先对于第一个客户端的输入流就会崩溃~~已修复

4. In Future

- [ ] 方便的使用点对点
- [x] 方便的使用多对一

5. HttpServer

**问题！！**

- json解析碰到*会解析出错，可能影响所有符号
