package com.bidizhaobiao.data.Crawl.entity.mongo;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author 作者: 廉建林
 * @version 创建时间：2018年9月14日 上午10:02:54
 * config实体类
 */
@Document(collection = "BXKC_CRAWLCONFIG")
@Entity
public class CrawlerConfig {
    @Id
    private String id;
    @Field("DOCCHANNEL")
    private int docChannel;
    @Field("CRAWL_RESOURCESNAME")
    private String crawlResourcesName;
    @Field("CRAWL_RESOURCESNUMBER")
    private String crawlResourcesNum;
    @Field("CRAWL_STARTTIME")
    private String crawlStartTime;
    @Field("CRAWL_STOPTIME")
    private String crawlStopTime;
    @Field("CRAWL_PERIOD")
    private double crawlPeriod;
    @Field("CRAWL_THREADNUM")
    private int crawlThreadNum;
    @Field("CRAWL_RESOURCESFROM")
    private String crawlResourcesFrom;
    @Field("CRAWL_RESOURCESLEVEL")
    private String crawlResourcesLevel;
    @Field("CRAWL_STATUS")
    private int crawlStatus;
    @Field("CRAWL_CREATEBY")
    private String crawlCreateBy;
    @Field("CRAWL_CLASS")
    private String crawlClass;
    @Field("CRAWL_NUMBER")
    private String crawlNumber;
    //访问爬虫的链接
    @Field("CRAWL_INTERFACE")
    private String crawlInterFace;
    //爬虫全量或者增量：0：全量1：增量
    @Field("CRAWL_TYPE")
    private int crawlType;
    //全量爬虫的页码
    @Field("CRAWL_PAGENUM")
    private int crawlPagenum;
    //爬虫的上架时间
    @Field("CRAWL_CREATETIME")
    private String crawlCreatetime;
    //定时器名称
    @Field("CRAWL_TIMERNAME")
    private String timerName;
    //上一次校验的时间
    @Field("CRAWL_CHECKTIME")
    private String crawlChecktime;
    //记录正常启动的次数
    @Field("CRAWL_NORMALSTARTCOUNT")
    private int crawlNormalStartCount;
    //记录异常启动的次数
    @Field("CRAWL_ERRORSTARTCOUNT")
    private int crawlErrorStartCount;
    @Field("CRAWL_CHECKPERIOD")
    private int crawlCheckPeriod;
    //记录入库成功的条数
    @Field("CRAWL_SUCCESSNUM")
    private int crawlSuccessNum;

    //记录下次校验的页数
    @Field("NEED_CHECKNUM")
    private int needCheckPageNum;
    //爬虫的运行状态  0：未运行 1：正在运行
    @Field("SPIDERING_STATUS")
    private int spideringstatus = 0;

    public int getSpideringstatus() {
        return spideringstatus;
    }

    public void setSpideringstatus(int spideringstatus) {
        this.spideringstatus = spideringstatus;
    }

    public int getCrawlSuccessNum() {
        return crawlSuccessNum;
    }

    public void setCrawlSuccessNum(int crawlSuccessNum) {
        this.crawlSuccessNum = crawlSuccessNum;
    }

    public int getCrawlCheckPeriod() {
        return crawlCheckPeriod;
    }

    public void setCrawlCheckPeriod(int crawlCheckPeriod) {
        this.crawlCheckPeriod = crawlCheckPeriod;
    }

    public int getCrawlNormalStartCount() {
        return crawlNormalStartCount;
    }

    public void setCrawlNormalStartCount(int crawlNormalStartCount) {
        this.crawlNormalStartCount = crawlNormalStartCount;
    }

    public int getCrawlErrorStartCount() {
        return crawlErrorStartCount;
    }

    public void setCrawlErrorStartCount(int crawlErrorStartCount) {
        this.crawlErrorStartCount = crawlErrorStartCount;
    }

    public String getCrawlChecktime() {
        return crawlChecktime;
    }

    public void setCrawlChecktime(String crawlChecktime) {
        this.crawlChecktime = crawlChecktime;
    }

    public String getCrawlCreateTime() {
        return crawlCreatetime;
    }

    public void setCrawlCreateTime(String crawlCreatetime) {
        this.crawlCreatetime = crawlCreatetime;
    }

    public String getTimerName() {
        return timerName;
    }

    public void setTimerName(String timerName) {
        this.timerName = timerName;
    }

    public int getCrawlPagenum() {
        return crawlPagenum;
    }

    public void setCrawlPagenum(int crawlPagenum) {
        this.crawlPagenum = crawlPagenum;
    }

    public String getCrawlInterFace() {
        return crawlInterFace;
    }

    public void setCrawlInterFace(String crawlInterFace) {
        this.crawlInterFace = crawlInterFace;
    }

    public String getCrawlCreatetime() {
        return crawlCreatetime;
    }

    public void setCrawlCreatetime(String crawlCreatetime) {
        this.crawlCreatetime = crawlCreatetime;
    }

    public int getCrawlType() {
        return crawlType;
    }

    public void setCrawlType(int crawlType) {
        this.crawlType = crawlType;
    }

    public String getCrawlNumber() {
        return crawlNumber;
    }

    public void setCrawlNumber(String crawlNumber) {
        this.crawlNumber = crawlNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCrawlStopTime() {
        return crawlStopTime;
    }

    public void setCrawlStopTime(String crawlStopTime) {
        this.crawlStopTime = crawlStopTime;
    }

    public int getDocChannel() {
        return docChannel;
    }

    public void setDocChannel(int docChannel) {
        this.docChannel = docChannel;
    }

    public String getCrawlResourcesName() {
        return crawlResourcesName;
    }

    public void setCrawlResourcesName(String crawlResourcesName) {
        this.crawlResourcesName = crawlResourcesName;
    }

    public String getCrawlResourcesNum() {
        return crawlResourcesNum;
    }

    public void setCrawlResourcesNum(String crawlResourcesNum) {
        this.crawlResourcesNum = crawlResourcesNum;
    }

    public String getCrawlStartTime() {
        return crawlStartTime;
    }

    public void setCrawlStartTime(String crawlStartTime) {
        this.crawlStartTime = crawlStartTime;
    }

    public double getCrawlPeriod() {
        return crawlPeriod;
    }

    public void setCrawlPeriod(double crawlPeriod) {
        this.crawlPeriod = crawlPeriod;
    }

    public int getCrawlThreadNum() {
        return crawlThreadNum;
    }

    public void setCrawlThreadNum(int crawlThreadNum) {
        this.crawlThreadNum = crawlThreadNum;
    }

    public String getCrawlResourcesFrom() {
        return crawlResourcesFrom;
    }

    public void setCrawlResourcesFrom(String crawlResourcesFrom) {
        this.crawlResourcesFrom = crawlResourcesFrom;
    }

    public String getCrawlResourcesLevel() {
        return crawlResourcesLevel;
    }

    public void setCrawlResourcesLevel(String crawlResourcesLevel) {
        this.crawlResourcesLevel = crawlResourcesLevel;
    }

    public int getCrawlStatus() {
        return crawlStatus;
    }

    public void setCrawlStatus(int crawlStatus) {
        this.crawlStatus = crawlStatus;
    }

    public String getCrawlCreateBy() {
        return crawlCreateBy;
    }

    public void setCrawlCreateBy(String crawlCreateBy) {
        this.crawlCreateBy = crawlCreateBy;
    }

    public String getCrawlClass() {
        return crawlClass;
    }

    public void setCrawlClass(String crawlClass) {
        this.crawlClass = crawlClass;
    }

    public int getNeedCheckPageNum() {
        return needCheckPageNum;
    }

    public void setNeedCheckPageNum(int needCheckPageNum) {
        this.needCheckPageNum = needCheckPageNum;
    }
}





