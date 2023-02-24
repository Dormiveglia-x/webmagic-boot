package com.bidizhaobiao.data.Crawl.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeDiff {
    public static void main(String[] args) {
        String startTime = "2018-10-30 00:00:00";
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date startDateTime = sdf1.parse((startTime));
            insert();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    //计算日期差
    public static int getDatePoolByDay(Date nowDate, Date startDate) {
        long nd = 1000 * 24 * 60 * 60;
        // 获得两个时间的毫秒时间差异
        long diff = nowDate.getTime() - startDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        int h = (int) day;
        return h;
    }

    public static Map getDatePoor(Date nowDate, Date endDate) {
        Map map = new HashMap();
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        int h = (int) hour;
        System.out.println(h);
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        int m = (int) min;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        map.put("day", day);
        map.put("hour", h);
        map.put("min", m);
        return map;
    }

    public static void insert() {
        String war = "warName:/home/appuser/apache-tomcat-test/webapps/bxkc-0.0.1-SNAPSHOT/WEB-INF/classes/com/bidizhaobiao/data/Crawl/scheduler/";
        Pattern r = Pattern.compile("(.*?)webapps");
        Matcher m = r.matcher(war);
        if (m.find()) {
            String text = m.group();
            System.out.println(text);
        }
    }
}
