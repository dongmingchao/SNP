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
            DataInputStream flagStream = new DataInputStream(new ByteArrayInputStream(("--" + boundary).getBytes()));
            LinkedList<Integer> flagInt = new LinkedList<>();
            try {
                while (flagStream.available() > 0)
                    flagInt.add(flagStream.readUnsignedByte());
            } catch (IOException e) {
                System.out.println("boundary转换为unsignByte失败");
                e.printStackTrace();
            }
            DataInputStream post = new DataInputStream(in);
            LinkedList<LinkedList<Integer>> receive = new LinkedList<>();
            try {
                while (post.available() > 0) {
                    String param = listToString(depart(post, flagInt, false));
                    if (param.equals("")) continue;
                    System.out.println("param: "+param);
                    String key = param.split("\"")[1];
                    System.out.println("key: "+key);
                    if (param.split("; ").length==3 && param.split("\"").length == 4){
                        String filename = param.split("\"")[3];
                        String filetype = listToString(depart(post, flagInt, false));
                        System.out.println("filename: "+filename);
                        System.out.println("filetype: "+filetype);
                        depart(post, flagInt, false);//只是为了输出那个key和value之间的空行 TDOD: 换成更底层的控制
                        receive.add(depart(post, flagInt, false));
                    }else {
                        depart(post, flagInt, false);//只是为了输出那个key和value之间的空行 TDOD: 换成更底层的控制
                        System.out.println("value: " + listToString(depart(post, flagInt, false)));
                    }
//                    param_POST.put(, param.split("\\n\\r\\n")[1]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            receive.forEach(each -> {
                System.out.println("---------------------------");
                byte[] s = new byte[each.size()];
                for (int i = 0; i < each.size(); i++) {
                    s[i] = each.get(i).byteValue();
                }
                String theS = new String(s);
                System.out.println(theS);
                System.out.println("---------------------------");
            });
//            ArrayList<Integer> fileRaw = readUnsignedByte(in,flagInt);
//            writeFile(receive.getLast(), "ano.png", 0);
        }
    }

    private String listToString(LinkedList<Integer> each) {
        int length = each.size();
        byte[] s = new byte[length];
        for (int i = 0; i < length; i++) {
            s[i] = each.poll().byteValue();
        }
        return new String(s);
    }

    private LinkedList<Integer> depart(DataInputStream post, LinkedList<Integer> flagInt, boolean notBreak) {
        LinkedList<Integer> res = new LinkedList<>();
        boolean wait = true;
        try {
            while (wait) {
                int got = post.readUnsignedByte();
                if (got == flagInt.getFirst() && post.available() > flagInt.size()) {
                    LinkedList<Integer> compare = new LinkedList<>();
                    compare.add(got);
                    for (int i = 0; i < flagInt.size() - 1; i++) {
                        int meta = post.readUnsignedByte();
                        if (meta == 13 && !notBreak) {
                            int next = post.readUnsignedByte();
                            if (next == 10) {
                                wait = false;
                                break;
                            } else {
                                compare.add(meta);
                                compare.add(next);
                            }
                        } else compare.add(meta);
                    }
                    System.out.println(compare.equals(flagInt));
//                    System.out.print("compare= ");
//                    compare.forEach(System.out::println);
//                    System.out.print("flagInt= ");
//                    flagInt.forEach(System.out::println);
                    if (!compare.equals(flagInt)) res.addAll(compare);
                    else {
                        post.readByte();
                        post.readByte();
                    }
                } else if (got == 13 && !notBreak) {
                    int next = post.readUnsignedByte();
                    if (next == 10)
                        wait = false;
                    else {
                        res.add(got);
                        res.add(next);
                    }
                } else res.add(got);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public ArrayList<Integer> readUnsignedByte(InputStream in, LinkedList<Integer> flagInt) {
        ArrayList<Integer> res = new ArrayList<>();
        try {
            DataInputStream op = new DataInputStream(in);
            while (op.available() > 0) {
                int got = op.readUnsignedByte();
                if (got == flagInt.getFirst() && op.available() > flagInt.size()) {
                    LinkedList<Integer> compare = new LinkedList<>();
                    compare.add(got);
                    for (int i = 0; i < flagInt.size() - 1; i++) {
                        compare.add(op.readUnsignedByte());
                    }
                    System.out.println(compare.equals(flagInt));
                    System.out.print("compare= ");
                    compare.forEach(System.out::println);
                    System.out.print("flagInt= ");
                    flagInt.forEach(System.out::println);
                    if (!compare.equals(flagInt)) res.addAll(compare);
                } else res.add(got);
            }
            System.out.println("结束");
            return res;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void writeFile(LinkedList<Integer> in, String fileName, long skip) {
        if (in == null) return;
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName));
            in.forEach(each -> {
                try {
                    out.writeByte(each);
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
