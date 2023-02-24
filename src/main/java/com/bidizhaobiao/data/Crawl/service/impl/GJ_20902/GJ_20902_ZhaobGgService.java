package com.bidizhaobiao.data.Crawl.service.impl.GJ_20902;

import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
import com.bidizhaobiao.data.Crawl.utils.CheckProclamationUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 程序员：周素茵 日期：2022-03-11
 * 原网站：http://www.cpca.cn/site/term/57.html
 * 主页：http://www.cpca.cn
 **/
@Service
public class GJ_20902_ZhaobGgService extends SpiderService implements PageProcessor {
    public Spider spider = null;

    public String listUrl = "http://www.cpca.cn/site/term/57.html?page=1&per-page=10";
    public String baseUrl = "http://www.cpca.cn";
    public Pattern datePat = Pattern.compile("(\\d{4})(年|/|-|\\.)(\\d{1,2})(月|/|-|\\.)(\\d{1,2})");

    // 网站编号
    public String sourceNum = "20902";
    // 网站名称
    public String sourceName = "中国卫生有害生物防制协会";
    // 信息源
    public String infoSource = "政府采购";
    // 设置地区
    public String area = "全国";
    // 设置省份
    public String province;
    // 设置城市
    public String city;
    // 设置县
    public String district;
    public String createBy = "周素茵";
    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(2).setTimeOut(30000).setSleepTime(20);

    public Site getSite() {
        return this.site;
    }

    public void startCrawl(int ThreadNum, int crawlType) {
        // 赋值
        serviceContextEvaluation();
        // 保存日志
        serviceContext.setCrawlType(crawlType);
        saveCrawlLog(serviceContext);
        // 启动爬虫
        spider = Spider.create(this).thread(ThreadNum)
                .setDownloader(new MyDownloader(serviceContext, false, listUrl));
        spider.addRequest(new Request(listUrl));
        serviceContext.setSpider(spider);
        spider.run();
        // 爬虫状态监控部分
        saveCrawlResult(serviceContext);
    }

    public void process(Page page) {
        String url = page.getUrl().toString();
        try {
            List<BranchNew> detailList = new ArrayList<BranchNew>();
            Thread.sleep(500);
            if (url.contains("?page=")) {
                Document doc = Jsoup.parse(page.getRawText());
                Elements listElement = doc.select(".List_one>li:has(a)");
                if (listElement.size() > 0) {
                    for (Element element : listElement) {
                        Element a = element.select("a").first();
                        String link = a.attr("href").trim();
                        String id = link;
                        link = baseUrl + link;
                        String detailLink = link;
                        String date = "";
                        Matcher dateMat = datePat.matcher(element.select("span.Right").text());
                        if (dateMat.find()) {
                            date = dateMat.group(1);
                            date += dateMat.group(3).length() == 2 ? "-" + dateMat.group(3) : "-0" + dateMat.group(3);
                            date += dateMat.group(5).length() == 2 ? "-" + dateMat.group(5) : "-0" + dateMat.group(5);
                        }
                        String title = a.attr("title").trim();
                        if (title.length() < 2) title = a.text().trim().replace("...", "");
                        if (!CheckProclamationUtil.isProclamationValuable(title, null)) {
                            continue;
                        }
                        String content = "<div>附件下载：<a href=" + detailLink + ">" + title + "</a></div>";
                        BranchNew branch = new BranchNew();
                        branch.setId(id);
                        serviceContext.setCurrentRecord(branch.getId());
                        branch.setLink(link);
                        branch.setDetailLink(detailLink);
                        branch.setDate(date);
                        branch.setTitle(title);
                        branch.setContent(content);
                        detailList.add(branch);
                    }
                    List<BranchNew> branchNewList = checkData(detailList, serviceContext);
                    for (BranchNew branch : branchNewList) {
                        map.put(branch.getLink(), branch);
                        RecordVO recordVO = new RecordVO();
                        recordVO.setId(branch.getId());
                        recordVO.setListTitle(branch.getTitle());
                        recordVO.setTitle(branch.getTitle());
                        recordVO.setDetailLink(branch.getDetailLink());
                        recordVO.setDate(branch.getDate());
                        recordVO.setContent(branch.getContent().replaceAll("\\ufeff|\\u2002|\\u200b|\\u2003", ""));
                        dataStorage(serviceContext, recordVO, branch.getType());
                    }
                } else {
                    dealWithNullListPage(serviceContext);
                }
                Element nextPage = doc.select("a:contains(下一页)").first();
                if (nextPage != null && nextPage.attr("href").contains("?page=") && serviceContext.isNeedCrawl()) {
                    String href = baseUrl + nextPage.attr("href").trim();
                    serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                    page.addTargetRequest(href);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            dealWithError(url, serviceContext, e);
        }
    }


}
