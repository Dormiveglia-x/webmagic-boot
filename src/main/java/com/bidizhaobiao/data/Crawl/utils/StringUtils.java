package com.bidizhaobiao.data.Crawl.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static final String ALL_WHITE_SPACE = "\u00a0\u1680\u180e\u2000-\u200a\u2028\u2029\u202f\u205f\u3000\ufeff\\s";

    /**
     * 删除字符串首尾的所有空格
     *
     * @param str 待处理字符串
     * @return 处理后字符串
     */
    public static String trim(String str) {
        if (str == null) {
            return null;
        }

        str = str.replaceAll(String.format("^[%s]+", ALL_WHITE_SPACE), "")
                .replaceAll(String.format("[%s]+$", ALL_WHITE_SPACE), "");
        return str;
    }

    /**
     * 删除字符串的所有空格
     *
     * @param str 待处理字符串
     * @return 处理后字符串
     */
    public static String removeAllSpace(String str) {
        if (str == null) {
            return null;
        }

        str = str.replaceAll(String.format("[%s]+", ALL_WHITE_SPACE), "");
        return str;
    }

    /**
     * 删除字符串中连续多个空格，仅保留一个，并统一保留的空格格式为半角空格
     *
     * @param str 待处理字符串
     * @return 处理后字符串
     */
    public static String removeRedundantSpace(String str) {
        if (str == null) {
            return null;
        }

        str = str.replaceAll(String.format("[%s]+", ALL_WHITE_SPACE), " ");
        return str;
    }

    /**
     * 判断字符序列是否为null或为空序列
     *
     * @param cs 待判定字符序列
     * @return 判定结果
     */
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * 判断字符序列在忽略空格的情况下是否为null或为空序列
     *
     * @param cs 待判定字符串
     * @return 判定结果
     */
    public static boolean isEmptyIfRgdlssSpace(CharSequence cs) {
        if (cs == null) {
            return true;
        }
        String str = cs.toString();
        str = removeAllSpace(str);
        if (str.isEmpty()) {
            return true;
        }

        return false;
    }

    /**
     * 合并多个对象为字符串
     *
     * @param elements 对象集
     * @param <T>      对象类型参数
     * @return 合并得到的字符串
     */
    @SafeVarargs
    public static <T> String join(T... elements) {
        return org.apache.commons.lang3.StringUtils.join(elements);
    }

    /**
     * 用指定 {@code separator} 分割字符串 {@code str}
     *
     * @param str       待分割字符串
     * @param separator 分隔符
     * @return
     */
    public static String[] splitByWholeSeparator(final String str, final String separator) {
        return org.apache.commons.lang3.StringUtils.splitByWholeSeparator(str, separator);
    }

    /**
     * 比较两个字符串 {@code str1} 和 {@code str2} 是否相等
     * <p>当 {@code str1} 和 {@code str2} 中的任意一个为null时，返回结果一定为false</p>
     *
     * @param str1 待比较的字符串1
     * @param str2 待比较的字符串2
     * @return 字符串相等性测试结果
     */
    public static boolean isEqual(final String str1, final String str2) {
        if (str1 == null || str2 == null) {
            return false;
        }

        return str1.equals(str2);
    }

    /*
     * 通过正则获取一条连接上的域名信息
     */
    public static String getHostByUrl(String url) {
        Pattern pattern = Pattern.compile("^(http://|https://)(.*?)([0-9])*/");
        Matcher matcher = pattern.matcher(url);
        String host = "";
        if (matcher.find()) {
            host = matcher.group(0);
        }
        host = host.substring(0, host.length() - 1);
        return host;
    }


}
