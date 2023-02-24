package com.bidizhaobiao.data.Crawl.entity.oracle;

import java.util.Date;

/**
 * @author 廉建林
 * @version 1.0
 * @date 2020/6/24 14:45
 * 类说明：用来封装待爬取的对象，从而进行排序
 */
public class NeedStartSpider {
    private String className;
    private int  crawlType;
    private Date crawlStartTime;

    public int getCrawlType() {
        return crawlType;
    }

    public void setCrawlType(int crawlType) {
        this.crawlType = crawlType;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Date getCrawlStartTime() {
        return crawlStartTime;
    }

    public void setCrawlStartTime(Date crawlStartTime) {
        this.crawlStartTime = crawlStartTime;
    }
}
