package com.bidizhaobiao.data.Crawl.service;

import com.bidizhaobiao.data.Crawl.entity.mongo.CrawlerConfig;
import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.utils.SpecialUtil;
import org.apache.http.HttpHost;
import org.json.JSONArray;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import us.codecraft.webmagic.Spider;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/*
用于定义爬虫过程中需要修改和更新的参数
 */
public class ServiceContext {
    //设置网页时间临界点
    final private String splitPointStr = "2016-01-01";
    // 初始化是否存在超过长度限制的公告详情的标识变量为false
    private boolean hasOverLengthContent = false;
    // 初始化是否存在空标题的标识标量为false
    private boolean hasNullTitle = false;
    // 初始化是否存在空标题的标识标量为false
    private boolean hasNullRecordId = false;
    // 以下对象用于记录带待校验的列表
    private List<BranchNew> checkList = new ArrayList<BranchNew>();
    // 用来记录redis保存的recordId
    private List<String> recordIdsInfoRedis = new ArrayList<>();
    // 用来记录当前接口前3页的recordId
    private List<String> recordIdsInfoList = new ArrayList<>();
    // 用来记录当前接口前3页的recordId
    LinkedList<String> linkedList = new LinkedList<String>();

    public List<String> getRecordIdsInfoRedis() {
        return recordIdsInfoRedis;
    }

    public void setRecordIdsInfoRedis(List<String> recordIdsInfoRedis) {
        this.recordIdsInfoRedis = recordIdsInfoRedis;
    }

    public List<String> getRecordIdsInfoList() {
        return recordIdsInfoList;
    }

    public void setRecordIdsInfoList(List<String> recordIdsInfoList) {
        this.recordIdsInfoList = recordIdsInfoList;
    }

    public LinkedList<String> getLinkedList() {
        return linkedList;
    }

    public void setLinkedList(LinkedList<String> linkedList) {
        this.linkedList = linkedList;
    }

    // 以下对象用于记录是否需要爬取
    private boolean needCrawl = true;
    //用于记录是否存在多个页面的数据都已经存在
    private int hasDataPageNum = 0;
    //用于记录是否存在连续10条错误
    private int hasErrorNum = 0;
    //用于记录存在连续5条空标题或者空id
    private int hasNullTitleOrNullIdErrorNum = 0;
    // 记录当前爬虫的错误页数
    private int errorPage = 1;
    // 记录当前爬虫的成功数
    private int successNum = 0;
    // 记录当前爬虫的失败数
    private int errorNum = 0;
    // 以下对象用于记录当前的页码
    private int pageNum = 1;
    // 记录当前爬虫的最大页
    private int maxPage = 1;
    private int flag = 0;
    private Spider spider;
    //判断爬虫是增量还是全量
    private int crawlType = 0;
    // 初始化“是否已到达timeSplitPoint以前的数据”的标识变量为false
    private boolean hasReachedTooOldData = false;
    //爬虫启动时间
    private Date startTime;
    //爬虫结束时间
    private Date endTime;
    // 记录当前广联达入库的成功数
    private int gSuccessNum = 0;
    //是否需要入GONG_GAO_XIN_XI ;
    private boolean isNeedInsertGonggxinxi;
    //站源类型
    private String taskType;
    //站源名称
    private String taskName;
    //记录定时器启动的uuid
    private String controlUuid = null;
    //判断广联达编号校验的时候是否遇到19年之前的数据
    private boolean isExistOldData = false;
    private String crawlCreateBy;
    private String crawlCreateTime;
    private PhantomJSDriver driver = null;
    private Date timeSplitPoint;
    private String name = null;
    private String tableName = null;
    private String sourceNum = null;
    private String sourceName = null;
    private String infoSource = null;
    private String area = null;
    private String city = null;
    private String province = null;
    private String district = null;
    private Date proxyExpireDate = null;

    private boolean isSaveFileAddRef = false;
    private boolean isSaveFileAddSSL = false;

    public Date getProxyExpireDate() {
        return proxyExpireDate;
    }

    public void setProxyExpireDate(Date proxyExpireDate) {
        this.proxyExpireDate = proxyExpireDate;
    }

    public boolean isSaveFileAddSSL() {
        return isSaveFileAddSSL;
    }

    public void setSaveFileAddSSL(boolean isSaveFileAddSSL) {
        this.isSaveFileAddSSL = isSaveFileAddSSL;
    }

    public boolean isSaveFileAddRef() {
        return isSaveFileAddRef;
    }

    public void setSaveFileAddRef(boolean isSaveFileAddRef) {
        this.isSaveFileAddRef = isSaveFileAddRef;
    }

    //是否需要保存附件
    private boolean isSaveFile = true;
    //传递网站域名到download
    private String baseUrl = "https://www.youzhicai.com/";
    //附件下载异常的个数
    private int errorSaveFileNum = 0;
    //文件所在的war名字
    private String warName = null;
    //是否需要校验含有iframe标签
    private boolean isNeedCheckIframe = true;
    //是否需要代理
    private boolean isNeedProxy = false;
    //用于存储爬虫的配置表，防止查询超时
    private CrawlerConfig crawlerConfig = new CrawlerConfig();
    //用于存储爬虫执行过程中的报错
    private JSONArray errorJsonArray = new JSONArray();
    //是否还需要继续插入邮件监控表
    private boolean isNeedInsertCheckError = true;
    //    //上一次errorRecordId有没有历史记录的地方
//    private boolean hasOldErrorInfo = false;
    private String fristRecordId = null;
    //存储在redis中的errorRecordId
    private String errorRecordId = null;
    //存储在正常爬虫启动的上一次recordId
    private String normalRecordId = null;
    //用来存储当前的recordId
    private String currentRecord = null;
    //当存在errorRecordId的情况下是否还要判断
    private boolean needCheckRecordId = true;
    //判断是否到达上次记录的地方
    private boolean hasReach = false;
    //标识超过recordId的页码
    private int reachRecordIdNum = 1;
    //当前报错的recordId
    //用来存储当前报错的recordId
    private String currentErrorRecord = null;
    //记录是否已经记录了recordId
    private boolean isRecord = false;
    //标识超过recordId的页码
    private int checkListPageNum = 1;

    //需要校验的页数
    private int needCheckPageNum = 0;
    //当前的proxy
    private HttpHost proxyInfo = null;

    public int getCheckListPageNum() {
        return checkListPageNum;
    }

    public void setCheckListPageNum(int checkListPageNum) {
        this.checkListPageNum = checkListPageNum;
    }

    public CrawlerConfig getCrawlerConfig() {
        return crawlerConfig;
    }

    public void setCrawlerConfig(CrawlerConfig crawlerConfig) {
        this.crawlerConfig = crawlerConfig;
    }

    public HttpHost getProxyInfo() {
        return proxyInfo;
    }

    public void setProxyInfo(HttpHost proxyInfo) {
        this.proxyInfo = proxyInfo;
    }

    public boolean isRecord() {
        return isRecord;
    }

    public void setRecord(boolean record) {
        isRecord = record;
    }

    public String getCurrentErrorRecord() {
        return currentErrorRecord;
    }

    public void setCurrentErrorRecord(String currentErrorRecord) {
        this.currentErrorRecord = currentErrorRecord;
    }

    public int getReachRecordIdNum() {
        return reachRecordIdNum;
    }

    public void setReachRecordIdNum(int reachRecordIdNum) {
        this.reachRecordIdNum = reachRecordIdNum;
    }

    public boolean isHasReach() {
        return hasReach;
    }

    public void setHasReach(boolean hasReach) {
        this.hasReach = hasReach;
    }

    public String getFristRecordId() {
        return fristRecordId;
    }

    public void setFristRecordId(String fristRecordId) {
        this.fristRecordId = fristRecordId;
    }


    public boolean isNeedCheckRecordId() {
        return needCheckRecordId;
    }

    public void setNeedCheckRecordId(boolean needCheckRecordId) {
        this.needCheckRecordId = needCheckRecordId;
    }

    public String getCurrentRecord() {
        return currentRecord;
    }

    public void setCurrentRecord(String currentRecord) {
        this.currentRecord = currentRecord;
    }

    public String getErrorRecordId() {
        return errorRecordId;
    }

    public void setErrorRecordId(String errorRecordId) {
        this.errorRecordId = errorRecordId;
    }

    public String getNormalRecordId() {
        return normalRecordId;
    }

    public void setNormalRecordId(String normalRecordId) {
        this.normalRecordId = normalRecordId;
    }

    public boolean isNeedInsertCheckError() {
        return isNeedInsertCheckError;
    }

    public void setNeedInsertCheckError(boolean needInsertCheckError) {
        isNeedInsertCheckError = needInsertCheckError;
    }

    public JSONArray getErrorJsonArray() {
        return errorJsonArray;
    }

    public void setErrorJsonArray(JSONArray errorJsonArray) {
        this.errorJsonArray = errorJsonArray;
    }

    public boolean isNeedProxy() {
        return isNeedProxy;
    }

    public void setNeedProxy(boolean needProxy) {
        isNeedProxy = needProxy;
    }

    public boolean isNeedCheckIframe() {
        return isNeedCheckIframe;
    }

    public void setNeedCheckIframe(boolean needCheckIframe) {
        isNeedCheckIframe = needCheckIframe;
    }

    public String getWarName() {
        return warName;
    }

    public void setWarName(String warName) {
        this.warName = warName;
    }

    public int getErrorSaveFileNum() {
        return errorSaveFileNum;
    }

    public void setErrorSaveFileNum(int errorSaveFileNum) {
        this.errorSaveFileNum = errorSaveFileNum;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public boolean isSaveFile() {
        return isSaveFile;
    }

    public void setSaveFile(boolean saveFile) {
        isSaveFile = saveFile;
    }

    {
        try {
            timeSplitPoint = SpecialUtil.str2Date(splitPointStr);
        } catch (ParseException e) {

        }
    }

    public boolean isHasNullTitle() {
        return hasNullTitle;
    }

    public void setHasNullTitle(boolean hasNullTitle) {
        this.hasNullTitle = hasNullTitle;
    }

    public boolean isHasNullRecordId() {
        return hasNullRecordId;
    }

    public void setHasNullRecordId(boolean hasNullRecordId) {
        this.hasNullRecordId = hasNullRecordId;
    }

    public String getControlUuid() {
        return controlUuid;
    }

    public void setControlUuid(String controlUuid) {
        this.controlUuid = controlUuid;
    }

    public boolean isExistOldData() {
        return isExistOldData;
    }

    public void setExistOldData(boolean existOldData) {
        isExistOldData = existOldData;
    }

    public int getHasErrorNum() {
        return hasErrorNum;
    }

    public void setHasErrorNum(int hasErrorNum) {
        this.hasErrorNum = hasErrorNum;
    }

    public int getHasNullTitleOrNullIdErrorNum() {
        return hasNullTitleOrNullIdErrorNum;
    }

    public void setHasNullTitleOrNullIdErrorNum(int hasNullTitleOrNullIdErrorNum) {
        this.hasNullTitleOrNullIdErrorNum = hasNullTitleOrNullIdErrorNum;
    }

    public int gethasErrorNum() {
        return hasErrorNum;
    }

    public void sethasErrorNum(int hasErrorNum) {
        this.hasErrorNum = hasErrorNum;
    }

    public boolean isNeedInsertGonggxinxi() {
        return isNeedInsertGonggxinxi;
    }

    public void setNeedInsertGonggxinxi(boolean needInsertGonggxinxi) {
        isNeedInsertGonggxinxi = needInsertGonggxinxi;
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

    public int getgSuccessNum() {
        return gSuccessNum;
    }

    public void setgSuccessNum(int gSuccessNum) {
        this.gSuccessNum = gSuccessNum;
    }

    public PhantomJSDriver getDriver() {
        return driver;
    }

    public void setDriver(PhantomJSDriver driver) {
        this.driver = driver;
    }

    public String getCrawlCreateTime() {
        return crawlCreateTime;
    }

    public void setCrawlCreateTime(String crawlCreateTime) {
        this.crawlCreateTime = crawlCreateTime;
    }

    public String getCrawlCreateBy() {
        return crawlCreateBy;
    }

    public void setCrawlCreateBy(String crawlCreateBy) {
        this.crawlCreateBy = crawlCreateBy;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getCrawlType() {
        return crawlType;
    }

    public void setCrawlType(int crawlType) {
        this.crawlType = crawlType;
    }

    public int getHasDataPageNum() {
        return hasDataPageNum;
    }

    public void setHasDataPageNum(int hasDataPageNum) {
        this.hasDataPageNum = hasDataPageNum;
    }

    public Spider getSpider() {
        return spider;
    }

    public void setSpider(Spider spider) {
        this.spider = spider;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSourceNum() {
        return sourceNum;
    }

    public void setSourceNum(String sourceNum) {
        this.sourceNum = sourceNum;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getInfoSource() {
        return infoSource;
    }

    public void setInfoSource(String infoSource) {
        this.infoSource = infoSource;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public int getErrorPage() {
        return errorPage;
    }

    public void setErrorPage(int errorPage) {
        this.errorPage = errorPage;
    }

    public int getSuccessNum() {
        return successNum;
    }

    public void setSuccessNum(int successNum) {
        this.successNum = successNum;
    }

    public int getErrorNum() {
        return errorNum;
    }

    public void setErrorNum(int errorNum) {
        this.errorNum = errorNum;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public void setMaxPage(int maxPage) {
        this.maxPage = maxPage;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getSplitPointStr() {
        return splitPointStr;
    }

    public Date getTimeSplitPoint() {
        return timeSplitPoint;
    }

    public void setTimeSplitPoint(Date timeSplitPoint) {
        this.timeSplitPoint = timeSplitPoint;
    }

    public boolean isNeedCrawl() {
        return needCrawl;
    }

    public void setNeedCrawl(boolean needCrawl) {
        this.needCrawl = needCrawl;
    }

    public List<BranchNew> getCheckList() {
        return checkList;
    }

    public void setCheckList(List<BranchNew> checkList) {
        this.checkList = checkList;
    }


    public boolean isHasOverLengthContent() {
        return hasOverLengthContent;
    }

    public void setHasOverLengthContent(boolean hasOverLengthContent) {
        this.hasOverLengthContent = hasOverLengthContent;
    }

    public boolean isHasReachedTooOldData() {
        return hasReachedTooOldData;
    }

    public void setHasReachedTooOldData(boolean hasReachedTooOldData) {
        this.hasReachedTooOldData = hasReachedTooOldData;
    }

    public int getNeedCheckPageNum() {
        return needCheckPageNum;
    }

    public void setNeedCheckPageNum(int needCheckPageNum) {
        this.needCheckPageNum = needCheckPageNum;
    }

    //初始化参数信息,避免数据遗漏
    public void serviceContextInitParam() {
        // 初始化是否存在超过长度限制的公告详情的标识变量为false
        hasOverLengthContent = false;
        hasNullTitle = false;
        // 初始化是否存在空标题的标识标量为false
        hasNullRecordId = false;

        // 以下对象用于记录是否需要爬取
        needCrawl = true;
        // 记录当前爬虫的错误页数
        errorPage = 0;
        // 记录当前爬虫的成功数
        successNum = 0;
        // 记录当前爬虫的失败数
        errorNum = 0;
        //记录连续存在报错的个数
        hasErrorNum = 0;
        //记录连续存在的空标题或者空id
        hasNullTitleOrNullIdErrorNum = 0;
        gSuccessNum = 0;
        flag = 0;
        hasDataPageNum = 0;
        pageNum = 1;
        crawlType = 0;
        startTime = null;
        endTime = null;
        crawlCreateBy = null;
        crawlCreateTime = null;
        controlUuid = null;
        isExistOldData = false;
        errorSaveFileNum = 0;
        warName = null;
        isNeedCheckIframe = true;
        isSaveFile = true;
        isSaveFileAddSSL = false;
        isSaveFileAddRef = false;
        isNeedProxy = false;
        errorJsonArray = new JSONArray();
        recordIdsInfoRedis = new ArrayList<>();
        recordIdsInfoList = new ArrayList<>();
        linkedList = new LinkedList<String>();
        //存储在redis中的errorRecordId
        errorRecordId = null;
        //存储在正常爬虫启动的上一次recordId
        normalRecordId = null;
        fristRecordId = null;
        currentRecord = null;
        needCheckRecordId = true;
        hasReach = false;
        reachRecordIdNum = 1;
        currentErrorRecord = null;
        hasReachedTooOldData = false;
        isRecord = false;
        proxyInfo = null;
        checkListPageNum = 1;
        needCheckPageNum = 0;
    }

}
