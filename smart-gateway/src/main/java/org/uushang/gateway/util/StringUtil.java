package org.uushang.gateway.util;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * 字符串相关操作
 *
 *
 * @author liyue
 * @version v1
 * @create 2019-04-03 14:07:21
 * @copyright www.liderong.cn
 */
public class StringUtil {

    private static final Pattern PATTERN = Pattern.compile("\\s*|\t|\r|\n");

    /**
     * 去掉空格,换行和制表符
     * @param str
     * @return
     */
    public static String format(String str){
        if (!StringUtils.isEmpty(str)) {
            Matcher m = PATTERN.matcher(str);
            return m.replaceAll("");
        }
        return str;
    }
}