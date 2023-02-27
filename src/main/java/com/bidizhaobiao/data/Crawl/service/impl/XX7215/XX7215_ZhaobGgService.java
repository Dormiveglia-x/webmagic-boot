package com.bidizhaobiao.data.Crawl.service.impl.XX7215;

import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.regex.Pattern;

/**
 * 日期: 2023/2/27
 * 原网站: http://www.jsjhzx.com/index/xxgg.htm
 * 主页: http://www.jsjhzx.com/index.jsp
 */

public class XX7215_ZhaobGgService extends SpiderService implements PageProcessor {
    public Spider spider = null;

    public String listUrl = "http://www.jsjhzx.com/index/xxgg.htm";

    public String baseUrl = "http://www.jsjhzx.com";

    // 网站编号
    public String sourceNum = "XX7215";
    // 网站名称
    public String sourceName = "徐州市铜山区夹河中学";

    // 信息源
    public String infoSource = "政府采购";
    // 设置地区
    public String area = "华东";
    // 设置省份
    public String province = "江苏";
    // 设置城市
    public String city = "徐州市";
    // 设置县
    public String district = "铜山区";
    // 设置CreateBy
    public String createBy = "何康";

    public Pattern p = Pattern.compile("(\\d{4})(年|/|-)(\\d{1,2})(月|/|-)(\\d{1,2})");

    @Override
    public void startCrawl(int threadNum, int crawlType) {
        serviceContextEvaluation();
        serviceContext.setCrawlType(crawlType);
        // 保存日志
        saveCrawlLog(serviceContext);
        // 启动爬虫
        spider = Spider.create(this).thread(threadNum)
                .setDownloader(new MyDownloader(serviceContext, false, listUrl));
        Request request = new Request(listUrl);
        spider.addRequest(request);
        serviceContext.setSpider(spider);
        spider.run();
        // 爬虫状态监控部分
        saveCrawlResult(serviceContext);
    }

    @Override
    public void process(Page page) {

    }

    @Override
    public Site getSite() {
        return Site.me()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36")
                .setTimeOut(3000)
                .setRetryTimes(3);
    }
}
