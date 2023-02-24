package com.bidizhaobiao.data.Crawl.service;

import com.bidizhaobiao.data.Crawl.dao.mongo.CrawlerConfigDao;
import com.bidizhaobiao.data.Crawl.dao.mongo.CrawlerLogDao;
import com.bidizhaobiao.data.Crawl.dao.oracle.*;
import com.bidizhaobiao.data.Crawl.entity.mongo.CrawlerConfig;
import com.bidizhaobiao.data.Crawl.entity.mongo.CrawlerLog;
import com.bidizhaobiao.data.Crawl.entity.oracle.*;
import com.bidizhaobiao.data.Crawl.utils.*;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Spider;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author 作者: 廉建林
 * @version 创建时间：2018年9月25日 下午3:57:51 类说明,定义父类,提取爬虫需要的公共类方法
 */
@Service
@PropertySource({"classpath:application.properties"})
public abstract class SpiderService implements ISpiderService {
    public static final Logger logger = LoggerFactory.getLogger(SpiderService.class);
    public static String startTime;//爬虫定时器名字
    public static Map<String, BranchNew> map = new HashMap<String, BranchNew>();
    public static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    protected static ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            // TODO Auto-generated method stubs
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };
    @Autowired
    public CrawlerLogDao crawlerLogDao;
    @Autowired
    public ProclamationDao proclamationDao;
    @Autowired
    public CrawlerConfigDao crawlerConfigDao;
    @Autowired
    public GongGaoXinXiDao gongGaoXinXiDao;
    @Autowired
    public DownloadInfoDao downloadInfoDao;
    @Autowired
    public StringRedisTemplate stringRedisTemplate;
    @Autowired
    AreaDao areaDao;
    @Autowired
    RegionErrorInfoDao regionErrorInfoDao;
    @Value("${spring.project.warName}")
    public String warName;//爬虫定时器名字
    @Value("#{${spring.page.maxPageNum}}")
    public Integer maxPageNum;//最大页数
    public ServiceContext serviceContext = new ServiceContext();

    @Value("${spring.guanglianda.startDate}")
    public void setApi(String db) {
        startTime = db;
    }

    @Value("${imgPath}")
    public String imgPath;//爬虫下载图片的地址
    SaveFileUtils saveFileUtils = new SaveFileUtils();

    // 启动爬虫
    public abstract void startCrawl(int threadNum, int crawlType);

    //serviceContext赋值
    public void serviceContextEvaluation() {
        try {
            String conUuid = serviceContext.getControlUuid();
            CrawlerConfig crawlerConfig = serviceContext.getCrawlerConfig();
            //初始化数据
            serviceContext.serviceContextInitParam();
            //赋值
            serviceContext.setControlUuid(conUuid);
            serviceContext.setCrawlerConfig(crawlerConfig);
            serviceContext.setWarName(warName);
            //初始化serviceContext参数
            String name = this.getClass().getSimpleName();
            //获取需要校验的页数
            int needCheckPageNum = crawlerConfig.getNeedCheckPageNum();
            serviceContext.setNeedCheckPageNum(needCheckPageNum);
            logger.info("获取到{}需要校验的页数为：{}", name, needCheckPageNum);
            //获取Redis里记录的recordId
            List<String> allRecordIds = stringRedisTemplate.opsForList().range(name + "_RecordIdsInfo", 0, -1);
            logger.info("{}取到的集合为：{}", name + "_RecordIdsInfo", allRecordIds.size());
            serviceContext.setRecordIdsInfoRedis(allRecordIds);
            SpiderService spiderService = (SpiderService) SpringContextUtil.getBean(name);
            name = name.substring(name.lastIndexOf("_") + 1);
            serviceContext.setName(this.getClass().getSimpleName());
            String tableName = SpecialUtil.getTableName(name);
            serviceContext.setTableName(tableName);
            //获取该类下面的参数
            Field[] fields = spiderService.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {//遍历
                fields[i].setAccessible(true);
                //获取属性
                String fieldsName = fields[i].getName();
                switch (fieldsName) {
                    case "sourceNum":
                        String sourceNum = (String) fields[i].get(spiderService);
                        serviceContext.setSourceNum(sourceNum);
                        break;
                    case "sourceName":
                        String sourceName = (String) fields[i].get(spiderService);
                        serviceContext.setSourceName(sourceName);
                        break;
                    case "infoSource":
                        String infoSource = (String) fields[i].get(spiderService);
                        serviceContext.setInfoSource(infoSource);
                        break;
                    case "createBy":
                        String createBy = (String) fields[i].get(spiderService);
                        serviceContext.setCrawlCreateBy(createBy);
                        break;
                    case "area":
                        String area = (String) fields[i].get(spiderService);
                        serviceContext.setArea(area);
                        break;
                    case "province":
                        String province = (String) fields[i].get(spiderService);
                        serviceContext.setProvince(province);
                        break;
                    case "city":
                        String city = (String) fields[i].get(spiderService);
                        serviceContext.setCity(city);
                        break;
                    case "district":
                        String district = (String) fields[i].get(spiderService);
                        serviceContext.setDistrict(district);
                        break;
                    //修改网站入口链接
                    case "baseUrl":
                        String baseUrl = (String) fields[i].get(spiderService);
                        serviceContext.setBaseUrl(baseUrl);
                        break;
                    //修改下载附件变量
                    case "isNeedSaveFile":
                        boolean isNeedSaveFile = (boolean) fields[i].get(spiderService);
                        serviceContext.setSaveFile(isNeedSaveFile);
                        break;
                    //是否需要校验iframe
                    case "isNeedCheckIframe":
                        boolean isNeedCheckIframe = (boolean) fields[i].get(spiderService);
                        serviceContext.setNeedCheckIframe(isNeedCheckIframe);
                        break;
                    // 修改下载附件变量
                    case "isNeedSaveFileAddRef":
                        boolean isNeedSaveFileAddRef = (boolean) fields[i].get(spiderService);
                        serviceContext.setSaveFileAddRef(isNeedSaveFileAddRef);
                        break;
                    // 修改下载附件变量
                    case "isNeedSaveFileAddSSL":
                        boolean isNeedSaveFileAddSSL = (boolean) fields[i].get(spiderService);
                        serviceContext.setSaveFileAddSSL(isNeedSaveFileAddSSL);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加爬虫启动日志
     *
     * @param serviceContext 用于传递上下文参数的封装对象
     */
    public void saveCrawlLog(ServiceContext serviceContext) {
        try {
            // 以下对象用于记录带待校验的列表,此处清空原始数据
            serviceContext.getCheckList().clear();
            // 修改配置信息
            CrawlerConfig crawlerConfig = serviceContext.getCrawlerConfig();
            if (crawlerConfig != null) {
                int crawlType = crawlerConfig.getCrawlType();
                //如果是全量爬虫，则接着上一次爬取的地方
                if (crawlType == 0 && warName.contains("total")) {
                    if (crawlerConfig.getCrawlPagenum() > 1) {
                        serviceContext.setPageNum(crawlerConfig.getCrawlPagenum() - 1);
                    }
                } else {
                    // 初始化当前的页码
                    serviceContext.setPageNum(1);
                }
                // 如果周期为空，则默认为4
                if (crawlerConfig.getCrawlPeriod() == 0) {
                    crawlerConfig.setCrawlPeriod(4);
                }
            }
            // 记录爬虫启动时间
            serviceContext.setStartTime(new Date());
            logger.info("{}启动日志记录成功===========================", serviceContext.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 监控爬虫状态
     *
     * @param serviceContext 用于传递上下文参数的封装对象
     */
    public void saveCrawlResult(ServiceContext serviceContext) {
        while (true) {
            //判断爬虫是否真正的执行完毕
            if (serviceContext.getSpider().getThreadAlive() == 0) {
                break;
            }
        }
        int errorNum = serviceContext.getErrorNum();
        int successNum = serviceContext.getSuccessNum();
        int crawlType = serviceContext.getCrawlType();
        String className = serviceContext.getName();

        // 修改爬虫下次启动时间以及校验时间
        CrawlerConfig crawlerConfig = serviceContext.getCrawlerConfig();
        int lastSuccessNum = crawlerConfig.getCrawlSuccessNum();
        if (successNum > 0) {
            lastSuccessNum = lastSuccessNum + successNum;
            crawlerConfig.setCrawlSuccessNum(lastSuccessNum);
        }
        //修改爬虫启动时间,下一次启动时间为:(当前时间+周期)
        StringBuilder logMessage = new StringBuilder();
        double period = crawlerConfig.getCrawlPeriod();
        int checkPeriod = crawlerConfig.getCrawlCheckPeriod();
        //若配置表无校验周期，则默认为60个小时
        if (checkPeriod == 0) {
            checkPeriod = 30;
        }
        logger.info("{}执行完毕，当前校验周期为：{}", className, checkPeriod);
        try {
            //记录linkList到redis中的集合recordIdsInfo
            LinkedList<String> linkedList = serviceContext.getLinkedList();
            logger.info("{}需要追加到redis的集合为：{}", serviceContext.getName() + "_RecordIdsInfo", linkedList.size());
            List<String> recordIdsInfoRedis = serviceContext.getRecordIdsInfoRedis();
            int originalRedisSize = recordIdsInfoRedis.size();
            if (recordIdsInfoRedis != null) {
                if (linkedList != null && linkedList.size() > 0) {
                    for (int i = 0; i < linkedList.size(); i++) {
                        if (!recordIdsInfoRedis.contains(linkedList.get(i))) {
                            recordIdsInfoRedis.add(linkedList.get(i));
                        }
                    }
                }
            }
            //校验前3页数据是否已存在recordIdsInfoRedis中
            boolean isIdExist = true;
            List<String> recordIdsInfoList = serviceContext.getRecordIdsInfoList();
            if (recordIdsInfoList != null) {
                for (String recordId : recordIdsInfoList) {
                    if (!recordIdsInfoRedis.contains(recordId)) {
                        isIdExist = false;
                        break;
                    }
                }
            }
            if (isIdExist) {
                if (originalRedisSize == recordIdsInfoList.size() && linkedList.size() == 0) {
                    logger.info("{}:redis集合与前3页的recordId集合一致，不更新redis", serviceContext.getName());
                } else {
                    if (recordIdsInfoList != null && recordIdsInfoList.size() > 0) {
                        if (originalRedisSize < recordIdsInfoList.size()) {
                            logger.info("{}:redis集合中包含前3页的recordId,更新前3页recordId到redis中", serviceContext.getName());
                            boolean isDelete = stringRedisTemplate.delete(serviceContext.getName() + "_RecordIdsInfo");
                            stringRedisTemplate.opsForList().rightPushAll(serviceContext.getName() + "_RecordIdsInfo", recordIdsInfoList);
                        } else {
                            if (serviceContext.getCheckListPageNum() > 3) {
                                logger.info("{}:redis集合中包含前3页的recordId,更新前3页recordId到redis中", serviceContext.getName());
                                boolean isDelete = stringRedisTemplate.delete(serviceContext.getName() + "_RecordIdsInfo");
                                stringRedisTemplate.opsForList().rightPushAll(serviceContext.getName() + "_RecordIdsInfo", recordIdsInfoList);
                            } else {
                                logger.info("{}:redis集合中包含所有校验的recordId，且校验的页数不超过3页，不更新redis", serviceContext.getName());
                            }
                        }
                    }
                }
            } else {
                if (recordIdsInfoRedis != null && recordIdsInfoRedis.size() > 0) {
                    logger.info("{}:redis集合中不全包含前3页的recordId,更新新增Id到redis中", serviceContext.getName());
                    boolean isDelete = stringRedisTemplate.delete(serviceContext.getName() + "_RecordIdsInfo");
                    stringRedisTemplate.opsForList().rightPushAll(serviceContext.getName() + "_RecordIdsInfo", recordIdsInfoRedis);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
             校验周期，默认为1个小时，若当次启动无数据，则在当前的校验周期+10，最大为1小时
             若当次启动有数据，则当前的校验周期-10，最小为10分钟。
             增量启动时，增量的启动周期为period保持不变，结束时修改启动周期
                        并且按照校验周期的判断规则，修改校验周期
         */
        try {
            Date startDateTime = serviceContext.getStartTime();
            if (startDateTime == null) {
                startDateTime = new Date();
            }
            if (crawlType == 0 && warName.contains("total")) {
                /*如果是全量，则在完成的时候判断全量是否完成，若完成，则将爬虫类型更改为增量1
                1:如果当前页等于最大页数,则判定为全量完成
                2:如果结束时，当前页数大于100页,则判定为全量完成
                3:已经到达时间判断临界点,则判定为全量完成
             */
                //增量无报错且执行到2016年数据或者执行到列表页最大页的时候，则将爬虫改为增量
                if ((serviceContext.getPageNum() >= serviceContext.getMaxPage()) || serviceContext.isHasReachedTooOldData() || (serviceContext.getPageNum() >= 100)) {
                    crawlerConfig.setCrawlType(1);
                }
            } else {
                //校验周期以及校验时间
                logMessage.append(className + " 校验完毕");
                String newCheckTime = null;
                if (successNum == 0) {
                    // 修改校验周期 若当次启动无数据，则在当前的校验周期+10，最大为1小时
                    if (checkPeriod != 60) {
                        checkPeriod = checkPeriod + 10;
                        if (checkPeriod > 60) {
                            checkPeriod = 60;
                        }
                        crawlerConfig.setCrawlCheckPeriod(checkPeriod);
                    }
                    // 修改校验时间 若当次启动无数据，则在校验时间是原有校验时间+10分钟
                    newCheckTime = SpecialUtil.UpdateTime(SpecialUtil.date2StrContainHour(startDateTime), checkPeriod * 60 * 1000);
                    crawlerConfig.setCrawlChecktime(newCheckTime);
                } else {
                    // 修改校验时间 若当次启动无数据，则修改校验周期为10，最小时间间隔
                    checkPeriod = 10;
                    crawlerConfig.setCrawlCheckPeriod(checkPeriod);
                    newCheckTime = SpecialUtil.UpdateTime(SpecialUtil.date2StrContainHour(startDateTime), checkPeriod * 60 * 1000);
                    crawlerConfig.setCrawlChecktime(newCheckTime);
                }

                //如果成功数和失败数都为0则下一次启动只校验1页
                if (successNum == 0 && errorNum == 0) {
                    crawlerConfig.setNeedCheckPageNum(1);
                    logger.info("{}本次启动，成功数失败数都为0，下次启动只校验1页", className);
                } else {
                    crawlerConfig.setNeedCheckPageNum(0);
                    logger.info("{}本次启动，成功数为：{}，失败数为：{}，下次启动恢复正常校验", className, successNum, errorNum);
                }
                logger.info("{}校验启动执行完毕，入库成功数为：{}，修改后的校验周期为：{}，下一次的校验时间为：{}", className, successNum, checkPeriod, newCheckTime);
            }


        } catch (Exception e) {
            e.printStackTrace();
            //默认60分钟校验一次
            String newCheckTime = SpecialUtil.UpdateTime(SpecialUtil.date2StrContainHour(new Date()), 60 * 60 * 1000);
            crawlerConfig.setCrawlChecktime(newCheckTime);
        }
        if (errorNum > 0 || serviceContext.getErrorJsonArray().length() != 0) {
            logMessage.append(serviceContext.getErrorJsonArray().toString());
            //设置本次是正常启动还是异常启动
            int errorStartCount = crawlerConfig.getCrawlErrorStartCount();
            logger.info("{}本次启动异常，历史异常启动次数为：{}", className, errorStartCount);
            crawlerConfig.setCrawlErrorStartCount(errorStartCount + 1);
        } else {
            int normalStartCount = crawlerConfig.getCrawlNormalStartCount();
            crawlerConfig.setCrawlNormalStartCount(normalStartCount + 1);
            logger.info("{}本次启动正常，历史正常启动次数为：{}", className, normalStartCount);
        }
        crawlerConfigDao.save(crawlerConfig);

        if ((errorNum != 0) || (successNum != 0)) {
            //如果当前启动成功数和失败数都为0，则不记录日志
            //记录启动日志
            CrawlerLog crawlerLog = new CrawlerLog();
            crawlerLog.setCrawlClass(serviceContext.getName());
            crawlerLog.setTimerName(serviceContext.getWarName());
            crawlerLog.setCrawlCreateBy(serviceContext.getCrawlCreateBy());
            crawlerLog.setCrawlResourcesNum(serviceContext.getSourceNum());

            Date dateStart = serviceContext.getStartTime();
            //启动时间
            Calendar calStart = Calendar.getInstance();
            calStart.setTime(dateStart);
            calStart.add(Calendar.HOUR_OF_DAY, +8);
            dateStart = calStart.getTime();
            crawlerLog.setCrawlStartTime(dateStart);
            crawlerLog.setCrawlLogUuid(serviceContext.getControlUuid());


            crawlerLog.setCrawlError(errorNum);
            crawlerLog.setCrawlSuccess(successNum);
            //爬虫结束时间，因为mongo有8个小时时差所有要处理日期
            Date dateEnd = new Date();
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(dateEnd);
            calEnd.add(Calendar.HOUR_OF_DAY, +8);
            dateEnd = calEnd.getTime();
            crawlerLog.setCrawlEndTime(dateEnd);

            crawlerLog.setCrawlResult(logMessage.toString());
            crawlerLogDao.save(crawlerLog);
        } else {
            logger.info("{}启动完毕，成功数为0，失败数为0，不记录日志", className);
        }

        logger.info("{} Done!", this.getClass().getSimpleName());
        // 初始化启动参数防止数据遗失
        serviceContext.serviceContextInitParam();
    }


    /**
     * 判断是否需要停止爬虫
     *
     * @param serviceContext 用于传递上下文参数的封装对象
     */
    public boolean isNeedStopSpider(ServiceContext serviceContext) {
        boolean flag = false;
        Spider spider = serviceContext.getSpider();
        if (spider != null) {
            /* 1:  如果爬虫isNeedCrawl为false，则说明需要立刻停止爬虫
             */
            if (!serviceContext.isNeedCrawl()) {
                if (spider.getStatus().toString().equals("Running")) {
                    serviceContext.setNeedCrawl(false);
                    spider.stop();
                    flag = true;
                }
            }
        }
        return flag;
    }

    /**
     * 判断标题和id是否为空并处理
     *
     * @param serviceContext 用于传递上下文参数的封装对象
     * @param title          列表页获取的标题
     * @param id             列表页获取的id
     */
    public void dealWithNullTitleOrNullId(ServiceContext serviceContext, String title, String id) throws Exception {
        boolean titleFlag = StringUtils.isEmptyIfRgdlssSpace(title);
        if (titleFlag || StringUtils.isEmptyIfRgdlssSpace(id)) {
            serviceContext.setHasNullTitleOrNullIdErrorNum(serviceContext.getHasNullTitleOrNullIdErrorNum() + 1);
            if (titleFlag) {
                throw new Exception("异常原因说明：公告标题为空,请检查程序");
            } else {
                throw new Exception("异常原因说明：公告id为空,请检查程序");
            }
        }
    }


    /**
     * 处理列表页为空的情况
     *
     * @param serviceContext 用于传递上下文参数的封装对象
     */
    public void dealWithNullListPage(ServiceContext serviceContext) throws Exception {
        if (serviceContext.getPageNum() == 1) {
            throw new Exception("首页内容为空,请检查程序");
        } else {
            // 出现5次列表页为空，则停止爬虫
            serviceContext.setErrorPage(serviceContext.getErrorPage() + 1);
            if (serviceContext.getErrorPage() >= 5) {
                throw new Exception("异常原因说明：连续5页列表页为空，请检查翻页规则");
            }
        }

    }

    //判断数据是否存在数据库
    public BranchNew isDataExist(BranchNew branch, ServiceContext serviceContext) {
        List<String> reccordIdsInfoRedis = serviceContext.getRecordIdsInfoRedis();
        String recordId = branch.getId();
        if (!reccordIdsInfoRedis.contains(recordId)) {
            List<Proclamation> oldPro = proclamationDao.isProclamationExist(
                    serviceContext.getTableName(), serviceContext.getSourceNum(), serviceContext.getSplitPointStr(),
                    recordId);
            if (oldPro.size() == 0) {
                branch.setType(3);
                logger.info("{}：oracle中不存在的recordId为：{}", serviceContext.getName(), recordId);
            } else {
                LinkedList linkedList = serviceContext.getLinkedList();
                if (!linkedList.contains(recordId)) {
                    linkedList.addFirst(recordId);
                    serviceContext.setLinkedList(linkedList);
                    logger.info("{}linkedList中不存在的recordId为：{}", serviceContext.getName(), recordId);
                } else {
                    logger.info("{}linkedList识别到存在的recordId为：{}", serviceContext.getName(), recordId);
                }
            }
        } else {
            logger.info("{}识别到已经在redis中的recordId为：{}", serviceContext.getName(), recordId);
        }
        return branch;
    }

    /*
      * 判断是不是需要继续进行爬虫或者停止
      * >>>>>>如果是全量则不需要校验，跑到2016年数据或者跑到连续报错符合停止条件，则停止爬虫
        广联达定时器，每页都要校验，校验到连续3页并且当前列表有5月1号之前的数据存在，则停止爬虫
        普通定时器启动，每页都要校验，校验到连续3页数据都存在数据库，则停止爬虫
        *              如果第一页出现2020年以前的数据，且第一页无数据入库，则停止爬虫
        普通定时器校验，校验到redis的normalId，如果首页无数据入库，则停止爬虫，若有入库，则爬到连续3页数据都存在则停止爬虫
        分解数据，则每页校验，检查到5页的入库数量为0 ，则停止爬虫
      */
    public List<BranchNew> checkData(List<BranchNew> detailList, ServiceContext serviceContext) {
        boolean flag = isNeedStopSpider(serviceContext);
        //待爬取的详情页集合
        List<BranchNew> needCrawldetailList = new ArrayList<>();
        int crawlType = serviceContext.getCrawlType();
        //接口校验的列表次数
        int checkListPageNum = serviceContext.getCheckListPageNum();
        String className = serviceContext.getName();
        int needCheckPageNum = serviceContext.getNeedCheckPageNum();
        int pageNum = serviceContext.getPageNum();
        logger.info("{}:第{}页列表需要校验的条数为{},需要校验的列表个数为：{},已经校验的列表个数为：{}", className, pageNum, detailList.size(), needCheckPageNum, checkListPageNum);
        //如果是全量，则不需要校验
        if (!flag) {
            try {
                // 遍历判断每一条爬虫是否存在有无必要进行数据爬取
                int oldDataNum = 0;
                if ((checkListPageNum == 2) && (serviceContext.getSuccessNum() == 0 && serviceContext.getErrorNum() == 0)) {
                    if (needCheckPageNum == 1) {
                        //招标分解中标，第一页有数据需要爬取但是无数据需要入库，则停止
                        serviceContext.setNeedCrawl(false);
                        logger.info("{},只需要校验1页，第一页有数据需要爬取但未入库准备校验第2页，符合停止条件", className);
                    }
                } else {
                    for (BranchNew branch : detailList) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            Date pageDate = new Date();
                            if (branch.getDate() != null && branch.getDate().length() != 0) {
                                pageDate = sdf.parse(branch.getDate());
                                if (pageDate.before(sdf.parse("2016-01-01"))) {
                                    continue;
                                }
                                //如果，待爬取列表包含公告时间位于2019年1月1号之前的数据，则标识识别到旧数据
                                if (pageDate.before(sdf.parse("2019-01-01"))) {
                                    serviceContext.setExistOldData(true);
                                }
                            }
                            //记录前3页的recordId
                            if (pageNum <= 3) {
                                List<String> recordIdsInfoList = serviceContext.getRecordIdsInfoList();
                                if (recordIdsInfoList == null) {
                                    recordIdsInfoList = new ArrayList<>();
                                }
                                if (!recordIdsInfoList.contains(branch.getId())) {
                                    recordIdsInfoList.add(branch.getId());
                                    serviceContext.setRecordIdsInfoList(recordIdsInfoList);
                                }
                            }
                            if (branch.getTitle() != null && (branch.getTitle().contains("国泰测试") || branch.getTitle().contains("招采进宝测试"))) {
                                continue;
                            }
                            branch = isDataExist(branch, serviceContext);
                            if (branch.getType() == 3) {
                                needCrawldetailList.add(branch);
                            } else {
                                oldDataNum = oldDataNum + 1;
                            }
                        } catch (Exception e) {
                            dealWithError(branch.getLink() != null ? branch.getLink() : branch.getTitle(), serviceContext, e);
                        }
                    }
                    //修改列表连续存在数据库的页数
                    if (needCrawldetailList.size() == 0) {
                        serviceContext.setHasDataPageNum(serviceContext.getHasDataPageNum() + 1);
                    } else {
                        // 如果发现有一条数据不存在，则将该变量初始化为0
                        serviceContext.setHasDataPageNum(0);
                    }

                    if (crawlType == 0 && warName.contains("total")) {
                        //全量启动，则不干预爬虫
                        return needCrawldetailList;
                    } else {
                        if ((pageNum == 1) && needCrawldetailList.size() == 0 && needCheckPageNum == 1) {
                            //如果第一页无数据需要入库，则停止
                            serviceContext.setNeedCrawl(false);
                            logger.info("{},只需要校验1页且当前页为{}，无数据需要入库，符合停止条件", className, pageNum);
                        } else if (checkListPageNum == 1 && needCrawldetailList.size() == 0 && serviceContext.isExistOldData()) {
                            //如果是校验启动，且首页无数据入库，且首页数据存在到2019年的数据,则停止爬虫
                            serviceContext.setNeedCrawl(false);
                            logger.info("{},校验启动首页无数据入库且首页含有2019年前的数据，符合停止条件", className, serviceContext.getHasDataPageNum(), pageNum);
                        } else if (serviceContext.getHasDataPageNum() >= 3) {
                            //如果校验启动，连续3页列表都无数据入库，则停止爬虫
                            serviceContext.setNeedCrawl(false);
                            logger.info("{},校验启动连续存在的历史数据为：{},已爬取的页数为{}，符合停止条件", className, serviceContext.getHasDataPageNum(), pageNum);
                        } else if (serviceContext.getSuccessNum() == 0 && (pageNum >= 5 || checkListPageNum > 5)) {
                            //如果请求5页成功数仍然为0，则有可能是分解，则执行5页后停止(针对校验或者增量启动)
                            logger.info("{},校验启动连续存在的历史数据为：{},爬取5页仍无数据需要入库，疑似分解，需要停止爬虫，当前页为{}", className, serviceContext.getHasDataPageNum(), pageNum);
                            serviceContext.setNeedCrawl(false);
                        } else if (pageNum > maxPageNum || (checkListPageNum > maxPageNum)) {
                            // 设置增量或者校验的爬虫最大翻页为35页
                            logger.info("{},校验启动连续存在的历史数据为：{},已爬取到最大页需要停止爬虫，当前页为{}", className, serviceContext.getHasDataPageNum(), pageNum);
                            serviceContext.setNeedCrawl(false);
                        }
                    }
                }
                //校验爬虫是否需要停止
                boolean result = isNeedStopSpider(serviceContext);
                serviceContext.setCheckListPageNum(checkListPageNum + 1);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return needCrawldetailList;
    }

    static String[] fileSuffix = {".zip", ".rar", ".tar", ".7z", ".wim", ".docx", ".doc", ".xlsx", ".xls", ".pdf", ".txt", ".hnzf", ".bmp", ".jpg", ".jpeg", ".png", ".tif", ".swf", "UploadedFile", "attachment.jspx"};


    //下载内容中的图片以及附件
    public JSONObject downFile(String uuid, RecordVO recordVO, ServiceContext serviceContext) {
        int saveFileNum = 0;
        String content = recordVO.getContent();
        JSONObject jsonObject = new JSONObject();
        JSONArray fileArray = new JSONArray();
        try {
            boolean isValidate = true;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            String dateString = formatter.format(new Date());
            String sourceNum = serviceContext.getSourceNum();
            String serviceName = serviceContext.getName();
            if (sourceNum.contains("-")) {
                sourceNum = sourceNum.substring(0, sourceNum.indexOf("-"));
            }
            //需要入库的时候则进行图片下载
            Document doc = Jsoup.parse(content);
            Elements eles = doc.select("*");
            StringBuilder errorInfo = new StringBuilder();
            for (Element ele : eles) {
                String tagName = ele.tagName();
                String link = "";
                String fileType = "";
                if ("a".equals(tagName)) {
                    link = ele.attr("href");
                    String txt = ele.text();
                    for (String str : fileSuffix) {
                        if (link.toLowerCase().contains(str)) {
                            fileType = str;
                            break;
                        }
                        if (txt != null && txt.length() > 0 && fileType.length() == 0) {
                            if (txt.toLowerCase().contains(str)) {
                                fileType = str;
                                break;
                            }
                        }
                    }
                } else if ("img".equals(tagName)) {
                    link = ele.attr("src");
                    if (link.contains("data:image")) {
                        try {
                            String path = imgPath + "/" + dateString + "/" + recordVO.getDate() + "/" + sourceNum;
                            String fileName = String.valueOf(System.currentTimeMillis()) + ".png";
                            String newLink = "http://www.bidizhaobiao.com/file/" + dateString + "/" + recordVO.getDate() + "/" + sourceNum + "/" + fileName;
                            // 文件保存位置
                            File saveDir = new File(path);
                            if (!saveDir.exists()) {
                                saveDir.mkdirs();
                            }
                            byte[] imagedata = DatatypeConverter.parseBase64Binary(link.substring(link.indexOf(",") + 1));
                            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imagedata));
                            ImageIO.write(bufferedImage, "png", new File(path + "/" + fileName));
                            ele.attr("src", newLink);
                            JSONObject fileJson = new JSONObject();
//                            fileJson.put("fileLink", infoLink);
                            //String md5 = SpecialUtil.stringMd5(imagedata);

                            //将文件流转换成md5
                            FileInputStream fis = new FileInputStream(new File(path + "/" + fileName));
                            byte[] buffer = new byte[10 * 1024];
                            MessageDigest md = MessageDigest.getInstance("MD5");
                            int len = 0;
                            while ((len = fis.read(buffer)) != -1) {
                                md.update(buffer, 0, len);
                            }
                            byte[] b = md.digest();
                            String md5 = new HexBinaryAdapter().marshal(b).toLowerCase();
                            fileJson.put("fileMd5", md5);
                            //替换原始链接
                            ele.attr("fileLink", md5);
                            fileArray.put(fileJson);
                            content = doc.body().html();
                        } catch (Exception e) {
                            ele.remove();
                        }
                        continue;
                    }

                    if (link.contains("file:")) {
                        ele.remove();
                        continue;
                    }
                    for (String str : fileSuffix) {
                        if (link.toLowerCase().contains(str)) {
                            fileType = str;
                            if (str.equals("UploadedFile")) {
                                fileType = ".png";
                            }
                            break;
                        }
                    }
                }
                if (fileType.length() != 0) {
                    boolean needSave = true;
                    List<DownloadInfo> downloadInfos = downloadInfoDao.findByOldLinkAndRecordId(link, recordVO.getId());
                    if (downloadInfos.size() > 0) {
                        DownloadInfo downloadInfo = downloadInfos.get(0);
                        String md5 = downloadInfo.getFileInfo();
                        if (md5 != null) {
                            logger.info("{}中的{}附件已经下载过不用下载{}", serviceName, recordVO.getId(), link);
                            needSave = false;
                            String infoLink = downloadInfo.getNewLink();
                            JSONObject fileJson = new JSONObject();
                            fileJson.put("fileMd5", md5);
                            ele.attr("fileLink", md5);
                            ele.attr("rel", "noreferrer");
                            if (ele.hasAttr("href")) {
                                logger.info(serviceName + ":公告id为：" + recordVO.getId() + "连接替换成功");
                                ele.after("<a href=\"" + infoLink + "\" style=\"display:none\"  data=" + md5 + ">" + ele.text() + "</a>");
                                fileJson.put("fileTitle", ele.text());
                            } else if (ele.hasAttr("src")) {
                                ele.after("<img src=\"" + infoLink + "\" style=\"display:none\"  data=" + md5 + ">");
                                logger.info(serviceName + ":公告id为：" + recordVO.getId() + "连接替换成功");
                            }

                            fileArray.put(fileJson);
                            content = doc.body().html();
                        } else {

                            logger.info("{}中的{}附件已经下载过,但是md5为空，需要继续下载{}", serviceName, recordVO.getId(), link);
                        }
                    }
                    if (needSave) {
                        String fileName = String.valueOf(System.currentTimeMillis()) + fileType;
                        //图片类型
                        String path = imgPath + "/" + dateString + "/" + recordVO.getDate() + "/" + sourceNum;
                        String infoLink = "";
                        String newLink = "";
                        //记录原始链接和下载后的链接
                        DownloadInfo downloadInfo = new DownloadInfo();
                        try {
                            //遍历字符串
                            for (int i = 0; i < link.length(); i++) {
                                char charAt = link.charAt(i);
                                //只对汉字处理
                                if (isChineseChar(charAt)) {
                                    String encode = URLEncoder.encode(charAt + "", "UTF-8");
                                    infoLink += encode;
                                } else {
                                    infoLink += charAt;
                                }
                            }
                            newLink = "http://www.bidizhaobiao.com/file/" + dateString + "/" + recordVO.getDate() + "/" + sourceNum + "/" + fileName;
                            logger.info(serviceName + ":公告id为：" + recordVO.getId() + "开始下载第" + saveFileNum + "个附件：" + infoLink + "附件路径:" + path + "/" + fileName);
                            downloadInfo.setUuid(uuid);
                            downloadInfo.setWebSourceNo(serviceContext.getSourceNum());
                            downloadInfo.setRecordId(recordVO.getId());
                            downloadInfo.setOldLink(infoLink);
                            downloadInfo.setNewLink(newLink);
                            downloadInfo.setCreateTime(new Date());
                            String name = serviceName;
                            name = name.substring(name.lastIndexOf("_") + 1);
                            int docChannel = SpecialUtil.calcDocChannel(name);
                            downloadInfo.setDocChannel(docChannel);
                            JSONObject saveResultJsonObject = saveFileUtils.saveUrlAs(serviceContext, infoLink.trim(), path, fileName,
                                    recordVO.getDetailLink());
                            boolean saveResult = saveResultJsonObject.getBoolean("result");
                            if (saveResult) {
                                saveFileNum = saveFileNum + 1;
                                String md5 = saveResultJsonObject.getString("FileIno");
                                JSONObject fileJson = new JSONObject();
//                            fileJson.put("fileLink", infoLink);
                                fileJson.put("fileMd5", md5);
                                //替换原始链接
                                ele.attr("fileLink", md5);
                                ele.attr("rel", "noreferrer");
                                if (ele.hasAttr("href")) {
                                    logger.info(serviceName + ":公告id为：" + recordVO.getId() + "连接替换成功");
                                    ele.after("<a href=\"" + newLink + "\" style=\"display:none\"  data=" + md5 + ">" + ele.text() + "</a>");
                                    fileJson.put("fileTitle", ele.text());
                                } else if (ele.hasAttr("src")) {
                                    ele.after("<img src=\"" + newLink + "\" style=\"display:none\"  data=" + md5 + ">");
                                    logger.info(serviceName + ":公告id为：" + recordVO.getId() + "连接替换成功");
                                }
                                fileArray.put(fileJson);
                                if (infoLink.getBytes("utf-8").length < 1024) {
                                    downloadInfo.setFileInfo(md5);
                                    downloadInfoDao.save(downloadInfo);
                                } else {
                                    logger.info("{}的附件{}下载链接太长，暂不入库", serviceName, recordVO.getId());
                                }
                            }
                            content = doc.body().html();
                        } catch (Exception e) {
                            e.printStackTrace();
                            errorInfo.append(SpecialUtil.getErrorInfoFromException(e));
                            isValidate = false;
                        }
                    }
                }
                if (saveFileNum > 50) {
                    logger.info("{}的附件{}下载个数太多，暂不下载", serviceName, recordVO.getId());
                    break;
                }
            }
            jsonObject.put("content", content);
            jsonObject.put("fileInfo", fileArray);
            //如果下载异常，则记录异常公告id
            if (!isValidate) {
                //记录单次附件下载异常的次数
                serviceContext.setErrorSaveFileNum(serviceContext.getErrorSaveFileNum() + 1);
                JSONObject errorJson = new JSONObject();
                errorJson.put("link", recordVO.getId());
                errorJson.put("errorInfo", "附件下载异常，请检查" + errorInfo);
                serviceContext.getErrorJsonArray().put(errorJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    //判断汉字的方法,只要编码在\u4e00到\u9fa5之间的都是汉字
    private static boolean isChineseChar(char c) {
        boolean result = false;
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            result = true;

        }
        return result;
    }


    // 获取信息，修正广联达有问题编号进行数据校验和入库
    public boolean dataStorage(ServiceContext serviceContext, RecordVO recordVo, int type) throws Exception {
        boolean result = false;
        String uuid = SpecialUtil.getNewUUID().toString();
        try {
            //根据标题判断是否是有价值数据
            String title = recordVo.getTitle();
            if (title == null) {
                title = recordVo.getListTitle();
                if (title != null) {
                    recordVo.setTitle(title);//将列表页标题先赋值给详情页标题保证可以入库
                }
            }

            boolean isValue = isValuableByTitle(title);
            if (isValue) {
                // 入库前增强处理数据对象
                recordVo = CrawlCheckUtils.refreshRecordVO(recordVo);
                // 对解析出来的结果进行校验---是否合法
                recordVo = CrawlCheckUtils.validateDetail(serviceContext, recordVo);
                // 判断时间是不是分界点之前的数据
                if (serviceContext.isHasReachedTooOldData()) {
                    throw new Exception("获取到2016-01-01之前的数据");
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date pageDate = sdf.parse(recordVo.getDate());
                JSONArray fileArray = new JSONArray();
                if (serviceContext.isSaveFile() && (pageDate.after(sdf.parse("2020-01-01")))) {
                    JSONObject saveFileJsonObject = downFile(uuid, recordVo, serviceContext);//下载文件中的图片
                    String content = saveFileJsonObject.getString("content");
                    fileArray = saveFileJsonObject.getJSONArray("fileInfo");
                    recordVo.setContent(content);
                    if (serviceContext.getErrorSaveFileNum() > 3 && (serviceContext.getCrawlType() != 0)) {
                        serviceContext.setSaveFile(false);
                    }
                }
                //检测公告内容是否为空
                String detailContent = recordVo.getContent();
                detailContent = detailContent.replace(title, "");
                Document detailHtml = Jsoup.parse(detailContent);
                String text = detailHtml.text();
                if (text.length() == 0) {
                    //公告内容无iframe也没有img
                    if (detailHtml.select("img[src^=http]").size() == 0) {
                        throw new Exception("识别到公告内容为空！");
                    }
                }
                String area = recordVo.getArea();
                String province = recordVo.getProvince();
                String city = recordVo.getCity();
                String district = recordVo.getDistrict();
                // 判断地区信息
                if ((area != null && area.trim().length() > 0) || (province != null && province.trim().length() > 0)
                        || (city != null && city.trim().length() > 0) || (district != null && district.trim().length() > 0)) {
                    boolean isRegionalException = false;
                    List<PosOfProclamation> regionInfoList = new ArrayList<>();
                    if (district != null && district.length() > 0) {
                        district = district.replace("市", "");
                        //区县
                        if (city != null && city.length() > 0) {
                            city = city.replace("市", "");
                            List<PosOfProclamation> list = areaDao.findByDistrictAndCity(district.trim(), city.trim());
                            if (list.size() == 0) {
                                isRegionalException = true;
                            } else {
                                regionInfoList = list;
                            }
                        } else {
                            List<PosOfProclamation> list = areaDao.findByDistrict(district.trim());
                            if (list.size() == 1) {
                                regionInfoList = list;
                            } else if (list.size() == 0 || ((province == null || province.isEmpty()) && (area == null || area.isEmpty()))) {
                                isRegionalException = true;
                            }
                        }
                    } else if (city != null && city.length() > 0) {
                        //地市
                        city = city.replace("市", "");
                        List<PosOfProclamation> list = areaDao.findByCity(city.trim());
                        if (list.size() == 0) {
                            isRegionalException = true;
                        } else {
                            regionInfoList = list;
                        }
                    } else if (province != null && province.length() > 0) {
                        //省份
                        province = province.replace("市", "").replace("自治区", "").replaceAll("维吾尔|回族|壮族", "");
                        List<PosOfProclamation> list = areaDao.findByProvince(province.trim());
                        if (list.size() == 0) {
                            isRegionalException = true;
                        } else {
                            regionInfoList = list;
                        }
                    }
                    if (!isRegionalException && regionInfoList.size() == 0 && !"全国".equals(area)) {
                        isRegionalException = true;
                    } else if (!isRegionalException && !"全国".equals(area)) {
                        if (regionInfoList.size() > 1) {
                            boolean isSuccess = false;
                            for (int i = 0; i < regionInfoList.size() && !isSuccess; i++) {
                                PosOfProclamation posOfProclamation = regionInfoList.get(i);
                                if ((city != null && !city.equals(posOfProclamation.getCity())) || (province != null && !province.equals(posOfProclamation.getProvince()))
                                        || (area != null && !area.equals(posOfProclamation.getArea()))) {
                                    continue;
                                }
                                isSuccess = true;

                            }
                            if (!isSuccess) {
                                isRegionalException = true;
                            }
                        } else {
                            PosOfProclamation posOfProclamation = regionInfoList.get(0);
                            if ((district != null && !district.equals(posOfProclamation.getDistrict()))
                                    || (city != null && !city.equals(posOfProclamation.getCity()))
                                    || (province != null && !province.equals(posOfProclamation.getProvince()))
                                    || (area != null && !area.equals(posOfProclamation.getArea()))) {
                                isRegionalException = true;
                            } else {
                                district = posOfProclamation.getDistrict();
                                city = posOfProclamation.getCity();
                                province = posOfProclamation.getProvince();
                                area = posOfProclamation.getArea();
                            }
                        }
                    }
                    if (isRegionalException) {
                        recordVo.setArea("全国");
                        recordVo.setProvince(null);
                        recordVo.setCity(null);
                        recordVo.setDistrict(null);
                        Date nowDate = new Date();
                        RegionErrorInfo regionErrorInfo = new RegionErrorInfo();
                        regionErrorInfo.setWebSourceNo(serviceContext.getSourceNum());
                        regionErrorInfo.setRecordId(recordVo.getId());
                        regionErrorInfo.setPageTitle(recordVo.getTitle());
                        regionErrorInfo.setDetailLink(recordVo.getDetailLink());
                        regionErrorInfo.setErrorInfo("当前地区错误：" + area + "（区域）/" + province + "（省份）/" + city + "（地市）/" + district + "（区县）");
                        regionErrorInfo.setCreateTime(nowDate);
                        regionErrorInfo.setErrorTime(SpecialUtil.date2Str(nowDate));
                        regionErrorInfoDao.save(regionErrorInfo);
                        logger.error("公告信息所属地区错误！");
                    } else {
                        recordVo.setArea(area);
                        recordVo.setProvince(province);
                        recordVo.setCity(city);
                        recordVo.setDistrict(district);
                    }
                } else {
                    recordVo.setArea(serviceContext.getArea());
                    recordVo.setCity(serviceContext.getCity());
                    recordVo.setProvince(serviceContext.getProvince());
                    recordVo.setDistrict(serviceContext.getDistrict());
                }

                //根据类型判断是否需要入库
           /* 标识数据需要入库的类型 初始值type=1
            1：既需要入招投标信息表，又需要入公告信息表
            2：不需要入招投标信息表，需要入公告信息表
            3：只需要入招投标信息表，不需要入公告信息表
            */
                // 入库比地招标库
                Proclamation proclamation = proclamationDao.createEntity(serviceContext.getSourceNum(),
                        serviceContext.getSourceName(), serviceContext.getInfoSource(), recordVo);
                proclamation.setId(uuid);
                proclamation.setDetailLink(recordVo.getDetailLink());
                if (fileArray != null) {
                    if (recordVo.getAttachment() != null && fileArray.length() == 0) {
                        fileArray = new JSONArray(recordVo.getAttachment());
                        proclamation.setAttachmentPath(recordVo.getAttachment());
                    }
                    //入库前判断附件字段长度
                    if (fileArray.toString().getBytes("utf-8").length > 2048) {
                        JSONArray newArrayInfo = new JSONArray();
                        JSONObject messageJson = new JSONObject();
                        messageJson.put("message", "to long");
                        newArrayInfo.put(messageJson);
                        for (int i = 0; i < fileArray.length(); i++) {
                            JSONObject jsonObjectInfo = fileArray.getJSONObject(i);
                            newArrayInfo.put(jsonObjectInfo);
                            if (newArrayInfo.toString().getBytes("utf-8").length > 2048) {
                                newArrayInfo.remove(newArrayInfo.length() - 1);
                                proclamation.setAttachmentPath(newArrayInfo.toString());
                                break;
                            }
                        }
                    } else {
                        proclamation.setAttachmentPath(fileArray.toString());
                    }
                }
                proclamationDao.saveProclamation(serviceContext.getTableName(), proclamation);
                serviceContext.setSuccessNum(serviceContext.getSuccessNum() + 1);
                LinkedList linkedList = serviceContext.getLinkedList();
                if (!linkedList.contains(recordVo.getId())) {
                    linkedList.addFirst(recordVo.getId());
                    serviceContext.setLinkedList(linkedList);
                }
                logger.info("{}的入库成功数为:{}，失败数为：{},下载的页面个数为：{}", serviceContext.getName(), serviceContext.getSuccessNum(), serviceContext.getErrorNum(), serviceContext.getPageNum());
                serviceContext.sethasErrorNum(0);
                serviceContext.setHasNullTitleOrNullIdErrorNum(0);
                serviceContext.setErrorPage(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String message = (e.getMessage() == null ? "" : e.getMessage());
            throw new Exception(message, e);
        }
        return result;
    }

    /**
     * 判断当前抓取到的公告是否包含测试字眼标题
     *
     * @param title         公告标题
     * @param titleKeyWords 公告内容
     * @return 判断结果（true：有价值；false 无价值：. 默认值为true）
     */
    static String[] titleKeyWords = {"测试测试", "测试勿删", "测试数据"};

    public static boolean isValuableByTitle(String title) throws Exception {
        boolean isValuable = true;
        try {
            for (String titleKeyWord : titleKeyWords) {
                if (title.contains(titleKeyWord)) {
                    isValuable = false;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            String message = (e.getMessage() == null ? "" : e.getMessage());
            throw new Exception(message, e);
        }
        return isValuable;
    }


    /**
     * 异常处理模块
     *
     * @param serviceContext 用于传递上下文参数的封装对象
     * @param e              方法内捕获到的异常信息
     */
    public void dealWithError(String url, ServiceContext serviceContext, Exception e) {
        e.printStackTrace();
        serviceContext.sethasErrorNum(serviceContext.gethasErrorNum() + 1);
        // 登记异常对象中的Message信息
        String message = (e.getMessage() == null ? SpecialUtil.getErrorInfoFromException(e) : e.getMessage());
        /*
         * 处理异常类信息，如果首页内容为空，或者存在连续5条报错则停掉爬虫
         */
        if (message.contains("首页内容为空") || message.contains("连续5页列表页为空")) {
            serviceContext.setNeedCrawl(false);
            boolean flag = isNeedStopSpider(serviceContext);
        } else if (message.contains("公告标题为空") || message.contains("公告id为空")) {
            if (serviceContext.getHasNullTitleOrNullIdErrorNum() >= 5) {
                serviceContext.setNeedCrawl(false);
                boolean flag = isNeedStopSpider(serviceContext);
            }
        } else if (message.contains("获取到2016-01-01之前的数据")) {
            serviceContext.setNeedCrawl(false);
            boolean flag = isNeedStopSpider(serviceContext);
        } else {
            // 增量爬虫才会干预停止，存在连续5条的报错，则停止
            if (serviceContext.gethasErrorNum() >= 5 && serviceContext.getCrawlType() != 0) {
                serviceContext.setNeedCrawl(false);
                boolean flag = isNeedStopSpider(serviceContext);
            }
        }
        if (!message.contains("获取到2016-01-01之前的数据")) {
            serviceContext.setErrorNum(serviceContext.getErrorNum() + 1);
            JSONObject errorJson = new JSONObject();
            errorJson.put("link", url);
            errorJson.put("errorInfo", message);
            serviceContext.getErrorJsonArray().put(errorJson);
        }


    }

    //设置重试次数控制连接超时
    private static HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {
        @Override
        public boolean retryRequest(IOException exception,
                                    int executionCount, HttpContext context) {
            return false;
        }
    };

    //从定时器外部获取httpclent的方法
    public CloseableHttpClient getHttpClient(boolean isNeedSsl, boolean isNeedProxy) {
        SocketConfig.Builder socketConfigBuilder = SocketConfig.custom();
        socketConfigBuilder.setSoKeepAlive(true).setTcpNoDelay(true).setSoTimeout(30 * 1000);
        SocketConfig socketConfig = socketConfigBuilder.build();
        HttpHost httpHost = null;
        String className = serviceContext.getName();
        CloseableHttpClient client = null;
        try {
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30 * 1000).setConnectionRequestTimeout(30 * 1000).setSocketTimeout(30 * 1000).build();
            if (isNeedProxy) {
                logger.info("{}自发请求请求代理开始", className);
                httpHost = ProxyPoolUtil.getProxyPoolApi();
                logger.info("{}自发请求请求代理结束{}:{}", className, httpHost.getHostName(), httpHost.getPort());
            }
            if (isNeedSsl) {
                /*                HttpClientBuilder b = HttpClientBuilder.create();*/
                HttpClientBuilder b = HttpClients.custom();
                // 禁用SSL验证
                SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                    public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                        return true;
                    }
                }).build();
                b.setSslcontext(sslContext);
                SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext,
                        NoopHostnameVerifier.INSTANCE);
                Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory()).register("https", sslSocketFactory)
                        .build();
                BasicHttpClientConnectionManager connMgr = new BasicHttpClientConnectionManager(socketFactoryRegistry);
                connMgr.setSocketConfig(socketConfig);
                // HttpClients.custom().setConnectionManager(connMgr);
                //需要代理，则在原有基础上添加代理
                if (httpHost != null) {
                    DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(httpHost);
                    client = HttpClients.custom().setConnectionManager(connMgr).setRoutePlanner(routePlanner).setDefaultRequestConfig(requestConfig).setDefaultSocketConfig(socketConfig).setRetryHandler(myRetryHandler).build();
                } else {
                    client = HttpClients.custom().setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig).setDefaultSocketConfig(socketConfig).setRetryHandler(myRetryHandler).build();
                }
            } else if (isNeedProxy) {
                //普通自发请求----需要代理
                if (httpHost != null) {
                    client = HttpClients.custom().setDefaultRequestConfig(requestConfig).setDefaultSocketConfig(socketConfig).setProxy(httpHost).setRetryHandler(myRetryHandler).build();
                } else {
                    client = HttpClients.custom().setDefaultRequestConfig(requestConfig).setDefaultSocketConfig(socketConfig).setRetryHandler(myRetryHandler).build();
                }
            } else {
                //普通自发请求--不需要代理
                client = HttpClients.custom().setDefaultRequestConfig(requestConfig).setDefaultSocketConfig(socketConfig).setRetryHandler(myRetryHandler).build();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return client;
    }
}
