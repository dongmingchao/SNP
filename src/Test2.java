import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Test2 {
    public static void main(String[] args) throws Exception {
        HashMap<String, Object> got = HttpServer.json("route.json");
        System.out.println(got);
        HashMap<String, LinkedList<Path>> res = new HashMap<>();
        got.forEach((key, value) -> {
            if (value instanceof String) {
                LinkedList<Path> paths = new LinkedList<>();
                if (key.matches("/.*")) {
                    System.out.println(key);
                    paths.add(Paths.get(((String) value)));
                    res.put(key, paths);
                } else if (key.equals("[all]")){
                    try {
                        if (((String) value).endsWith("*")) {
                            String typeDir = ((String) value).split("/\\*")[0];
                            paths.addAll(Files.walk(Paths.get(typeDir)).collect(Collectors.toList()));
                        } else {
                            paths.addAll(Files.walk(Paths.get(((String) value)), 1).collect(Collectors.toList()));
                        }
                    } catch (IOException e) {
                        System.out.println("路由文件配置出错：没有文件夹");
                        e.printStackTrace();
                    }
                    res.put(key.substring(1, key.length() - 1), paths);
                } else if (key.matches("\\[.+]")) {
                    try {
                        String type = key.substring(1,key.length()-1);
                        if (((String) value).endsWith("*")) {
                            String typeDir = ((String) value).split("/\\*")[0];
                            paths.addAll(Files.walk(Paths.get(typeDir)).filter(path -> path.getFileName().toString().endsWith("."+type)).collect(Collectors.toList()));
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
        System.out.println(res);
    }

    public static void testGetAvailableCharsets() {

        // 获得本机所有编码格式
        Map<String, Charset> charsets = Charset.availableCharsets();
        // 迭代遍历出编码方式
        for (Map.Entry<String, Charset> entry : charsets.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue().name());
        }

    }

    static public StringBuilder replace(String line, String pattern, BiConsumer<String, String[]> deal) {
        Matcher m = Pattern.compile(pattern).matcher(line);
        StringBuilder res = new StringBuilder();
        int start = 0, end = 0;
        String[] re = {""};
        while (m.find()) {
            String each = m.group();
            start = m.start();
            res.append(line.substring(end, start));
            deal.accept(each, re);
            res.append(re[0]);
            end = m.end();
        }
        return res;
    }
}
