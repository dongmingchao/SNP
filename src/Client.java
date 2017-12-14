import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client extends Thread{
    Scanner in;
    PrintWriter out;
    public String serverName;
    public int port;
    PrintWriter outLocal;//本地输出接口
    Scanner inLocal;//本地输入接口
    boolean online;
    InputStream send;
    OutputStream message;

    public Client(String serverName, int port, InputStream send, OutputStream message) {
        this.serverName = serverName;
        this.port = port;
        this.send = send;
        this.message = message;
        inLocal = new Scanner(send);
        outLocal = new PrintWriter(message,true);
    }

    Socket connect(){
        try {
            return new Socket(serverName,port);
        } catch (IOException e) {
            outLocal.println("链接失败");
            e.printStackTrace();
            return null;
        }
    }

    boolean initStream(Socket s){
        try {
            in = new Scanner(s.getInputStream());
            out = new PrintWriter(s.getOutputStream(),true);
            return true;
        } catch (IOException e) {
            System.out.println("建立通信渠道失败");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void run() {
        online = true;
        Socket client = connect();
        if (initStream(client)){
            outLocal.println("已链接"+client.getRemoteSocketAddress());
            new Thread(hand).start();
            while(in.hasNextLine()){//这里阻塞
                String got = in.nextLine();
                outLocal.println(got);
                if (got.equals("bye")) {
                    if (closeClient(client)) {
                        System.exit(0);
                    }else {
                        System.exit(1);
                    }
                }
            }
        }
    }

    boolean closeClient(Socket client){
        try {
            in.close();
            out.close();
            client.close();
            online = false;
            send.close();
            message.close();
            inLocal.close();
            outLocal.close();
            return true;
        } catch (IOException e) {
            outLocal.println("关闭客户端出错");
            e.printStackTrace();
            return false;
        }
    }

    private Runnable hand = new Runnable() {
        @Override
        public void run() {
            while (online){
                if (inLocal.hasNextLine()){
                    out.println(inLocal.nextLine());
                }
            }
        }
    };
}
