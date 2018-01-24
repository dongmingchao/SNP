import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Reflex {

    public static void main(String[] args) {
        HashMap<String, Object> json = HttpServer.json("file.json");
        ArrayList<String> e = (ArrayList<String>) json.get("scope");
        ArrayList<String> t = (ArrayList<String>) json.get("script");
        Reflex rf = new Reflex(t, e);
        rf.parse();
//        Path[] got = Script.FSwalk(Paths.get("page"),1);
//        if (got==null) return;
//        for (Path s : got) {
//            System.out.println(s.getFileName());
//        }
    }

    ArrayList<String> param;//LinkedList
    HashMap<String, Object> vars;
    Class<Script> script;
    HashMap<String, String> param_SCPOE;

    public Reflex(ArrayList<String> param, ArrayList<String> scope) {
        this.param = param;
        create_vars(scope);
        script = Script.class;
        param_SCPOE = new HashMap<>();
    }

    /**
     * 从json中的script字段读出并解析语法
     * 从scope字段读出变量
     *
     * @return
     */
    void parse() {
//        Collections.reverse(param);
        for (int i1 = 0; i1 < param.size(); i1+=3) {
            String key = param.get(i1);
            String res = null;
            String operator = param.get(i1+1);
            String express = param.get(i1+2);
            if (vars.containsKey(key)) {
                String[] deal = express.split("\\(");
                String methodName = deal[0];
                deal = deal[1].split("[,)]");
                Object[] args = new Object[deal.length];
                for (int i = 0; i < deal.length; i++) {
                    String s = deal[i];
                    if (s.matches("'.+'")) args[i] = s.substring(1, s.length() - 1);
                    else args[i] = Integer.parseInt(s);
                }
                String[] subCla = methodName.split("\\.");
                methodName = subCla[0].toUpperCase().concat(subCla[1]);
                try {
                    Object script_Obj = script.newInstance();
                    switch (args.length) {
                        case 3: {
                            Method method = script.getMethod(methodName, args[0].getClass(), args[1].getClass(), args[2].getClass());
                            res = (String) method.invoke(script_Obj, args[0], args[1],args[2]);
                        }
                        case 2: {
                            Method method = script.getMethod(methodName, args[0].getClass(), args[1].getClass());
                            res = (String) method.invoke(script_Obj, args[0], args[1]);
                        }
                        case 1: {
                            Method method = script.getMethod(methodName, args[0].getClass());
                            res = (String) method.invoke(script_Obj, args[0]);
                        }
                    }
                } catch (NoSuchMethodException e) {
                    System.out.println("没有此内建方法");
                    e.printStackTrace();
                } catch (IllegalAccessException | InstantiationException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                if (operator.equals("=")) {
                    param_SCPOE.put(key, res);
                }
                System.out.println("方法名：" + methodName);
                System.out.println(Arrays.toString(deal));
            }
        }
    }

    /**
     * 将scpoe中的变量变成hashMap的Key
     */
    void create_vars(ArrayList<String> scope) {
        vars = new HashMap<>();
        for (String s : scope) {
            vars.put(s, null);
        }
    }
}
