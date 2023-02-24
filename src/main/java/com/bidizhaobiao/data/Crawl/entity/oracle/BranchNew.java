package com.bidizhaobiao.data.Crawl.entity.oracle;

/**
 * @author 作者: 廉建林
 * @version 创建时间：2018年9月14日 上午10:02:54
 * 用来存储webmagic列表页参数
 */
public class BranchNew {

    private String title;
    private String id;
    private String date;
    private String tablename;
    private String link;
    private String content;
    //列表页获取的详情链接
    private String detailLink;
    //区域
    private String area;
    //省份
    private String province;
    //城市
    private String city;
    //区县
    private String district;

    /*标识数据需要入库的类型
        1：既需要入招投标信息表，又需要入公告信息表
        2：不需要入招投标信息表，需要入公告信息表
        3：只需要入招投标信息表，不需要入公告信息表
    */
    private int type = 1;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    public String getDetailLink() {
        return detailLink;
    }

    public void setDetailLink(String detailLink) {
        this.detailLink = detailLink;
    }

    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getDate() {
        return date;
    }


    public void setDate(String date) {
        this.date = date;
    }


    public String getTablename() {
        return tablename;
    }


    public void setTablename(String tablename) {
        this.tablename = tablename;
    }


    public String getLink() {
        return link;
    }


    public void setLink(String link) {
        this.link = link;
    }


    public String getContent() {
        return content;
    }


    public void setContent(String content) {
        this.content = content;
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

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String toString() {
        return "BranchNew[ title=" + title + ";id=" + id + ";date=" + date + ";]";
    }
}
