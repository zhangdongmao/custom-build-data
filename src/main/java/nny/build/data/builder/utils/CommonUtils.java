package nny.build.data.builder.utils;

import nny.build.data.builder.exception.BuilderException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用工具类
 *
 * @author shengyong.huang
 * @date 2020-04-23
 */
public class CommonUtils {

    /**
     * 解析${}正则表达式
     */
    private static final Pattern REF_EXPRESSION_REGEX = Pattern.compile("\\$\\{([^}]*)}");
    private static final Pattern REF_PARAM_EXPRESSION_REGEX = Pattern.compile("\\[([^]}]*)]");

    public static List<String> matchRefExpression(String str) {
        List<String> matcherList = new ArrayList<>(10);
        Matcher matcher = REF_EXPRESSION_REGEX.matcher(str);
        while (matcher.find()) {
            matcherList.add(matcher.group(1));
        }
        return matcherList;
    }

    public static List<String> matchRefParamExpression(String str) {
        List<String> matcherList = new ArrayList<>(10);
        Matcher matcher = REF_PARAM_EXPRESSION_REGEX.matcher(str);
        while (matcher.find()) {
            matcherList.add(matcher.group(1));
        }
        return matcherList;
    }


    /**
     * 序列化方式深拷贝
     *
     * @param src
     * @param <T>
     * @return
     */
    public static <T> List<T> deepCopy(List<T> src) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(byteOut);
            out.writeObject(src);

            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream in = new ObjectInputStream(byteIn);

            @SuppressWarnings("unchecked")
            List<T> dest = (List<T>) in.readObject();
            return dest;

        } catch (Exception e) {
            e.printStackTrace();
            throw new BuilderException("deep error");
        }

    }

    /**
     * 序列化方式深拷贝
     *
     * @param src
     * @param <T>
     * @return
     */
    public static <T> T deepCopy(T src) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(byteOut);
            out.writeObject(src);

            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream in = new ObjectInputStream(byteIn);

            @SuppressWarnings("unchecked")
            T dest = (T) in.readObject();
            return dest;

        } catch (Exception e) {
            e.printStackTrace();
            throw new BuilderException("deep error");
        }
    }

    /**
     * 合并ConcurrentHashMap
     * @param CSVMap1
     * @param CSVMap2
     * @return
     */
    public static ConcurrentHashMap<String, List<String>> mergeCSVMap(ConcurrentHashMap<String, List<String>> CSVMap1, ConcurrentHashMap<String, List<String>> CSVMap2){
        if (CSVMap1.isEmpty()){
            return CSVMap2;
        }else if (CSVMap2.isEmpty()){
            return CSVMap1;
        }
        for (Map.Entry<String, List<String>> entry :CSVMap1.entrySet()){
            if (CSVMap2.containsKey(entry.getKey())){
                CSVMap2.get(entry.getKey()).addAll(entry.getValue());
            }else {
                CSVMap2.put(entry.getKey(),entry.getValue());
            }
        }
        return CSVMap2;
    }

    public static void main(String[] args) {
        String e = "${9.bm_cc_loan_plan.plan_detl_id.[billing_tenor:1]}";

        List<String> s = matchRefParamExpression(e);

        s.forEach(System.out::println);
    }

}
