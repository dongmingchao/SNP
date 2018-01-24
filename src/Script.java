import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Script {

    public static String FSls(String path,Integer maxDepth){
        Path[] got = FSls(Paths.get(path),maxDepth);
        if (got==null) return null;
        else return Arrays.stream(got).map(each -> "\""+each+"\"").collect(Collectors.toList()).toString();
    }

    public static String FSls(String path){
        Path[] got = FSls(Paths.get(path),1);
        if (got==null) return null;
        else return Arrays.stream(got).map(each -> "\""+each+"\"").collect(Collectors.toList()).toString();
    }

    public static String FSinspect (String path){
        return deJson(FSinspect(Paths.get(path)));
    }

    /**
     * 转换=为:，添加双引号
     * @param in
     * @return
     */
    static String deJson(HashMap<String,Object> in){
        StringBuilder res = new StringBuilder("{");
        in.forEach((key, value) -> {
            res.append("\"").append(key).append("\":");
            if (value instanceof Boolean) res.append(value);
            else res.append("\"").append(value).append("\"");
            res.append(", ");
        });
        int length = res.length();
        res.delete(length-2,length);
        res.append("}");
        return res.toString();
    }

    static HashMap<String,Object> FSinspect(Path path){
        File got = path.toFile();
        HashMap<String,Object> res = new HashMap<>();
        res.put("name",got.getName());
        res.put("isDirectory",got.isDirectory());//bool
        try {
            res.put("size",Files.size(path));//long
        } catch (IOException e) {
            res.put("size","获取出错");
            e.printStackTrace();
        }
        try {
            res.put("lastModified",Files.getLastModifiedTime(path));//FileTime
        } catch (IOException e) {
            res.put("lastModified","获取出错");
            e.printStackTrace();
        }
        return res;
    }

    static Path[] FSls(Path path, int maxDepth){
        try {
            return Files.walk(path,maxDepth).toArray(Path[]::new);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println(FSinspect("cloud/desktop/test.png"));
    }
}
