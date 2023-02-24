package com.bidizhaobiao.data.Crawl.scheduler;

import com.bidizhaobiao.data.Crawl.dao.mongo.CrawlerConfigDao;
import com.bidizhaobiao.data.Crawl.dao.mongo.CrawlerLogDao;
import com.bidizhaobiao.data.Crawl.dao.mongo.CrawlerTimerLogDao;
import com.bidizhaobiao.data.Crawl.entity.mongo.CrawlerConfig;
import com.bidizhaobiao.data.Crawl.entity.mongo.CrawlerLog;
import com.bidizhaobiao.data.Crawl.entity.mongo.CrawlerTimerLog;
import com.bidizhaobiao.data.Crawl.entity.oracle.NeedStartSpider;
import com.bidizhaobiao.data.Crawl.service.ServiceContext;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
import com.bidizhaobiao.data.Crawl.utils.FileReadUtil;
import com.bidizhaobiao.data.Crawl.utils.SpecialUtil;
import com.bidizhaobiao.data.Crawl.utils.SpringContextUtil;
import com.bidizhaobiao.data.Crawl.utils.TimeDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Spider;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author 作者: 廉建林
 * @version 创建时间：2018年8月9日 上午11:07:54 类说明
 * 定时器校验，用来启动和触发符合条件的爬虫
 */
@Component
@PropertySource({"classpath:application.properties"})
public class CrawlService {
    private static final Logger logger = LoggerFactory.getLogger(CrawlService.class);
    public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    //控制周期的集合
    public static List<String> updatePriodList = new ArrayList<>();
    //启动爬虫所用的集合
    public static List<String> startCrawlList = new ArrayList<>();
    public static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Value("${spring.project.warName}")
    public String warName;//爬虫定时器名字
    @Value("#{${spring.threadpool.fixedThreadPoolNum}}")
    public Integer fixedThreadPoolNum;//外部线程池的个数
    @Value("#{${spring.threadpool.spiderThreadNum}}")
    public Integer spiderThreadNum;//每个爬虫内部线程个数
    @Autowired
    protected CrawlerConfigDao crawlerConfigDao;
    @Autowired
    protected CrawlerTimerLogDao crawlerTimerLogDao;
    @Autowired
    protected CrawlerLogDao crawlerLogDao;
    FileReadUtil fileReadUtil = new FileReadUtil();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd ");
    public Map<String, CrawlerConfig> configMap = new HashMap<String, CrawlerConfig>();


    //定时器周期控制，9点将所有爬虫的校验周期设置为10分钟一次,并且将校验时间调整为10分钟后
    @Scheduled(cron = "0 0 9 * * ?")
    public void crawlerConfigTask() {
        try {
            logger.info("{}开始调整校验周期为10分钟", warName);
            //获取本项目所有的爬虫
            if (updatePriodList.size() == 0) {
                updatePriodList = fileReadUtil.selectConfig();
            }
            int num = 0;
            List<CrawlerConfig> updateConfigList = new ArrayList<CrawlerConfig>();
            while (num < updatePriodList.size()) {
                List subList = new ArrayList();
                if (updatePriodList.size() - num > 500) {
                    subList = updatePriodList.subList(num, 500 + num);
                    num = num + 500;
                } else {
                    subList = updatePriodList.subList(num, updatePriodList.size());
                    num = updatePriodList.size();
                }
                List<CrawlerConfig> configs = crawlerConfigDao.findByCrawlClassIn(subList);
                updateConfigList.addAll(configs);
            }
            //根据本项目下的services类名，读取mongodb获取待爬取的数据
            for (final CrawlerConfig crawlerConfig : updateConfigList) {
                //获取创建时间
                String createTime = crawlerConfig.getCrawlCreateTime();
                if (createTime == null) {
                    createTime = format.format(new Date());
                    crawlerConfig.setCrawlCreateTime(createTime);
                }
                //校验周期统一设置为10分钟
                crawlerConfig.setCrawlCheckPeriod(10);
                //启动时间设置为10分钟以后
                String newCheckTime = SpecialUtil.UpdateTime(SpecialUtil.date2StrContainHour(new Date()), 10 * 60 * 1000);
                crawlerConfig.setCrawlChecktime(newCheckTime);
                crawlerConfigDao.save(crawlerConfig);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //定时器控制爬虫每1分钟启动一次，触发应该启动的爬虫
    @Scheduled(fixedDelay = 1000 * 30)
    public void crawlerStartTask() {
        logger.info("{}定时器开始啦", warName);
        CrawlerTimerLog timerLog = new CrawlerTimerLog();
        try {
            configMap.clear();
            String uuid = SpecialUtil.getNewUUID().toString();
            Date timerStart = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(timerStart);
            cal.add(Calendar.HOUR_OF_DAY, +8);
            timerStart = cal.getTime();
            timerLog.setTimerStartTime(timerStart);
            timerLog.setTimerName(warName);
            timerLog.setTimerControlUuid(uuid);
            timerLog.setTimerType("爬虫控制");
            crawlerTimerLogDao.save(timerLog);
            //插入本项目所有的爬虫配置，获取该定时器所有的爬虫
            if (startCrawlList.size() == 0) {
                startCrawlList = fileReadUtil.selectConfig();
                //存储配置信息
                insertConfig(startCrawlList);
            }
            int num = 0;
            List<CrawlerConfig> crawlListInfoList = new ArrayList<CrawlerConfig>();
            while (num < startCrawlList.size()) {
                List subList = new ArrayList();
                if (startCrawlList.size() - num > 500) {
                    subList = startCrawlList.subList(num, 500 + num);
                    num = num + 500;
                } else {
                    subList = startCrawlList.subList(num, startCrawlList.size());
                    num = startCrawlList.size();
                }
                List<CrawlerConfig> configs = crawlerConfigDao.findByCrawlClassIn(subList);
                crawlListInfoList.addAll(configs);
            }

            // 创建集合用于存放符合启动条件的爬虫
            List<NeedStartSpider> needStartList = new ArrayList<NeedStartSpider>();
            /*
            爬虫的启动类型分为3种：1，新增接口，则需要跑全量，一直跑到2016年的数据，则全量结束
                                2：增量启动，跑到连续3页无数据入库，则停止，按照启动周期，启动
                                3：校验启动，校验首页是否有数据入库，无入库，则停止
                                   校验周期，默认为1个小时，若当次启动无数据，则在当前的校验周期+10，最大为1小时
                                            若当次启动有数据，则当前的校验周期-10，最小为10分钟。
             */
            for (final CrawlerConfig crawlerConfig : crawlListInfoList) {
                // 判断爬虫是否符合条件将其放入集合
                int crawlType = crawlerConfig.getCrawlType();
                String checkTime = crawlerConfig.getCrawlChecktime();
                String className = crawlerConfig.getCrawlClass();
                configMap.put(className, crawlerConfig);
                //如果是全量，则类型匹配便可以启动
                if ((warName.contains("total") && crawlType == 0)) {
                    NeedStartSpider needStartSpider = new NeedStartSpider();
                    needStartSpider.setClassName(className);
                    needStartSpider.setCrawlStartTime(new Date());
                    needStartSpider.setCrawlType(crawlType);
                    needStartList.add(needStartSpider);
                } else if ((!warName.contains("total")) && crawlType == 1) {
                    //如果是普通增量启动，则判断启动时间是否符合条件
                    NeedStartSpider needStartSpider = new NeedStartSpider();
                    if (checkTime != null) {
                        //判断是否到达校验时间，保证每隔15分钟都有校验,只有增量才会有
                        Date checkDate = df.parse(checkTime);
                        if (checkDate.before(new Date())) {
                            needStartSpider.setClassName(className);
                            needStartSpider.setCrawlStartTime(checkDate);
                            needStartSpider.setCrawlType(1);
                            needStartList.add(needStartSpider);
                        }
                    } else if (checkTime == null) {
                        // 如果未校验过，切不符合启动条件，则校验
                        needStartSpider.setClassName(className);
                        needStartSpider.setCrawlStartTime(new Date());
                        needStartSpider.setCrawlType(1);
                        needStartList.add(needStartSpider);
                    }
                }
            }


            //记录定时器单次启动的待爬取队列的个数
            timerLog.setTimerTotalCount(startCrawlList.size());
            timerLog.setTimerNeedStartCount(needStartList.size());
            logger.info("{}定时器包含的爬虫个数为：{}符合启动条件的爬虫个数为：{}", warName, startCrawlList.size(), needStartList.size());
            //对待爬取的爬虫根据时间进行排序;
            Collections.sort(needStartList, new Comparator<NeedStartSpider>() {
                @Override
                public int compare(NeedStartSpider needStartSpider1, NeedStartSpider needStartSpider2) {
                    try {
                        return needStartSpider1.getCrawlStartTime().compareTo(needStartSpider2.getCrawlStartTime());
                    } catch (Exception e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            });
            List<Future> futureList = new ArrayList<Future>();
            // 开启线程池，并发抓取数据
            ExecutorService fixedThreadPool = Executors.newFixedThreadPool(fixedThreadPoolNum);
            for (final NeedStartSpider needStartSpider : needStartList) {
                Future future = fixedThreadPool.submit(new Runnable() {
                    public void run() {
                        String className = needStartSpider.getClassName();
                        SpiderService spiderService = (SpiderService) SpringContextUtil.getBean(className);
                        ServiceContext serviceContext = null;
                        try {
                            //Thread.currentThread().setName(className);
                            int type = needStartSpider.getCrawlType();
                            String time = df.format(needStartSpider.getCrawlStartTime());
                            logger.info("当前要启动的爬虫的名字为：{},启动的时间为：{},启动的类型为：{}", className, time, type);
                            Field[] fields = spiderService.getClass().getDeclaredFields();
                            Field field = spiderService.getClass().getSuperclass().getDeclaredField("serviceContext");
                            serviceContext = (ServiceContext) field.get(spiderService);
                            serviceContext.setControlUuid(uuid);
                            serviceContext.setCrawlerConfig(configMap.get(className));
                            for (int i = 0; i < fields.length; i++) {//遍历
                                fields[i].setAccessible(true);
                                if (fields[i].getName().contains("spider")) {
                                    Spider spider = (Spider) fields[i].get(spiderService);
                                    //判断如果为Running可能是上一次异常中断未stop
                                    if (spider != null && spider.getStatus().toString().equals("Running")) {
                                        spider.stop();
                                    }
                                    spiderService.startCrawl(spiderThreadNum, type);
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            // 爬虫状态监控部分
                            saveCrawlException(serviceContext, SpecialUtil.getErrorInfoFromException(e));
                        }
                    }
                });
                futureList.add(future);

            }
            if (fixedThreadPool != null) {
                // 关闭线程池并等待线程池内的所有线程结束
                fixedThreadPool.shutdown();
                boolean timeout = false;
                boolean isInterrupted = false;
                //只对增量做超时控制机制
                if (!warName.contains("total")) {
                    logger.info("{}进入第一次等待的时间", warName);
                    ThreadPoolExecutor tpe = ((ThreadPoolExecutor) fixedThreadPool);
                    //先等待15分钟，判断15分钟时的线程池状态
                    if (!fixedThreadPool.awaitTermination(15, TimeUnit.MINUTES)) {
                        int fristQueueSize = tpe.getQueue().size();
                        int fristActiveCount = tpe.getActiveCount();
                        logger.info("{}进入第一次等待结束", warName);
                        logger.info("{}本次需要启动的爬虫的个数为：{}", warName, needStartList.size());
                        logger.info("{}第一次等待结束后当前排队线程数：{}", warName, fristQueueSize);
                        logger.info("{}第一次等待结束后当前活动线程数：{}", warName, fristActiveCount);
                        if (fristActiveCount == 1 && fristQueueSize == 0) {
                            //如果当前等待队列为0，而且阻塞线程只有1，则停止
                            logger.info("{}第一次等待结束后，符合停止的条件啦", warName);
                            for (Future<?> task : futureList) {
                                if (!task.isDone()) {
                                    task.cancel(true);
                                    isInterrupted = true;
                                }
                            }
                            //第一次等待结束，不满足干预条件则继续校验
                            //15分钟执行完成以后，未达到干预条件，再等待3分钟，再次判断
                        } else if (!fixedThreadPool.awaitTermination(3, TimeUnit.MINUTES)) {
                            logger.info("{}进入第二次等待结束", warName);
                            int queueSize = tpe.getQueue().size();
                            int secondActiveCount = tpe.getActiveCount();
                            logger.info("{}第二次等待结束后当前排队线程数：{}", warName, queueSize);
                            logger.info("{}第二次等待结束后当前活动线程数：{}", warName, secondActiveCount);
                            //如果当前等待队列为0，而且阻塞线程只有1，或者，等待3分钟后第二次阻塞的个数与第一次阻塞的个数和队列等待的个数仍然相同，则停止
                            if ((secondActiveCount == 1 && queueSize == 0) || ((secondActiveCount == fristActiveCount) && queueSize == fristQueueSize)) {
                                //如果当前等待队列为0，而且阻塞线程只有1，则停止
                                logger.info("{}第二次等待结束后，符合停止的条件啦", warName);
                                for (Future<?> task : futureList) {
                                    if (!task.isDone()) {
                                        task.cancel(true);
                                        isInterrupted = true;
                                    }
                                }
                                //第三次，前面已经两次最长已经等待18分钟了，再等待2分钟，保证定时器最长执行时间为20分钟
                            } else if (!fixedThreadPool.awaitTermination(2, TimeUnit.MINUTES)) {
                                int lastQueueSize = tpe.getQueue().size();
                                int thirdActiveCount = tpe.getActiveCount();
                                logger.info("{}第三次等待结束后当前排队线程数：{}", warName, lastQueueSize);
                                logger.info("{}第三次等待结束后当前活动线程数：{}", warName, thirdActiveCount);
                                for (Future<?> task : futureList) {
                                    if (!task.isDone()) {
                                        task.cancel(true);
                                    }
                                }
                                timeout = true;
                            }
                        }
                    }

                }

                while (true) {
                    //异常结束
                    if (timeout) {
                        //停掉爬虫
                        stopSpider(needStartList);
                        fixedThreadPool.shutdownNow();
                        Date timerEnd = new Date();
                        Calendar cal1 = Calendar.getInstance();
                        cal1.setTime(timerEnd);
                        cal1.add(Calendar.HOUR_OF_DAY, +8);
                        timerEnd = cal1.getTime();
                        timerLog.setTimerEndTime(timerEnd);
                        timerLog.setTimerType("定时器超时结束");
                        crawlerTimerLogDao.save(timerLog);
                        logger.info("{}定时器超时结束啦", warName);
                        break;
                    }
                    //被干预结束
                    if (isInterrupted) {
                        //停掉爬虫
                        stopSpider(needStartList);
                        fixedThreadPool.shutdownNow();
                        Date timerEnd = new Date();
                        Calendar cal1 = Calendar.getInstance();
                        cal1.setTime(timerEnd);
                        cal1.add(Calendar.HOUR_OF_DAY, +8);
                        timerEnd = cal1.getTime();
                        timerLog.setTimerEndTime(timerEnd);
                        timerLog.setTimerType("定时器已干预结束");
                        crawlerTimerLogDao.save(timerLog);
                        logger.info("{}定时器已干预结束啦", warName);
                        break;
                    }
                    //正常结束
                    if (fixedThreadPool.isTerminated()) {
                        Date timerEnd = new Date();
                        Calendar cal1 = Calendar.getInstance();
                        cal1.setTime(timerEnd);
                        cal1.add(Calendar.HOUR_OF_DAY, +8);
                        timerEnd = cal1.getTime();
                        timerLog.setTimerEndTime(timerEnd);
                        timerLog.setTimerType("定时器正常结束");
                        crawlerTimerLogDao.save(timerLog);
                        logger.info("{}定时器结束啦", warName);
                        break;
                    }
                    logger.info("{}定时器进入最后一步判断的语句", warName);
                    Thread.sleep(1000);
                }
            }
        } catch (
                Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 处理请求cookie或者程序异常结束时的报错记录
     *
     * @param serviceContext 用于传递上下文参数的封装对象
     */
    public void saveCrawlException(ServiceContext serviceContext, String exceptionInfo) {
        String className = serviceContext.getName();
        logger.info("定时器外部识别到接口异常要停止的爬虫的名字为：{}", className);
        Spider spider = serviceContext.getSpider();
        //确认资源是否回收
        if (spider != null && spider.getStatus().toString().equals("Running")) {
            logger.info("当前因为接口异常要停止的爬虫的名字为：{}", className);
            spider.stop();
            spider.close();
        }
        int errorNum = serviceContext.getErrorNum();
        int successNum = serviceContext.getSuccessNum();

        // 修改爬虫下次启动时间以及校验时间
        CrawlerConfig crawlerConfig = crawlerConfigDao.findByCrawlClass(className);
        //修改爬虫启动时间,下一次启动时间为:(当前时间+周期)
        StringBuilder logMessage = new StringBuilder();
        int checkPeriod = crawlerConfig.getCrawlCheckPeriod();

        try {
            if (warName.contains("total")) {
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
                    newCheckTime = SpecialUtil.UpdateTime(SpecialUtil.date2StrContainHour(new Date()), checkPeriod * 60 * 1000);
                    crawlerConfig.setCrawlChecktime(newCheckTime);
                } else {
                    // 修改校验时间 若当次启动无数据，则修改校验周期为10，最小时间间隔
                    checkPeriod = 10;
                    crawlerConfig.setCrawlCheckPeriod(checkPeriod);
                    newCheckTime = SpecialUtil.UpdateTime(SpecialUtil.date2StrContainHour(new Date()), checkPeriod * 60 * 1000);
                    crawlerConfig.setCrawlChecktime(newCheckTime);
                }
                logger.info("{}校验启动执行完毕，入库成功数为：{}，修改后的校验周期为：{}，下一次的校验时间为：{}", className, successNum, checkPeriod, newCheckTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String newCheckTime = SpecialUtil.UpdateTime(SpecialUtil.date2StrContainHour(new Date()), checkPeriod * 60 * 1000);
            crawlerConfig.setCrawlChecktime(newCheckTime);
        }
        if (errorNum == 0) {
            errorNum = 1;
        } else {
            errorNum = errorNum + 1;
        }
        logMessage.append("定时器识别到爬虫启动异常，异常信息为：" + exceptionInfo);
        if (serviceContext.getErrorJsonArray().length() != 0) {
            logMessage.append(serviceContext.getErrorJsonArray().toString());
        }
        crawlerConfig.setCrawlStatus(0);
        //设置本次是正常启动还是异常启动
        int errorStartCount = crawlerConfig.getCrawlErrorStartCount();
        logger.info("{}本次启动异常，历史异常启动次数为：{}", className, errorStartCount);
        crawlerConfig.setCrawlErrorStartCount(errorStartCount + 1);
        crawlerConfigDao.save(crawlerConfig);

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

        //爬虫结束时间，因为mongo有8个小时时差所有要处理日期
        Date dateEnd = new Date();
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(dateEnd);
        calEnd.add(Calendar.HOUR_OF_DAY, +8);
        dateEnd = calEnd.getTime();
        crawlerLog.setCrawlError(errorNum);
        crawlerLog.setCrawlSuccess(successNum);
        crawlerLog.setCrawlEndTime(dateEnd);
        crawlerLog.setCrawlResult(logMessage.toString());
        crawlerLogDao.save(crawlerLog);

        // 初始化启动参数防止数据遗失
        logger.info("{} Done!", className);
        serviceContext.serviceContextInitParam();

    }

    //再次确认回收资源
    public void stopSpider(List<NeedStartSpider> needStartList) {
        try {
            for (NeedStartSpider needStartSpider : needStartList) {
                String className = needStartSpider.getClassName();
                int type = needStartSpider.getCrawlType();
                String time = df.format(needStartSpider.getCrawlStartTime());
                SpiderService spiderService = (SpiderService) SpringContextUtil.getBean(className);
                Field[] fields = spiderService.getClass().getDeclaredFields();
                Field field = spiderService.getClass().getSuperclass().getDeclaredField("serviceContext");
                ServiceContext serviceContext = (ServiceContext) field.get(spiderService);
                for (int i = 0; i < fields.length; i++) {//遍历
                    fields[i].setAccessible(true);
                    if (fields[i].getName().contains("spider")) {
                        Spider spider = (Spider) fields[i].get(spiderService);
                        //判断如果为Running可能是上一次异常中断未stop
                        if (spider != null && spider.getStatus().toString().equals("Running")) {
                            logger.info("当前因为定时器超时要停止的爬虫的名字为：{},启动的时间为：{},启动的类型为：{}", className, time, type);
                            spider.stop();
                            spider.close();
                        }
                        break;
                    }
                }
                //记录异常信息
                saveCrawlException(serviceContext, "爬虫因为定时器超时而异常终止");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //通过反射获取类里面的相关信息
    public CrawlerConfig getInfo(CrawlerConfig crawlerConfig, String className) {
        try {
            SpiderService spiderService = (SpiderService) SpringContextUtil.getBean(className);
            //获取该类下面的参数
            Field[] fields = spiderService.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {//遍历
                fields[i].setAccessible(true);
                //获取属性
                String name = fields[i].getName();
                switch (name) {
                    case "sourceNum":
                        String sourceNum = (String) fields[i].get(spiderService);
                        crawlerConfig.setCrawlResourcesNum(sourceNum);
                        break;
                    case "sourceName":
                        String sourceName = (String) fields[i].get(spiderService);
                        crawlerConfig.setCrawlResourcesName(sourceName);
                        break;
                    case "infoSource":
                        String infoSource = (String) fields[i].get(spiderService);
                        crawlerConfig.setCrawlResourcesFrom(infoSource);
                        break;
                    case "createBy":
                        String createBy = (String) fields[i].get(spiderService);
                        crawlerConfig.setCrawlCreateBy(createBy);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return crawlerConfig;
    }

    //判断爬虫的配置信息是否在数据库，没有则插入
    public void insertConfig(List<String> classList) {
        for (String className : classList) {
            String oldProjectName = null;
            try {
                CrawlerConfig crawlerConfig = crawlerConfigDao.findByCrawlClass(className);
                if (crawlerConfig == null) {
                    crawlerConfig = new CrawlerConfig();
                    crawlerConfig = getInfo(crawlerConfig, className);
                    crawlerConfig.setCrawlClass(className);
                    className = className.substring(className.lastIndexOf("_") + 1);
                    int type = SpecialUtil.calcDocChannel(className);
                    crawlerConfig.setDocChannel(type);
                    crawlerConfig.setCrawlPeriod(2);
                    String dateString = formatter.format(new Date());
                    crawlerConfig.setCrawlChecktime(dateString);
                    crawlerConfig.setCrawlThreadNum(4);
                    crawlerConfig.setCrawlPagenum(0);
                    crawlerConfig.setCrawlType(0);

                    crawlerConfig.setCrawlCreateTime(dateString);
                    crawlerConfig.setTimerName(warName);
                    crawlerConfigDao.save(crawlerConfig);
                    logger.info("配置信息插入成功");
                } else {
                    crawlerConfig = getInfo(crawlerConfig, className);
                    //判断启动时间与当前相比是不是超过两天
                    String startTime = crawlerConfig.getCrawlChecktime();
                    Date startDateTime = sdf1.parse((startTime));
                    String dateString = formatter.format(new Date());
                    //如果启动时间大于在当前时间之前
                    int d = TimeDiff.getDatePoolByDay(new Date(), startDateTime);
                    if (startDateTime.before(new Date()) && d > 1) {
                        crawlerConfig.setCrawlChecktime(dateString);
                    }
                    //判断周期大于两个小时的校验时间
                    if (crawlerConfig.getCrawlChecktime() == null) {
                        crawlerConfig.setCrawlChecktime(dateString);
                    }
                    String createTime = crawlerConfig.getCrawlCreateTime();
                    //获取创建时间
                    if (createTime == null) {
                        createTime = format.format(new Date());
                        crawlerConfig.setCrawlCreateTime(createTime);
                        crawlerConfigDao.save(crawlerConfig);
                    }
                    //如果爬虫定时器位置发生改变，则需要修改启动时间为当前时间
                    oldProjectName = crawlerConfig.getTimerName();
                    if ((oldProjectName != null) && (!oldProjectName.equals(warName))) {
                        crawlerConfig.setCrawlChecktime(dateString);
                    }
                    crawlerConfig.setCrawlType(1);
                    //修改当前所在定时器的位置
                    crawlerConfig.setTimerName(warName);
                    crawlerConfigDao.save(crawlerConfig);
                    logger.info("配置信息修改成功");
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }

}



