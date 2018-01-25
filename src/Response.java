import java.io.*;
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
    OutputStream out;
    HttpServer server;
    StringBuilder datagram = new StringBuilder();
    Header header = new Header();
    File file;

    public Response(OutputStream out,HttpServer server) {
        this.out = out;
        this.raw = new PrintWriter(out , true);;
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
        file = path.toFile();
        System.out.println("file: "+file);
        if (file.getName().endsWith(".json")) return parse();
        String[] suffix_T = file.getName().split("\\.");
        header.ContentType = server.getMIME("."+suffix_T[suffix_T.length-1]);
        writeFile(file);
        return this;
    }

    byte[] bindata = null;

    public void writeFile(File file){
        try {
            FileInputStream in = new FileInputStream(file);
            bindata = new byte[in.available()];
            in.read(bindata);
        } catch (FileNotFoundException e) {
            header.status_number=404;
            header.status = "Not Found";
            file(server.route("/NotFound.html"));
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Reflex rx = null;

    public Response parse(){
        HashMap<String,Object> content = HttpServer.json(file);
        if (content==null) return this;
        ArrayList<String> reg_script = (ArrayList<String>) content.get("script");
        ArrayList<String> reg_scope = (ArrayList<String>) content.get("scope");
        rx = new Reflex(reg_script,reg_scope,server.req);
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
        if (datagram.length() != 0) raw.println(datagram);
        if (bindata != null) {
            try {
                out.write(bindata);
            } catch (IOException e) {
                System.out.println("写二进制数据出错");
                e.printStackTrace();
            }
        }
        try {
            server.server.close();
        } catch (IOException e) {
            System.out.println("关闭出错");
            e.printStackTrace();
        }
    }
}
