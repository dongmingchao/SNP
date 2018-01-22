public class HttpNet {
    public static void main(String[] args) {
        HttpServer http = new HttpServer(9000,System.in,System.out);
        http.setRouteFile("route.json");
        http.start();
        http.come = (req, resp) -> {
            System.out.println("方法"+req.method);
            System.out.println("请求路径"+req.url);
            System.out.println("协议"+req.protocol);
            System.out.println("GET请求参数"+req.param_GET);
        };
    }
}
