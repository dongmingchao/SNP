import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Script {

    public static String FSls(String path, Integer maxDepth) {
        Path[] got = FSls(Paths.get(path), maxDepth);
        if (got == null) return null;
        else return Arrays.stream(got).map(each -> "\"" + each + "\"").collect(Collectors.toList()).toString();
    }

    public static String FSls(String path) {
        Path[] got = FSls(Paths.get(path), 1);
        if (got == null) return null;
        else return Arrays.stream(got).map(each -> "\"" + each + "\"").collect(Collectors.toList()).toString();
    }

    public static String FSinspect(String path) {
        return deJson(FSinspect(Paths.get(path)));
    }

    public static String FSsave(String path, String name, LinkedList<Integer> source) {
        return FSsave(Paths.get(path+"/"+name),source);
    }

    public static String FSrm(String path){
        return FSrm(Paths.get(path));
    }

    static String FSrm(Path path){
        try {
            if (Files.deleteIfExists(path)) return "success";
            else return "failed";
        } catch (IOException e) {
            System.out.println("文件读取出错");
            e.printStackTrace();
            return "failed";
        }
    }

    static String FSsave(Path path, LinkedList<Integer> source) {
        if (source == null) return "null";
        File outFIle = path.toFile();
        if (!outFIle.exists()) {
            try {
                if (!outFIle.createNewFile()) return "create file failed";
            } catch (IOException e) {
                System.out.println("无法创建文件");
                e.printStackTrace();
            }
        }
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(outFIle));
            source.forEach(each -> {
                try {
                    out.writeByte(each);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            out.close();
            source.clear();
            return "success";
        } catch (FileNotFoundException e) {
            System.out.println("文件未找到");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("关闭出错");
            e.printStackTrace();
        }
        return "failed";
    }

    /**
     * 转换=为:，添加双引号
     *
     * @param in
     * @return
     */
    static String deJson(HashMap<String, Object> in) {
        StringBuilder res = new StringBuilder("{");
        in.forEach((key, value) -> {
            res.append("\"").append(key).append("\":");
            if (value instanceof Boolean) res.append(value);
            else res.append("\"").append(value).append("\"");
            res.append(", ");
        });
        int length = res.length();
        res.delete(length - 2, length);
        res.append("}");
        return res.toString();
    }

    static HashMap<String, Object> FSinspect(Path path) {
        File got = path.toFile();
        HashMap<String, Object> res = new HashMap<>();
        res.put("name", got.getName());
        res.put("isDirectory", got.isDirectory());//bool
        try {
            res.put("size", Files.size(path));//long
        } catch (IOException e) {
            res.put("size", "获取出错");
            e.printStackTrace();
        }
        try {
            res.put("lastModified", Files.getLastModifiedTime(path));//FileTime
        } catch (IOException e) {
            res.put("lastModified", "获取出错");
            e.printStackTrace();
        }
        return res;
    }

    static Path[] FSls(Path path, int maxDepth) {
        try {
            return Files.walk(path, maxDepth).toArray(Path[]::new);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
    }
}
