import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test2 {
    public static void main(String[] args) {
        try {
            String flag = "----WebKitFormBoundary1uHTBlnPRxc1k36v";
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(flag.getBytes()));
            while (in.available() > 0)
                System.out.println(in.readInt());

//            1919763055
//            1970168929
//            1920547128
//            1700025713
//            1248941172
//            1365337154
//            879897901

//            1920554800
//            1682135350
//            1665418348
//            1110468405
//            1954098477

//            757935405
//            1466262091
//            1769227887
//            1919763055
//            1970168929  ---====---
//            1920562241
//            927613818
//            1630628460
//            1414934833
        } catch (IOException e) {
            e.printStackTrace();
        }
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
