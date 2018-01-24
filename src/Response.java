import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Response{
    PrintWriter raw;
    HttpServer server;
    StringBuilder datagram = new StringBuilder();
    Header header = new Header();
    File file;

    public Response(PrintWriter raw,HttpServer server) {
        this.raw = raw;
        this.server = server;
    }

    public void write(String text){
        datagram.append(text);
        datagram.append('\n');
    }

    public void write(StringBuilder text){
        datagram.append(text);
    }

    public void write(String text,String label){
        int lo = datagram.lastIndexOf(label);
        if (lo==-1) {
            datagram.append(text);
            datagram.append('\n');
        }else {
            datagram.insert(lo,text);
            datagram.insert(lo+text.length(),'\n');
        }
    }

    class Header{
        String protocol;
        String protocolVersion;
        int status_number;
        String status;
        String Date;
        String ContentType;
        String charset;

        public Header() {
            protocol = "HTTP";
            protocolVersion = "1.1";
            status_number = 200;
            status = "OK";
            Date = getTime_GMT();
            ContentType = "text/html";
            charset="UTF-8";
        }

        @Override
        public String toString() {
            return protocol + '/' + protocolVersion + ' '
                    + status_number + ' '
                    + status + '\n' +
                    "Date: " + Date + '\n' +
                    "Content-Type: " + ContentType + ';' +
                    "charset="+charset + '\n';
        }
    }

    String getTime_GMT(){
        Calendar cd = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT")); // 设置时区为GMT
        return sdf.format(cd.getTime());
    }

    public Response file(Path path){
        if (path == null) {
            header.status_number=404;
            header.status = "Not Found";
            return file(server.route("/NotFound.html"));
        }
        try {
            file = path.toFile();
            if (file.getName().endsWith(".json")) return parse();
            if (file.getName().endsWith(".css")) header.ContentType = "text/css";
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) write(sc.nextLine());
        } catch (FileNotFoundException e) {
            header.status_number=404;
            header.status = "Not Found";
            e.printStackTrace();
            return file(server.route("/NotFound.html"));
        }
        return this;
    }

    Reflex rx = null;

    public Response parse(){
        HashMap<String,Object> content = HttpServer.json(file);
        if (content==null) return this;
        ArrayList<String> reg_script = (ArrayList<String>) content.get("script");
        ArrayList<String> reg_scope = (ArrayList<String>) content.get("scope");
        rx = new Reflex(reg_script,reg_scope);
        rx.parse();
        String returnStatement = content.get("return").toString();
        String reg_var = "\\[(g|G|p|P|s|S):.+?]";
        write(replace(returnStatement,reg_var, this::parseParam));
        return this;
    }

    private String parseParam(String param){
        Scanner sc = new Scanner(param.substring(1,param.length()-1));
        sc.useDelimiter(":");
        while (sc.hasNext()) {
            String each = sc.next();
            switch (each.toUpperCase()) {
                case "G":
                    return server.req.param_GET.get(sc.next());
                case "P":
                    Object got = server.req.param_POST.get(sc.next());
                    while (got instanceof HashMap) {
                        got = ((HashMap) got).get(sc.next());
                    }
                    if (got == null) return null;
                    return got.toString();
                case "S":
                    return rx.param_SCPOE.get(sc.next());
            }
        }
        return null;
    }

    static public StringBuilder replace(String line,String pattern, Function<String,String> deal){
        Matcher m = Pattern.compile(pattern).matcher(line);
        StringBuilder res= new StringBuilder();
        int start=0,end=0;
        while (m.find()) {
            String each  = m.group();
            start = m.start();
            res.append(line.substring(end,start));
            res.append(deal.apply(each));
            end = m.end();
        }
        res.append(line.substring(end));
        return res;
    }

    public void end(){
        raw.println(header);
        raw.println(datagram);
        try {
            server.server.close();
        } catch (IOException e) {
            System.out.println("关闭出错");
            e.printStackTrace();
        }
    }
}
