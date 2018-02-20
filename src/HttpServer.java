import javax.annotation.Resources;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class HttpServer extends Thread {

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
    Socket server;
    private ArrayList<PrintWriter> outputStreams;
    Request req;
    Response resp;

    public HttpServer(int port, InputStream input, OutputStream output) {
        inLocal = new Scanner(input);
        outLocal = new PrintWriter(output, true);//Attention: 不打开autoFlush不会输出
        this.port = port;
        this.send = input;
        this.message = output;
        this.outputStreams = new ArrayList<>();
    }

    public HttpServer(ServerSocket socket, InputStream input, OutputStream output) {
        this(socket.getLocalPort(), input, output);
        this.socket = socket;
    }

    ServerSocket startServer() {
        try {
            if (socket == null) {
                socket = new ServerSocket(port);
//                socket.setSoTimeout(10000);//设置超时时间
            }
            outLocal.println("等待有人链接 http://127.0.0.1:" + port + "/");
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

    String getTime_GMT() {
        Calendar cd = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT")); // 设置时区为GMT
        return sdf.format(cd.getTime());
    }

    /**
     * Attition:out.println("Content-Length: 122");说明Content-Length来发送POST请求
     * out.println("Connection: keep-alive");使用KeepAlive发送3次请求
     */

    @Override
    public void run() {
        online = true;
        socket = startServer();
        while (online) {
            try {
                server = socket.accept();
//                new Thread(sonServer).start();
//                while(in.hasNextLine()) System.out.println(in.nextLine());
                req = new Request(server.getInputStream());
                resp = new Response(server.getOutputStream(), this);
                resp.file(route(req.url));
                come.accept(req, resp);
                resp.end();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        inLocal.close();
        outLocal.close();
    }

    public BiConsumer<Request, Response> come;
    public HashMap<String, Object> route;
    public HashMap<String, Object> mime;
    private String routeFile;
    private FileTime watchRoute;

    /**
     * MIME类型获取
     * @param suffix
     * @return
     */

    String getMIME(String suffix){
        if (mime==null) {
            mime = json(getClass().getResourceAsStream("mime.json"));
            return getMIME(suffix);
        }else {
            Object res = mime.get(suffix);
            if (res==null) return "text/plain";
            else return res.toString();
        }
    }

    public void setRouteFile(String routeFile) {
        try {
            watchRoute = Files.getLastModifiedTime(Paths.get(routeFile));
            this.routeFile = routeFile;
            route = json(routeFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Path route(String url) {
        try {
            if (!watchRoute.equals(Files.getLastModifiedTime(Paths.get(routeFile))))
                route = json(routeFile);
        } catch (IOException e) {
            System.out.println("配置文件" + routeFile + "出错");
            e.printStackTrace();
        }
//            if (url==null) return null;
//            String[] file = url.split("\\.");
//            final String[] dir = new String[1];
//            if (file.length > 1)
//                dir[0] = file[file.length - 1];
//            for (String s : route.keySet()) {
//                System.out.println("route中的路径："+s);
//                if (s.equals(url)) return Paths.get(route.get(s).toString());
//                else if (s.equals("[" + dir[0] + "]")) {
//                    String type = (String) route.get(s);
//                    try {
//                        if (type.endsWith("*")) {
//                            final String typeDir = type.split("/\\*")[0];
//                            return Files.walk(Paths.get(typeDir)).filter(path -> path.toString().equals(typeDir + url)).findFirst().orElse(null);
//                        } else {
//                            String[] theFile = url.split("/");
//                            String the = theFile[theFile.length - 1];
//                            return Files.find(Paths.get(type), 1, ((path, basicFileAttributes) -> path.toString().endsWith("." + dir[0])))
//                                    .filter(path -> path.toString().equals(type + "/" + the)).findFirst().orElse(null);
//                        }
//                    } catch (IOException e) {
//                        System.out.println("路由文件配置出错：没有文件夹 " + route.get(s));
//                        e.printStackTrace();
//                    }
//                }else if (s.equals("[all]")) {
//                    String type = (String) route.get(s);
//                    final String typeDir = type.split("/\\*")[0];
//                    try {
//                        return Files.walk(Paths.get(typeDir)).filter(path -> path.toString().equals(typeDir + url)).findFirst().orElse(null);
//                    } catch (IOException e) {
//                        System.out.println("路由文件配置出错：没有文件夹 " + route.get(s));
//                        e.printStackTrace();
//                    }
//                }
//            }
        if (route != null) {
            if (url==null) return null;
            Object one = route.get(url);
            Path res = Paths.get("");
            if (one != null) res = Paths.get(one.toString());
            if (res.toFile().exists()) return res;
            Object all = route.get("[all]");
            if (all instanceof String){
                String parent = null;
                if (((String) all).endsWith("*")){
                    parent = ((String) all).split("/\\*")[0];
                }else parent = ((String) all).split("/")[0];
                System.out.println("all: "+parent);
                res = Paths.get(parent.concat(url));
            }
            if (res.toFile().exists()) return res;
            String[] the = url.split("\\.");
            System.out.println("url: " + url);
            System.out.println(route);
            Object routType = route.get("["+the[the.length - 1]+"]");
            if (routType instanceof String) {//each=> page/js/---.js | page/--.html ; url=> /js/--.js | /--.html
                String parent = null;
                if (((String) routType).endsWith("*")){
                    parent = ((String) routType).split("/\\*")[0];
                }else parent = ((String) routType).split("/")[0];
                System.out.println("routeType: "+parent);
                res = Paths.get(parent.concat(url));
            }
            if (res.toFile().exists()) return res;
            return routeAgain(url);
        } else {
            route = json(routeFile);
            if (route == null) return null;
            else {
                return route(url);
            }
        }
    }

    HashMap<String, LinkedList<Path>> routePath(HashMap<String, Object> got) {
        HashMap<String, LinkedList<Path>> res = new HashMap<>();
        got.forEach((key, value) -> {
            if (value != null) {
                LinkedList<Path> paths = new LinkedList<>();
                if (key.matches("/.*")) {
                    paths.add(Paths.get(value.toString()));
                    res.put(key, paths);
                } else if (key.equals("[all]")) {
                    try {
                        if (value.toString().endsWith("*")) {
                            String typeDir = value.toString().split("/\\*")[0];
                            paths.addAll(Files.walk(Paths.get(typeDir)).collect(Collectors.toList()));
                        } else {
                            paths.addAll(Files.walk(Paths.get(value.toString()), 1).collect(Collectors.toList()));
                        }
                    } catch (IOException e) {
                        System.out.println("路由文件配置出错：没有文件夹");
                        e.printStackTrace();
                    }
                    res.put(key.substring(1, key.length() - 1), paths);
                } else if (key.matches("\\[.+]")) {
                    try {
                        String type = key.substring(1, key.length() - 1);
                        if (value.toString().endsWith("*")) {
                            String typeDir = value.toString().split("/\\*")[0];
                            paths.addAll(Files.walk(Paths.get(typeDir)).filter(path -> path.getFileName().toString().endsWith("." + type)).collect(Collectors.toList()));
                        } else {
                            paths.addAll(Files.find(Paths.get(((String) value)), 1, ((path, basicFileAttributes) -> path.toString().endsWith("." + type))).collect(Collectors.toList()));
                        }
                    } catch (IOException e) {
                        System.out.println("路由文件配置出错：没有文件夹 ");
                        e.printStackTrace();
                    }
                    res.put(key.substring(1, key.length() - 1), paths);
                }
            }
        });
        return res;
    }

    private Path routeAgain(String url) {
        HashMap<String, Object> refresh = json(routeFile);
        if (route.equals(refresh)) return null;
        else {
            route = refresh;
            return route(url);
        }
    }

    private static HashMap<String, Object> json(String[] para, int[] at) {
        HashMap<String, Object> res = new HashMap<>();
        int index = 0;
        while (at[0] <= para.length) {
            at[0]++;
            switch (para[at[0]]) {
                case "{":
                    if (index > 1)
                        res.putIfAbsent(para[at[0] - 2], json(para, at));
                    break;
                case "}":
                    return res;
                case ":":
                    if (para[at[0] + 2].equals("}"))
                        res.putIfAbsent(para[at[0] - 1], para[at[0] + 1]);
                    else if (!(para[at[0] + 1].equals("[") || para[at[0] + 1].equals("{")))
                        res.put(para[at[0] - 1], para[at[0] + 1]);
                    else res.put(para[at[0] - 1], null);
                    break;
                case ",":
                    break;
                case "[":
                    res.putIfAbsent(para[at[0] - 2], array(para, at));
                    break;
            }
            index++;
        }
        return res;
    }

    static HashMap<String, Object> json(String fileName) {
        if (fileName == null) return null;
        else return json(new File(fileName));
    }

    static HashMap<String, Object> json(InputStream in){
        return json(new Scanner(in));
    }

    static HashMap<String, Object> json(File file){
        try {
            return json(new Scanner(file));
        } catch (FileNotFoundException e) {
            System.out.println("文件未找到");
            e.printStackTrace();
            return null;
        }
    }

    static HashMap<String, Object> json(Scanner in) {
        in.useDelimiter("\"|\\s");
        ArrayList<String> got = new ArrayList<>();
        while (in.hasNext()) {
            String each = in.next();
            if (each.equals("")) continue;
            if (each.matches("\\W+")) {
                for (int i = 0; i < each.length(); i++)
                    got.add(String.valueOf(each.charAt(i)));
            } else got.add(each);
        }
        String[] convert = got.toArray(new String[0]);
        int[] start = {0};
        return json(convert, start);
    }

    static ArrayList array(String[] para, int[] at) {
        ArrayList res = new ArrayList();
        while (!para[at[0]].equals("]")) {
            at[0]++;
            switch (para[at[0]]) {
                case ",":
                    break;
                case "{":
                    at[0]++;
                    res.add(json(para, at));
                    break;
                case "[":
                    res.add(array(para, at));
                    break;
                case "]":
                    return res;
                default:
                    res.add(para[at[0]]);
            }
        }
        return res;
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
                out = new PrintWriter(trans.getOutputStream(), true);
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
