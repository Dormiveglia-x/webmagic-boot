package com.bidizhaobiao.data.Crawl.service.impl.DX011683
        ;

import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
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
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @程序员: 潘嘉明 日期：2022-02-15 13:38
 * @原网站: http://www.cqgic.com/aspx/ch/newslist.aspx?classid=162
 * @主页：TODO
 **/
@Service("DX011683_ZhaobGgService")
public class DX011683_ZhaobGgService extends SpiderService implements PageProcessor {

    public Spider spider = null;

    public String listUrl = "http://www.cqgic.com/aspx/ch/newslist.aspx?classid=162";

    public String baseUrl = "http://www.cqgic.com";
    // 网站编号
    public String sourceNum = "DX011683";
    // 网站名称
    public String sourceName = "重庆通用工业（集团）有限责任公司";
    // 信息源
    public String infoSource = "企业采购";
    // 设置地区
    public String area = "西南";
    // 设置省份
    public String province = "重庆";
    // 设置城市
    public String city = "";
    // 设置县
    public String district = "南岸";
    // 设置CreateBy
    public String createBy = "潘嘉明";

    public boolean isNeedSaveFileAddSSL = true;

    public Pattern p = Pattern.compile("(\\d{4})(年|/|-)(\\d{1,2})(月|/|-)(\\d{1,2})");

    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(3).setTimeOut(50000).setSleepTime(20);

    public Site getSite() {
        return this.site
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36");
    }

    @Override
    public void startCrawl(int threadNum, int crawlType) {
        // 赋值
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
        String url = page.getUrl().toString();
        String html = page.getHtml().toString();
        try {
            if (url.contains("newslist")) {
                Document document = Jsoup.parse(html);
                Elements lis = document.select("ul.newslist li:has(a)");
                List<BranchNew> detailList = new ArrayList<BranchNew>();
                if (lis.size() > 0) {
                    for (int i = 0; i < lis.size(); i++) {
                        Element li = lis.get(i);
                        Element a = li.select("a").first();
                        a.select("span").remove();
                        String title = a.text().trim();

                        String href = a.attr("href").trim();
                        String id = href;
                        String link = baseUrl + href;
                        String detailLink = link;
                        String date = "";

                        dealWithNullTitleOrNullId(serviceContext, title, id);
                        BranchNew branch = new BranchNew();
                        branch.setId(id);
                        branch.setTitle(title);
                        serviceContext.setCurrentRecord(id);
                        branch.setDetailLink(detailLink);
                        branch.setLink(link);
                        branch.setDate(date);
                        detailList.add(branch);
                    }
                    // 校验数据,判断是否需要继续触发爬虫
                    List<BranchNew> needCrawlList = checkData(detailList, serviceContext);
                    for (BranchNew branch : needCrawlList) {
                        map.put(branch.getDetailLink(), branch);
                        page.addTargetRequest(branch.getLink());
                    }
                } else {
                    dealWithNullListPage(serviceContext);
                }
//                if (serviceContext.getPageNum() < serviceContext.getMaxPage() && serviceContext.isNeedCrawl()) {
//                    serviceContext.setPageNum(serviceContext.getPageNum() + 1);
//                    page.addTargetRequest(listUrl.replace("1383-4965-1", "1383-4965-" + serviceContext.getPageNum()));
//                }
            } else {
                BranchNew branchNew = map.get(url);
                if (branchNew == null) {
                    return;
                }
                String title = Jsoup.parse(branchNew.getTitle()).text().replace("...", "");
                String id = branchNew.getId();
                serviceContext.setCurrentRecord(id);
                String detailLink = branchNew.getDetailLink();
                String detailTitle = title;
                String date = branchNew.getDate();
                String content = "";
                Document document = Jsoup.parse(html);
                Element dateE = document.select("div.time").first();
                if (dateE != null) {
                    String dateStr = dateE.text();
                    Matcher matcher = p.matcher(dateStr);
                    if (matcher.find()) {
                        date = matcher.group().replaceAll("[\\.|年|月|/]", "-");
                        date = SpecialUtil.date2Str(SpecialUtil.str2Date(date));
                    }
                }
                Element contentE = document.select("div.newsp").first();
                if (contentE != null) {
                    contentE.select("iframe").remove();
                    contentE.select("input").remove();
                    contentE.select("button").remove();
                    contentE.select("script").remove();
                    if (contentE.select("a").first() != null) {
                        Elements as = contentE.select("a");
                        for (Element a : as) {
                            String href = a.attr("href");
                            if (href.startsWith("file:")) {
                                a.removeAttr("href");
                                continue;
                            }
                            if ("".equals(href) || href == null
                                    || href.indexOf("#") == 0
                                    || href.contains("javascript:") || href.contains("mailto:")) {
                                a.removeAttr("href");
                                continue;
                            }
                            if (!href.contains("@") && !"".equals(href) && !href.contains("javascript") && !href.contains("http") && !href.contains("#")) {
                                if (href.startsWith("../")) {
                                    href = baseUrl + "/" + href.substring(href.lastIndexOf("./") + 2, href.length());
                                    a.attr("href", href);
                                    a.attr("rel", "noreferrer");
                                } else if (href.startsWith("./")) {
                                    href = url.substring(0, url.lastIndexOf("/")) + href.replace("./", "/");
                                    a.attr("href", href);
                                    a.attr("rel", "noreferrer");
                                } else if (href.startsWith("/")) {
                                    href = baseUrl + href;
                                    a.attr("href", href);
                                    a.attr("rel", "noreferrer");
                                } else {
                                    href = url.substring(0, url.lastIndexOf("/") + 1) + href;
                                    a.attr("href", href);
                                    a.attr("rel", "noreferrer");
                                }
                            }
                        }
                    }
                    if (contentE.select("img").first() != null) {
                        Elements imgs = contentE.select("img");
                        for (Element img : imgs) {
                            if (img.hasAttr("word_img") && img.attr("word_img").contains("file:")) {
                                img.remove();
                                continue;
                            }
                            String src = img.attr("src");
                            if (src.contains("data:image")) {
                                continue;
                            }
                            if (src.startsWith("file:")) {
                                img.removeAttr("src");
                            }
                            if (!"".equals(src) && !src.contains("#") && !src.contains("javascript:") && !src.contains("http")) {
                                if (src.startsWith("//")) {
                                    src = "http:" + src;
                                    img.attr("src", src);
                                } else if (src.startsWith("../")) {
                                    src = baseUrl + "/" + src.substring(src.lastIndexOf("./") + 2, src.length());
                                    img.attr("src", src);
                                } else if (src.startsWith("/")) {
                                    src = baseUrl + src;
                                    img.attr("src", src);
                                } else if (src.startsWith("./")) {
                                    src = url.substring(0, url.lastIndexOf("/")) + src.replace("./", "/");
                                    img.attr("src", src);
                                } else {
                                    src = url.substring(0, url.lastIndexOf("/") + 1) + src;
                                    img.attr("src", src);
                                }
                            }
                        }
                    }
                    content = "<div>" + title + "</div>" + contentE.outerHtml();
                }
                if (url.contains(".doc") || url.contains(".pdf") || url.contains(".zip") || url.contains(".xls")) {
                    content = "<div>附件下载：<a href='" + url + "'>" + detailTitle + "</a></div>";
                    html = Jsoup.parse(content).toString();
                    date = SpecialUtil.date2Str(new Date());
                }
                RecordVO recordVO = new RecordVO();
                recordVO.setId(id);
                recordVO.setListTitle(title);
                recordVO.setDate(date);
                recordVO.setContent(content.replaceAll("\\ufeff|\\u2002|\\u200b|\\u2003", ""));
                recordVO.setTitle(detailTitle);//详情页标题
                recordVO.setDdid(SpecialUtil.stringMd5(html));//详情页md5
                recordVO.setDetailLink(detailLink);//详情页链接
                recordVO.setDetailHtml(html);
                logger.info("入库id==={}", id);
                dataStorage(serviceContext, recordVO, branchNew.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
            dealWithError(url, serviceContext, e);
        }
    }

}
