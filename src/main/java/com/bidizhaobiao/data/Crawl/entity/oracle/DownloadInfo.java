package com.bidizhaobiao.data.Crawl.entity.oracle;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

/**
 * @author 作者: 廉建林
 * @version 创建时间：2018年9月14日 上午10:02:54
 * 用来存储附件下载的信息
 */
@Table(name = "bxkc.T_DOWNLOAD_INFO")
@GenericGenerator(name = "jpa-uuid", strategy = "uuid")
@EntityListeners(AuditingEntityListener.class)
@Entity
public class DownloadInfo {
    @Id
    @GeneratedValue(generator = "jpa-uuid")
    @Column(name = "id")
    private String id;
    @Column(name = "UUID")
    private String uuid;
    @Column(name = "WEB_SOURCE_NO")
    private String webSourceNo;
    @Column(name = "DOCCHANNEL")
    private int docChannel;
    @Column(name = "RECORD_ID")
    private String recordId;
    @Column(name = "OLD_LINK")
    private String oldLink;
    @Column(name = "NEW_LINK")
    private String newLink;
    @Column(name = "ERROR_INFO")
    private String errorInfo;
    @CreatedDate
    @Column(name = "CREATE_TIME")
    private Date createTime;
    @Column(name = "FILE_INFO")
    private String fileInfo;

    public String getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(String fileInfo) {
        this.fileInfo = fileInfo;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getWebSourceNo() {
        return webSourceNo;
    }

    public void setWebSourceNo(String webSourceNo) {
        this.webSourceNo = webSourceNo;
    }

    public int getDocChannel() {
        return docChannel;
    }

    public void setDocChannel(int docChannel) {
        this.docChannel = docChannel;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getOldLink() {
        return oldLink;
    }

    public void setOldLink(String oldLink) {
        this.oldLink = oldLink;
    }

    public String getNewLink() {
        return newLink;
    }

    public void setNewLink(String newLink) {
        this.newLink = newLink;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }
}
