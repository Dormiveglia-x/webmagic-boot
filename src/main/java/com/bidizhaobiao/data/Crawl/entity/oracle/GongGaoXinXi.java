package com.bidizhaobiao.data.Crawl.entity.oracle;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author 作者: 廉建林
 * @version 创建时间：2018年9月14日 上午10:02:54
 * 用来广联达入库信息的实体
 */
@Table(name = "bxkc.GONG_GAO_XIN_XI")
@Entity
public class GongGaoXinXi {
    @Id
    @Column(name = "id")
    private String id;
    @Column(name = "DETAIL_TITLE")
    private String detailTitle;
    @Column(name = "DETAIL_HTML")
    private String detailHtml;
    @Column(name = "DETAIL_LINK")
    private String detailLink;
    @Column(name = "DETAIL_DDID")
    private String detailDdid;
    @Column(name = "DOCCHANNEL")
    private int docChannel;
    @Column(name = "TASK_TYPE")
    private String taskType;
    @Column(name = "TASK_NAME")
    private String taskName;
    @Column(name = "DOC_ID")
    private String docId;
    @Column(name = "CREATETIME")
    private String createTime;
    @Column(name = "LIST_TITLE")
    private String listTitle;
    @Column(name = "POST_RESULT")
    private String postResult;
    @Column(name = "WEB_SOURCE_NO")
    private String webResourceNum;
    @Column(name = "PAGE_TIME")
    private String pageTime;
    @Column(name = "PUSH_TIME")
    private String pushTime;

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getPostResult() {
        return postResult;
    }

    public void setPostResult(String postResult) {
        this.postResult = postResult;
    }

    public String getPushTime() {
        return pushTime;
    }

    public void setPushTime(String pushTime) {
        this.pushTime = pushTime;
    }

    public String getPageTime() {
        return pageTime;
    }

    public void setPageTime(String pageTime) {
        this.pageTime = pageTime;
    }

    public String getWebResourceNum() {
        return webResourceNum;
    }

    public void setWebResourceNum(String webResourceNum) {
        this.webResourceNum = webResourceNum;
    }

    public String getListTitle() {
        return listTitle;
    }

    public void setListTitle(String listTitle) {
        this.listTitle = listTitle;
    }

    public String getDetailLink() {
        return detailLink;
    }

    public void setDetailLink(String detailLink) {
        this.detailLink = detailLink;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDetailTitle() {
        return detailTitle;
    }

    public void setDetailTitle(String detailTitle) {
        this.detailTitle = detailTitle;
    }

    public String getDetailHtml() {
        return detailHtml;
    }

    public void setDetailHtml(String detailHtml) {
        this.detailHtml = detailHtml;
    }

    public String getDetailDdid() {
        return detailDdid;
    }

    public void setDetailDdid(String detailDdid) {
        this.detailDdid = detailDdid;
    }

    public int getDocChannel() {
        return docChannel;
    }

    public void setDocChannel(int docChannel) {
        this.docChannel = docChannel;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
}
