public class NetClient {
    public static void main(String[] args) {
        String serverName = "127.0.0.1";
        int port = 9000;
        if (args.length > 1) serverName = args[0];
        if (args.length > 2) port = Integer.parseInt(args[1]);
        System.out.println(serverName + port);
        new Client(serverName, port, System.in, System.out).start();
    }
}
