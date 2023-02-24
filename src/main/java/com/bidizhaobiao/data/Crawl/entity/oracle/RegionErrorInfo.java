package com.bidizhaobiao.data.Crawl.entity.oracle;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

/**
 * @author 作者: 陆浩锐
 * @version 创建时间：2022年11月29日
 * 用来存储地区信息
 */
@Table(name = "bxkc.REGION_ERROR_INFO")
@GenericGenerator(name = "jpa-uuid", strategy = "uuid")
@EntityListeners(AuditingEntityListener.class)
@Entity
public class RegionErrorInfo {
    @Id
    @GeneratedValue(generator = "jpa-uuid")
    @Column(name = "ID")
    private String id;
    @Column(name = "WEB_SOURCE_NO")
    private String webSourceNo;
    @Column(name = "RRECORD_ID")
    private String recordId;
    @Column(name = "PAGE_TITLE")
    private String pageTitle;
    @Column(name = "DETAIL_LINK")
    private String detailLink;
    @Column(name = "ERROR_INFO")
    private String errorInfo;
    @Column(name = "CREATE_TIME")
    private Date createTime;
    @Column(name = "ERROR_TIME")
    private String errorTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWebSourceNo() {
        return webSourceNo;
    }

    public void setWebSourceNo(String webSourceNo) {
        this.webSourceNo = webSourceNo;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public String getDetailLink() {
        return detailLink;
    }

    public void setDetailLink(String detailLink) {
        this.detailLink = detailLink;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getErrorTime() {
        return errorTime;
    }

    public void setErrorTime(String errorTime) {
        this.errorTime = errorTime;
    }
}
