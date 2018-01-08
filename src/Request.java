import java.io.*;
import java.nio.Buffer;
import java.nio.charset.Charset;
import java.util.*;

public class Request {
    Scanner raw;
    InputStream inputStream;
    String method;
    String url;
    HashMap<String, String> param_GET;
    HashMap<String, Object> param_POST;
    String protocol;
    String protocolVersion;
    HashMap<String, String> header = new HashMap<>();

    public Request(InputStream in) {
        this.inputStream = in;
        this.raw = new Scanner(in);

//        while (raw.hasNextLine()) {
//            String got = raw.nextLine();
//            System.out.println(got);
//        }

        String[] firstLine = raw.nextLine().split(" ");
        if (firstLine.length > 2) {
            method = firstLine[0];
            if (!firstLine[1].contains("?")) url = firstLine[1];
            else {
                String[] raw_url = firstLine[1].split("\\?");
                url = raw_url[0];
                Scanner getPara = new Scanner(raw_url[1]);
                getPara.useDelimiter("&");
                param_GET = new HashMap<>();
                while (getPara.hasNext()) {
                    String[] para = getPara.next().split("=");
                    param_GET.put(para[0], para[1]);
                }
            }
            protocol = firstLine[2].split("/")[0];
            protocolVersion = firstLine[2].split("/")[1];
        }
        while (raw.hasNextLine()) {
            String got = raw.nextLine();
            if (got.equals("")) break;
            header.put(got.split(": ")[0], got.split(": ")[1]);
        }
        param_POST = new HashMap<>();
        String type = header.get("Content-Type");
        System.out.println(type);
        if (type == null) return;
        if (type.matches("multipart/form-data; boundary=----WebKitFormBoundary.+")) {
            String boundary = type.substring(30);
            System.out.println("本次boundary是" + boundary);
//            raw.useDelimiter("--" + boundary);
            String maybeLength = header.get("Content-Length");
            if (maybeLength != null) {
                try {
//                    Integer postLength = Integer.valueOf(maybeLength);
//                    System.out.println(postLength);
//                    Integer nowLength = 0;
                    while (raw.hasNextLine()) {
                        String chunk = raw.nextLine();
//                        nowLength = chunk.length() + 1 + nowLength;
                        if (chunk.matches("--" + boundary)) continue;
                        else if (chunk.split("Content-Disposition: form-data; name=\".+?\"").length == 0) {
//                            String value = raw.nextLine();
//                            nowLength = nowLength + value.length() + 1;
//                            param_POST.put(chunk.substring(38, chunk.length() - 1), value);
//                            System.out.println(chunk.substring(38, chunk.length() - 1));
                        } else if (chunk.split("Content-Disposition: form-data; name=\".+?\"; filename=\".+?\"").length == 0) {
//                            String key = chunk.split("Content-Disposition: form-data; name=\"")[1].split("\"; filename=\"")[0];
//                            String value_fileName = chunk.split("Content-Disposition: form-data; name=\".+?\"; filename=\"")[1].split("\"")[0];
//                            System.out.println("key\t" + key);
//                            System.out.println("fileName\t" + value_fileName);
//                            String contentType = raw.nextLine();
//                            nowLength = nowLength + contentType.length() + 1;
//                            contentType = contentType.split(": ")[1];
//                            System.out.println(contentType);
//                            System.out.println(nowLength);
                            ArrayList<Integer> fileRaw = readInt(in,boundary);
                            writeFile(fileRaw, "test.png", 0);
//                            while (raw.hasNextLine())
//                                System.out.println(raw.nextLine());
                            return;
                        } else if (chunk.equals("")) continue;
//                        else {
//                            System.out.println(chunk.split("; ")[1]);
//                        }
//                    for(int i=0; i<count; i++){
//                        out.write(buffer[i]);
//                    }
                    }
                } catch (NumberFormatException e) {
                    System.out.println("数字解析出错");
                    e.printStackTrace();
                }
            }
        }
    }

    public ArrayList<Integer> readInt(InputStream in,String boundary) {
        ArrayList<Integer> res = new ArrayList<>();
        DataInputStream flagStream = new DataInputStream(new ByteArrayInputStream(boundary.getBytes()));
        LinkedList<Integer> flagInt = new LinkedList<>();
        try {
            while (flagStream.available() > 3)
                flagInt.add(flagStream.readInt());
        }catch (IOException e) {
            System.out.println("boundary转换为int失败");
            e.printStackTrace();
        }
        try {
            DataInputStream op = new DataInputStream(in);
            while (op.available()>3){
                int got = op.readInt();
//                if (got==1970168929) break;
                if (got==757926413){
                    LinkedList<Integer> compare = new LinkedList<>();
                    for (int i = 0; i < flagInt.size(); i++) {
                        compare.add(op.readInt());
                    }
                    System.out.println(compare.equals(flagInt));
                    System.out.print("compare= ");
                    compare.forEach(System.out::println);
                    System.out.println(boundary);
                    System.out.print("flagInt= ");
                    flagInt.forEach(System.out::println);
                    if (compare.equals(flagInt)) break;
                    else res.addAll(compare);
                }
                res.add(got);
            }
            System.out.println("结束");
            return res;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void writeFile(ArrayList<Integer> in, String fileName, long skip) {
        if (in == null) return;
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName));
            in.forEach(each -> {
                try {
                    out.writeInt(each);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            out.close();
        } catch (FileNotFoundException e) {
            System.out.println("文件未找到");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("关闭出错");
            e.printStackTrace();
        }
    }

    public String gvar(String key) {
        if (param_GET == null) return null;
        else return param_GET.get(key);
    }
}
