package com.bidizhaobiao.data.Crawl.service.impl.QX_01516;

import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
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
 * 程序员：郭建婷  日期：2022-02-15
 * 原网站：http://www.xmta.gov.cn/zc/zdly/zfxx/zzgg/
 * 主页：http://www.xmta.gov.cn
 **/

@Service("QX_01516_3_TudKcService")
public class QX_01516_3_TudKcService extends SpiderService implements PageProcessor {

    public Spider spider = null;

    public String listUrl = "http://www.xmta.gov.cn/zc/zdly/zfxx/zzgg/index.htm";

    public String baseUrl = "http://www.xmta.gov.cn";

    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(3).setTimeOut(30000).setSleepTime(20);
    // 网站编号
    public String sourceNum = "01516-3";
    // 网站名称
    public String sourceName = "同安区人民政府";
    // 信息源
    public String infoSource = "政府采购";
    // 设置地区
    public String area = "华东";
    // 设置省份
    public String province = "福建";
    // 设置城市
    public String city = "厦门市";
    // 设置县
    public String district = "同安区";
    // 设置CreateBy
    public String createBy = "郭建婷";

    public Pattern p = Pattern.compile("(\\d{4})(年|/|-)(\\d{1,2})(月|/|-)(\\d{1,2})");

    public Pattern p_p = Pattern.compile("view\\('(.*?)','(.*?)','(.*?)'\\)");

    public Site getSite() {
        return this.site.setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
    }

    public void startCrawl(int ThreadNum, int crawlType) {
        // 赋值
        serviceContextEvaluation();
        serviceContext.setCrawlType(crawlType);
        // 保存日志
        saveCrawlLog(serviceContext);
        // 启动爬虫
        spider = Spider.create(this).thread(ThreadNum)
                .setDownloader(new MyDownloader(serviceContext, false, listUrl));
        Request request = new Request(listUrl);
        spider.addRequest(request);
        serviceContext.setSpider(spider);
        spider.run();
        // 爬虫状态监控部分
        saveCrawlResult(serviceContext);
    }

    public void process(Page page) {
        String url = page.getUrl().toString();
        try {
            List<BranchNew> detailList = new ArrayList<BranchNew>();
            Thread.sleep(2000);
            // 判断是否是翻页连接
            if (url.contains("index")) {
                Document document = Jsoup.parse(page.getRawText().replace("&nbsp;", "").replace("&amp;", "&").replace("&ensp;", "").replace("<![CDATA[", "").replace("]]>", "").replace("&lt;", "<").replace("&gt;", ">"));
                if (serviceContext.getPageNum() == 1) {
                    String pageCount = page.getRawText();
                    String pageReg = "pageCount:'(\\d{1,5})',";
                    Pattern pagePattern = Pattern.compile(pageReg);
                    Matcher pageMatcher = pagePattern.matcher(pageCount);
                    if (pageMatcher.find()) {
                        pageCount = pageMatcher.group(1);
                    } else {
                        pageCount = "1";
                    }
                    int maxPage = Integer.valueOf(pageCount);
                    serviceContext.setMaxPage(maxPage);
                }
                Elements lis = document.select("div.gl_r_ul > ul > li:has(a)");
                if (lis.size() > 0) {
                    for (Element li : lis) {
                        Element a = li.select("a").first();
                        String title = "";
                        if (a.hasAttr("title")) {
                            title = a.attr("title").trim();
                            if (title.equals("")) {
                                title = a.text().trim();
                            }
                        } else {
                            title = a.text().trim();
                        }
                        title = title.replace("...", "").replace(" ", "").trim();
                        String href = a.attr("href").trim();
                        String id = listUrl.substring(listUrl.indexOf(".cn/") + 3, listUrl.lastIndexOf("/") + 1).trim() + href.replace("./", "").trim();
                        String link = baseUrl + id;
                        String detailLink = link;
                        String date = li.select("span").last().text().replace(".", "-").trim();
                        dealWithNullTitleOrNullId(serviceContext, title, id);
                        BranchNew branch = new BranchNew();
                        branch.setId(id);
                        branch.setDate(date);
                        serviceContext.setCurrentRecord(id);
                        branch.setDetailLink(detailLink);
                        branch.setLink(link);
                        branch.setTitle(title);
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
                if (serviceContext.getPageNum() < serviceContext.getMaxPage() && serviceContext.isNeedCrawl()) {
                    serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                    page.addTargetRequest(listUrl.replace("index", "index_" + (serviceContext.getPageNum() - 1)));
                }
            } else {
                // 列表页请求
                BranchNew branchNew = map.get(url);
                if (branchNew == null) {
                    return;
                }
                String homeUrl = baseUrl;
                String title = branchNew.getTitle();
                String id = branchNew.getId();
                serviceContext.setCurrentRecord(id);
                String date = branchNew.getDate();
                String detailLink = branchNew.getDetailLink();
                String content = "";
                Document document = Jsoup.parse(page.getRawText().replace("&nbsp;", "").replace("&amp;", "&").replace("&ensp;", "").replace("<![CDATA[", "").replace("]]>", "").replace("&lt;", "<").replace("&gt;", ">"));
                Element contentE = document.select("div.content-wrap").first();
                Elements elements = contentE.getAllElements();
                for (Element element : elements) {
                    element.removeAttr("style");
                }
                contentE.select("div.xl_top").remove();
                contentE.select("iframe").remove();
                contentE.select("meta").remove();
                contentE.select("link").remove();
                contentE.select("button").remove();
                contentE.select("input").remove();
                contentE.select("*[style~=^.*display\\s*:\\s*none\\s*(;\\s*[0-9A-Za-z]+|;\\s*)?$]").remove();
                contentE.select("script").remove();
                contentE.select("style").remove();
                if (contentE.select("a") != null) {
                    Elements as = contentE.select("a");
                    for (Element a : as) {
                        a.attr("rel", "noreferrer");
                        String href = a.attr("href");
                        if (!href.contains("@") && !"".equals(href) && !href.contains("javascript") && !href.contains("http") && !href.contains("#")) {
                            if (href.contains("../")) {
                                href = homeUrl + "/" + href.replace("../", "");
                                a.attr("href", href);
                            } else if (href.startsWith("/")) {
                                href = homeUrl + href;
                                a.attr("href", href);
                            } else if (href.startsWith("./")) {
                                href = url.substring(0, url.lastIndexOf("/") + 1) + href.replace("./", "");
                                a.attr("href", href);
                            } else if (href.startsWith(" ")) {
                                href = url.substring(0, url.lastIndexOf("/")) + href.replace(" ", "");
                                a.attr("href", href);
                            } else {
                                href = homeUrl + "/" + href;
                                a.attr("href", href);
                            }
                        }
                        if (a.attr("href").equals("")) {
                            a.removeAttr("href");
                        }
                    }
                }
                if (contentE.select("img").first() != null) {
                    Elements imgs = contentE.select("img");
                    for (Element img : imgs) {
                        String src = img.attr("src");
                        if (!src.contains("javascript") && !"".equals(src) && !src.contains("http") && !src.contains("data:image")) {
                            if (src.contains("../")) {
                                src = homeUrl + "/" + src.replace("../", "");
                                img.attr("src", src);
                            } else if (src.startsWith("/")) {
                                src = homeUrl + src;
                                img.attr("src", src);
                            } else if (src.startsWith("./")) {
                                src = url.substring(0, url.lastIndexOf("/") + 1) + src.replace("./", "");
                                img.attr("src", src);
                            } else if (src.startsWith(" ")) {
                                src = url.substring(0, url.lastIndexOf("/")) + src.replace(" ", "");
                                img.attr("src", src);
                            } else {
                                src = homeUrl + "/" + src;
                                img.attr("src", src);
                            }
                        }
                        if (img.attr("src").equals("")) {
                            img.removeAttr("src");
                        }
                    }
                }
                if (contentE.select("a[href*=javascript]").first() != null) {
                    Elements as = contentE.select("a[href*=javascript]");
                    for (Element a : as) {
                        a.removeAttr("href");
                    }
                }
                if (contentE.select("a[href*=#]").first() != null) {
                    Elements as = contentE.select("a[href*=#]");
                    for (Element a : as) {
                        a.removeAttr("href");
                    }
                }
                content = "<div>" + title + "</div>" + contentE.outerHtml();
                RecordVO recordVO = new RecordVO();
                recordVO.setId(id);
                recordVO.setDate(date);
                recordVO.setContent(content.replace("未经授权，禁止转载！", ""));
                recordVO.setTitle(title.replaceAll("\\<.*?\\>", ""));//详情页标题
                recordVO.setDetailLink(detailLink);//详情页链接
                dataStorage(serviceContext, recordVO, branchNew.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
            dealWithError(url, serviceContext, e);
        }
    }


}
