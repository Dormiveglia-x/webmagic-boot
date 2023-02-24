package com.bidizhaobiao.data.Crawl.entity.mongo;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.*;
import java.util.Date;

/**
 * @author 作者: 廉建林
 * @version 创建时间：2018年9月14日 上午11:43:17
 * mongoDB  用来存储定时器启动日志信息
 */
@Document(collection ="BXKC_TIMER_LOG_DETAIL")
@Entity
public class CrawlerTimerLog {
    @Id
    private   String  id;
    @Field("TIMER_NAME")
    private String timerName;
    @Field("TIMER_STARTTIME")
    private Date timerStartTime;
    @Field("TIMER_ENDTIME")
    private Date timerEndTime;
    @Field("TIMER_TYPE")
    private String timerType;
    @Field("TIMER_CONTROLUUID")
    private String timerControlUuid;
    @Field("TIMER_TOTALCOUNT")
    private int timerTotalCount;
    @Field("TIMER_NEEDSTARTCOUNT")
    private int timerNeedStartCount;
    public int getTimerTotalCount() {
        return timerTotalCount;
    }

    public void setTimerTotalCount(int timerTotalCount) {
        this.timerTotalCount = timerTotalCount;
    }

    public int getTimerNeedStartCount() {
        return timerNeedStartCount;
    }

    public void setTimerNeedStartCount(int timerNeedStartCount) {
        this.timerNeedStartCount = timerNeedStartCount;
    }

    public String getTimerControlUuid() {
        return timerControlUuid;
    }

    public void setTimerControlUuid(String timerControlUuid) {
        this.timerControlUuid = timerControlUuid;
    }

    public String getTimerType() {
        return timerType;
    }

    public void setTimerType(String timerType) {
        this.timerType = timerType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimerName() {
        return timerName;
    }

    public void setTimerName(String timerName) {
        this.timerName = timerName;
    }

    public Date getTimerStartTime() {
        return timerStartTime;
    }

    public void setTimerStartTime(Date timerStartTime) {
        this.timerStartTime = timerStartTime;
    }

    public Date getTimerEndTime() {
        return timerEndTime;
    }

    public void setTimerEndTime(Date timerEndTime) {
        this.timerEndTime = timerEndTime;
    }
}





