package com.bidizhaobiao.data.Crawl.service.impl.DS_07447;

import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
import com.bidizhaobiao.data.Crawl.utils.CheckProclamationUtil;
import com.bidizhaobiao.data.Crawl.utils.SpecialUtil;
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
 * 程序员：陆浩锐 日期：2022-02-15
 * 原网站：http://kfq.chuzhou.gov.cn/zbcg/index.html
 * 主页：http://kfq.chuzhou.gov.cn
 **/

@Service
public class DS_07447_ZhongbXxService extends SpiderService implements PageProcessor {

    public Spider spider = null;

    public String listUrl = "http://kfq.chuzhou.gov.cn/content/column/5305120?pageIndex=1";
    public String baseUrl = "http://kfq.chuzhou.gov.cn";
    public Pattern p = Pattern.compile("(\\d{4})(年|/|-|\\.)(\\d{1,2})(月|/|-|\\.)(\\d{1,2})");
    public Pattern pg = Pattern.compile("pageCount:(\\d+),");
    // 网站编号
    public String sourceNum = "07447";
    // 网站名称
    public String sourceName = "安徽滁州经济技术开发区管理委员会";
    // 信息源
    public String infoSource = "政府采购";
    // 设置地区
    public String area = "华东";
    // 设置省份
    public String province = "安徽";
    // 设置城市
    public String city = "滁州市";
    // 设置县
    public String district;
    public String createBy = "潘嘉明";
    // 站源类型
    public String taskType = "kfq_chuzhou_gov_cn_zbcg_4";
    // 站源名称
    public String taskName = "滁州经济技术开发区管委会-招标采购";
    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数
    Site site = Site.me().setCycleRetryTimes(2).setTimeOut(30000).setSleepTime(20);
    // 是否需要入广联达
    public boolean isNeedSaveFileAddSSL = true;
    // 信息源

    public Site getSite() {
        return this.site.setUserAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36");
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
            Thread.sleep(1000);
            if (url.contains("pageIndex")) {
                Document doc = page.getHtml().getDocument();
                Element ele = doc.select("div[class=listnews]").first();
                if (ele != null) {
                    Matcher mg = pg.matcher(ele.outerHtml());
                    if (mg.find()) {
                        int maxPage = Integer.parseInt(mg.group(1));
                        serviceContext.setMaxPage(maxPage);
                    }
                }
                Elements list = doc.select("ul.doc_list li:has(a)");
                if (list.size() > 0) {
                    for (int i = 0; i < list.size(); i++) {
                        Element li = list.get(i);
                        Element a = li.select("a").first();
                        String id = a.attr("href");
                        String link = id;
                        id = id.substring(id.lastIndexOf("/") + 1);
                        String title = a.attr("title").trim();
                        if (title == null || title.length() < 2) {
                            title = a.text().trim();
                        }
                        if (!CheckProclamationUtil.isProclamationValuable(title)) {
                            continue;
                        }
                        Matcher m = p.matcher(li.text());
                        String date = "";
                        if (m.find()) {
                            String month = m.group(3).length() == 2 ? m.group(3) : "0" + m.group(3);
                            String day = m.group(5).length() == 2 ? m.group(5) : "0" + m.group(5);
                            date = m.group(1) + "-" + month + "-" + day;
                        }
                        BranchNew branch = new BranchNew();
                        branch.setId(id);
                        serviceContext.setCurrentRecord(branch.getId());
                        branch.setLink(link);
                        branch.setTitle(title);
                        branch.setDate(date);
                        detailList.add(branch);
                    }
                    // 校验数据,判断是否需要继续触发爬虫
                    List<BranchNew> needCrawlList = checkData(detailList, serviceContext);
                    for (BranchNew branch : needCrawlList) {
                        map.put(branch.getLink(), branch);
                        page.addTargetRequest(branch.getLink());
                    }
                } else {
                    dealWithNullListPage(serviceContext);
                }
                // 翻页连接
                if (serviceContext.getPageNum() < serviceContext.getMaxPage() && serviceContext.isNeedCrawl()) {
                    serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                    String nextPage = listUrl.replace("pageIndex=1", "pageIndex=" + serviceContext.getPageNum());
                    page.addTargetRequest(nextPage);
                }
            } else {
                String detailHtml = page.getHtml().toString();
                String Content = "";
                BranchNew bn = map.get(url);
                if (bn != null) {
                    serviceContext.setCurrentRecord(bn.getId());
                    String Title = bn.getTitle();
                    String date = bn.getDate();
                    Document doc = Jsoup.parse(page.getRawText());
                    Element div = doc.select("div[id=zoom]").first();
                    if (div != null) {
                        Element tit = doc.select("h1.newstitle").first();
                        if (tit != null) {
                            Content = tit.outerHtml();
                            Title = tit.text().trim();
                        }
                        div.select("script").remove();
                        div.select("style").remove();
                        Elements aList = div.select("a");
                        for (Element a : aList) {
                            String href = a.attr("href");
                            if (href.startsWith("mailto")) {
                                continue;
                            }
                            if (href.length() > 10 && href.indexOf("http") != 0) {
                                if (href.indexOf("/") == 0) {
                                    href = baseUrl + href;
                                    a.attr("href", href);
                                } else if (href.indexOf("../") == 0) {
                                    href = href.replace("../", "");
                                    href = baseUrl + "/" + href;
                                    a.attr("href", href);
                                } else if (href.indexOf("./") == 0) {
                                    href = url.substring(0, url.lastIndexOf("/") + 1) + href.substring(2);
                                    a.attr("href", href);
                                } else {
                                    href = url.substring(0, url.lastIndexOf("/") + 1) + href;
                                    a.attr("href", href);
                                }
                            }
                        }
                        Elements imgList = div.select("IMG");
                        for (Element img : imgList) {
                            String href = img.attr("src");
                            if (href.contains("dkxx.png")) {
                                img.remove();
                                continue;
                            }
                            if (href.length() > 10 && href.indexOf("http") != 0) {
                                if (href.indexOf("../") == 0) {
                                    href = href.replace("../", "");
                                    href = baseUrl + "/" + href;
                                    img.attr("src", href);
                                } else if (href.indexOf("./") == 0) {
                                    href = url.substring(0, url.lastIndexOf("/") + 1) + href.substring(2);
                                    img.attr("src", href);
                                } else if (href.indexOf("/") == 0) {
                                    href = baseUrl + href;
                                    img.attr("src", href);
                                } else {
                                    href = url.substring(0, url.lastIndexOf("/") + 1) + href;
                                    img.attr("src", href);
                                }
                            }
                        }
                        Content += div.outerHtml();

                    } else {
                        if (url.contains(".doc") || url.contains(".pdf") || url.contains(".zip")) {
                            Content = "<div>附件下载：<a href='" + url + "'>" + Title + "</a></div>";
                        }
                    }
                    RecordVO recordVO = new RecordVO();
                    recordVO.setId(bn.getId());
                    recordVO.setListTitle(bn.getTitle());
                    recordVO.setTitle(Title);
                    recordVO.setDetailLink(url);
                    recordVO.setDetailHtml(detailHtml);
                    recordVO.setDdid(SpecialUtil.stringMd5(detailHtml));
                    recordVO.setDate(date);
                    recordVO.setContent(Content);
                    dataStorage(serviceContext, recordVO, bn.getType());
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            dealWithError(url, serviceContext, e);
        }
    }
}
