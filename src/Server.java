import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Scanner;

public class Server extends Thread {

    boolean online;
    PrintWriter out;
    Scanner in;
    PrintWriter outLocal;//本地输出接口
    Scanner inLocal;//本地输入接口
    public int port;
    private ServerSocket socket = null;
    String received;
    InputStream send;
    OutputStream message;
    private Socket server;
    private ArrayList<PrintWriter> outputStreams;

    public Server(int port, InputStream input, OutputStream output) {
        inLocal = new Scanner(input);
        outLocal = new PrintWriter(output, true);//Attention: 不打开autoFlush不会输出
        this.port = port;
        this.send = input;
        this.message = output;
        this.outputStreams = new ArrayList<>();
    }

    public Server(ServerSocket socket, InputStream input, OutputStream output) {
        this(socket.getLocalPort(), input, output);
        this.socket = socket;
    }

    ServerSocket startServer() {
        try {
            if (socket == null) {
                socket = new ServerSocket(port);
//                socket.setSoTimeout(10000);//设置超时时间
            }
            outLocal.println("等待有人链接" + port + "端口...");
            return socket;
        } catch (SocketTimeoutException e) {
            outLocal.println("太久没有人链接，已退出");
            online = false;
            try {
                socket.close();
            } catch (IOException e1) {
                System.out.println("端口解除占用出错");
                e1.printStackTrace();
            }
            System.exit(0);
            return null;
        } catch (IOException e) {
            outLocal.println("网络IO出现问题");
            online = false;
            e.printStackTrace();
            return null;
        }
    }

    boolean initStream(Socket s) {
        if (!online) return false;
        if (s == null) {
            outLocal.println("未能正确建立链接");
            return false;
        }
        if (s.isClosed()) {
            outLocal.println("链接已断开");
            return false;
        }
        try {
            in = new Scanner(s.getInputStream());
            out = new PrintWriter(s.getOutputStream(), true);
            return true;
        } catch (IOException e) {
            outLocal.println("建立通信流出错");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void run() {
        online = true;
        socket = startServer();
        new Thread(hand).start();
        while (online) {
            try {
                server = socket.accept();
                new Thread(sonServer).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            if (initStream(server)) {
//                outLocal.println("已连接" + server.getRemoteSocketAddress());
//                while (in.hasNextLine()) {
//                    received = in.nextLine();
//                    if (received.equals("bye")) {
//                        out.println("bye");
//                        if (quitServer(server)) {
//                            outLocal.println("一个客户端安全退出");
//                        } else {
//                            outLocal.println("一个客户端异常退出");
//                        }
//                        break;
//                    } else outLocal.println(received);
//                }
//            }
        }
        inLocal.close();
        outLocal.close();
    }

    boolean quitServer(Socket server) {
        if (!online) return false;
        if (server == null) return true;
        try {
            in.close();
            out.close();
            server.close();
            return true;
        } catch (IOException e) {
            outLocal.println("断开链接出错");
            e.printStackTrace();
            return false;
        }
    }

    private Runnable hand = new Runnable() {

        @Override
        public void run() {
            System.out.println("一个输出线程启动");
//            PrintWriter outStream = new PrintWriter(server.getOutputStream(), true);
//            if (inLocal.hasNextLine()) {//注意：也会阻塞
            while (online) {
                if (inLocal.hasNextLine()) {
                    String got = inLocal.nextLine();
                    outputStreams.forEach(each -> each.println(got));
                    if (got.equals("bye")) break;
                }
            }
            System.out.println("一个输出线程关闭");
        }
    };

    private Runnable sonServer = new Runnable() {
        @Override
        public void run() {
            Socket trans = server;
            Scanner in = null;
            PrintWriter out = null;
            try {
                in = new Scanner(trans.getInputStream());
                out = new PrintWriter(trans.getOutputStream(),true);
                outputStreams.add(out);
                outLocal.println("已连接" + trans.getRemoteSocketAddress());
                while (in.hasNextLine()) {
                    received = in.nextLine();
                    if (received.equals("bye")) {
                        out.println("bye");
                        try {
                            outputStreams.remove(out);
                            trans.close();
                        } catch (IOException e) {
                            System.out.println("断开链接出错");
                            e.printStackTrace();
                        }
                        break;
                    } else outLocal.println(received);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("一个客户端断开链接");
        }
    };
}
