import javafx.scene.shape.Path;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test2 {
    public static void main(String[] args) throws Exception {
        File file = Paths.get("cloud/ano.png").toFile();
        FileInputStream in = new FileInputStream(file);
        byte[] store = new byte[in.available()];
        in.read(store);
        System.out.println(Arrays.toString(store));
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
