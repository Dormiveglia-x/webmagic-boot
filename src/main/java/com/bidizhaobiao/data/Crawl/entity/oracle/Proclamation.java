package com.bidizhaobiao.data.Crawl.entity.oracle;

import java.util.Date;

/**
 * @author 作者: 廉建林
 * @version 创建时间：2018年8月9日 上午11:07:54
 * 类说明 :公告实体类
 */
public class Proclamation {

    private String webSourceNo;
    private String area;
    private String province;
    private String city;
    private String webSourceName;
    private String infoSource;
    private String infoType;
    private String industry;
    private String recordId;
    private String id;
    private String pageTitle;
    private String pageTime;
    private String pageContent;
    private String pageAttachments;
    private Date createTime;
    private String district;
    private String detailLink;
    private String attachmentPath;

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    public String getDetailLink() {
        return detailLink;
    }

    public void setDetailLink(String detailLink) {
        this.detailLink = detailLink;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getWebSourceNo() {
        return webSourceNo;
    }

    public void setWebSourceNo(String webSourceNo) {
        this.webSourceNo = webSourceNo;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getWebSourceName() {
        return webSourceName;
    }

    public void setWebSourceName(String webSourceName) {
        this.webSourceName = webSourceName;
    }

    public String getInfoSource() {
        return infoSource;
    }

    public void setInfoSource(String infoSource) {
        this.infoSource = infoSource;
    }

    public String getInfoType() {
        return infoType;
    }

    public void setInfoType(String infoType) {
        this.infoType = infoType;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public String getPageTime() {
        return pageTime;
    }

    public void setPageTime(String pageTime) {
        this.pageTime = pageTime;
    }

    public String getPageContent() {
        return pageContent;
    }

    public void setPageContent(String pageContent) {
        this.pageContent = pageContent;
    }

    public String getPageAttachments() {
        return pageAttachments;
    }

    public void setPageAttachments(String pageAttachments) {
        this.pageAttachments = pageAttachments;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (this == obj) {
            isEqual = true;
        } else {
            if (obj instanceof com.bidizhaobiao.data.Crawl.entity.oracle.Proclamation) {
                com.bidizhaobiao.data.Crawl.entity.oracle.Proclamation zbdy = (com.bidizhaobiao.data.Crawl.entity.oracle.Proclamation) obj;
                if (this.recordId != null && this.recordId.equals(zbdy.getRecordId())) {
                    if (this.pageTitle != null && this.pageTitle.equals(zbdy.getPageTitle())) {
                        isEqual = true;
                    }
                }
            }
        }
        return isEqual;
    }

}
