import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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

    static Path[] FSls(Path path, int maxDepth){
        try {
            return Files.walk(path,maxDepth).toArray(Path[]::new);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
