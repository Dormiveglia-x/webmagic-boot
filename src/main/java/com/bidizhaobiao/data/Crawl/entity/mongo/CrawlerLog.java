package com.bidizhaobiao.data.Crawl.entity.mongo;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.*;
import java.util.Date;

/**
 * @author 作者: 廉建林
 * @version 创建时间：2018年9月14日 上午10:02:54
 * mongoDB  用来存储爬虫监控信息
 */
@Document(collection ="BXKC_CRAWLER_LOG_DETAIL")
@Entity
public class CrawlerLog {
    @Id
    private String  id;
    @Field("CRAWL_RESOURCESNUMBER")
    private String crawlResourcesNum;
    @Field("CRAWL_STARTTIME")
    private Date crawlStartTime;
    @Field("CRAWL_ENDTIME")
    private Date crawlEndTime;
    @Field("CRAWL_SUCCESS")
    private int crawlSuccess;
    @Field("CRAWL_CLASS")
    private String crawlClass;
    @Field("CRAWL_ERROR")
    private int crawlError;
    @Field("CRAWL_RESULT")
    private String crawlResult;
    @Field("CRAWL_CREATEBY")
    private String crawlCreateBy;
    @Field("CRAWL_CONTROLUUID")
    private String crawlLogUuid;
    @Field("TIMER_NAME")
    private String timerName;
    @Field("ERROR_TYPE")
    //0:程序异常 1：链接或者内容不符合要求
    private String errorType;

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }
    public String getTimerName() {
        return timerName;
    }

    public void setTimerName(String timerName) {
        this.timerName = timerName;
    }

    public String getCrawlLogUuid() {
        return crawlLogUuid;
    }

    public void setCrawlLogUuid(String crawlLogUuid) {
        this.crawlLogUuid = crawlLogUuid;
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

    public Date getCrawlStartTime() {
        return crawlStartTime;
    }

    public void setCrawlStartTime(Date crawlStartTime) {
        this.crawlStartTime = crawlStartTime;
    }

    public Date getCrawlEndTime() {
        return crawlEndTime;
    }

    public void setCrawlEndTime(Date crawlEndTime) {
        this.crawlEndTime = crawlEndTime;
    }

    public String getCrawlResult() {
        return crawlResult;
    }

    public void setCrawlResult(String crawlResult) {
        this.crawlResult = crawlResult;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCrawlResourcesNum() {
        return crawlResourcesNum;
    }

    public void setCrawlResourcesNum(String crawlResourcesNum) {
        this.crawlResourcesNum = crawlResourcesNum;
    }

    public int getCrawlSuccess() {
        return crawlSuccess;
    }

    public void setCrawlSuccess(int crawlSuccess) {
        this.crawlSuccess = crawlSuccess;
    }

    public int getCrawlError() {
        return crawlError;
    }

    public void setCrawlError(int crawlError) {
        this.crawlError = crawlError;
    }


}





