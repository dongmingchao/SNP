# Json Page -- SNP

1. Get Started

```bash
git clone https://github.com/dongmingchao/SNP
```

2. Example

run HttpNet.java and open http://127.0.0.1:9000

default page is index.html and you will see some simple test

http://127.0.0.1:9000/login.html is a cloud drive example for js and css transport test

2. Usage

Create a Server

```java
new Server([使用的端口号],[你的输入流],[你的输出流]).start();
```

Create a Client

```java
new Client([{String}服务器IP], [服务器端口号], [你的输入流], [你的输出流]).start();
```
3. Issue

- 原先点对点制作完成后，注释了原来的代码，想要改成多对一的
- ~~现状：服务器在收到第二个链接请求并建立后，原先对于第一个客户端的输入流就会崩溃~~ 已修复

4. In Future

- [ ] 方便的使用点对点
- [x] 方便的使用多对一

5. Project Structure

    Test Files
    - HttpNet.java => base HttpServer(SNP) test / Test Main
    - Net.java => mini server usage
    - NetClient.java => mini and cmd client usage
    
    Ignore Files, they are exist for past or future test
    - Test.java
    - Test2.java
    - TestSQL.java

    Project Files
    - HttpServer.java => first step of whole project
    - Request.java => parse what received, resolve http headers, collect vars
    - Response.java => send what we want, append http headers, unpack json script and vars
    - Reflex.java => parse json to a object and execute script and add vars to session scope
    - Script.java => script we already written, in a few words, build a bridge between json and java. you can put any extensions in here to bring java power in few words in json

    


5. HttpServer

**问题！！**

- json解析碰到*会解析出错，可能影响所有符号
