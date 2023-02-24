package com.bidizhaobiao.data.Crawl.service.impl.XX1575;

import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
import com.bidizhaobiao.data.Crawl.utils.CheckProclamationUtil;
import com.bidizhaobiao.data.Crawl.utils.SpecialUtil;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 程序员：许广衡 日期：2022-02-15
 * 原网站：http://ggbz.zjyc.edu.cn/zbdt.htm
 * 主页：http://www.zjyc.edu.cn
 **/

@Service
public class XX1575_ZhongbXxService extends SpiderService implements PageProcessor {

    public Spider spider = null;

    public String listUrl = "http://ggbz.zjyc.edu.cn/zbdt.htm";
    public String baseUrl = "http://ggbz.zjyc.edu.cn";
    public Pattern dateRex = Pattern.compile("(\\d{4})(年|/|-|\\.)(\\d{1,2})(月|/|-|\\.)(\\d{1,2})");

    // 网站编号
    public String sourceNum = "XX1575";
    // 网站名称
    public String sourceName = "浙江农林大学暨阳学院";
    // 信息源
    public String infoSource = "政府采购";
    // 设置地区
    public String area = "华东";
    // 设置省份
    public String province = "浙江";
    // 设置城市
    public String city = "绍兴市";
    // 设置县
    public String district = "诸暨市";
    public String createBy = "许广衡";
    // 站源类型
    public String taskType = "";
    // 站源名称
    public String taskName = "";
    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(2).setTimeOut(30000).setSleepTime(20);
    // 是否需要入广联达
    public boolean isNeedInsertGonggxinxi = false;
    // 信息源

    public Site getSite() {
        return this.site
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
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
        spider.addRequest(new Request("https://www.baidu.com/?wd=" + listUrl));
        serviceContext.setSpider(spider);
        spider.run();
        // 爬虫状态监控部分
        saveCrawlResult(serviceContext);
    }

    public void process(Page page) {
        String url = page.getUrl().toString();
        try {
            if (url.contains("wd=")) {
                url = url.substring(url.indexOf("wd=") + 3);
            }
            List<BranchNew> detailList = new ArrayList<BranchNew>();
            Thread.sleep(500);
            String pageHtml = getContent(url);
            int times = 2;
            while ("".equals(pageHtml) && times > 0) {
                pageHtml = getContent(url);
                times--;
            }
            String detailHtml = pageHtml;
            Document doc = Jsoup.parse(pageHtml);
            if (url.contains("/zbdt")) {
                Elements listElement = doc.select("div.main_conRCb li:has(a)");
                if (listElement.size() > 0) {
                    for (Element element : listElement) {
                        Element a = element.select("a").first();
                        String link = a.attr("href");
                        String id = link.substring(link.lastIndexOf("/") + 1);
                        if (link.startsWith("../")) {
                            link = baseUrl + link.substring(link.lastIndexOf("../") + 2);
                        } else {
                            link = url.substring(0, url.lastIndexOf("/") + 1) + link;
                        }
                        String date = "";
                        Matcher dateMat = dateRex.matcher(element.text());
                        if (dateMat.find()) {
                            date = dateMat.group(1);
                            date += dateMat.group(3).length() == 2 ? "-" + dateMat.group(3) : "-0" + dateMat.group(3);
                            date += dateMat.group(5).length() == 2 ? "-" + dateMat.group(5) : "-0" + dateMat.group(5);
                        }
                        String title = a.text().trim();
                        if (!CheckProclamationUtil.isProclamationValuable(title)) {
                            continue;
                        }
                        BranchNew branch = new BranchNew();
                        branch.setId(id);
                        branch.setLink(link);
                        branch.setDate(date);
                        branch.setTitle(title);
                        detailList.add(branch);
                    }
                    List<BranchNew> branchNewList = checkData(detailList, serviceContext);
                    for (BranchNew branch : branchNewList) {
                        map.put(branch.getLink(), branch);
                        page.addTargetRequest(new Request("https://www.baidu.com/?wd=" + branch.getLink()));
                    }
                } else {
                    dealWithNullListPage(serviceContext);
                }
                Element nextPage = doc.select("a:contains(下页)").first();
                if (nextPage != null && serviceContext.isNeedCrawl()) {
                    String href = nextPage.attr("href").trim();
                    if (!url.contains(href)) {
                        href = url.substring(0, url.lastIndexOf("/") + 1) + href;
                        serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                        page.addTargetRequest("https://www.baidu.com/?wd=" + href);
                    }
                }
            } else {
                BranchNew branch = map.get(url);
                if (branch != null) {
                    map.remove(url);
                    String title = branch.getTitle().replace("...", "");
                    String date = branch.getDate();
                    String content = "";
                    Elements aList = doc.select("a");
                    for (Element a : aList) {
                        String href = a.attr("href");
                        if (href.startsWith("mailto")) {
                            continue;
                        }
                        if (href.contains("javascript") || href.equals("#")) {
                            if (a.attr("onclick").contains("window.open('http")) {
                                String onclick = a.attr("onclick");
                                onclick = onclick.substring(onclick.indexOf("'") + 1, onclick.lastIndexOf("'"));
                                a.attr("href", onclick);
                                a.removeAttr("onclick");
                            } else {
                                a.removeAttr("href");
                                a.removeAttr("onclick");
                            }
                            continue;
                        }
                        if (href.length() > 10 && href.indexOf("http") != 0) {
                            if (href.indexOf("//www") == 0) {
                                href = baseUrl.substring(0, baseUrl.indexOf(":") + 1) + href;
                                a.attr("href", href);
                            } else if (href.indexOf("../") == 0) {
                                href = href.replace("../", "");
                                href = baseUrl + "/" + href;
                                a.attr("href", href);
                            } else if (href.startsWith("/")) {
                                href = baseUrl + href;
                                a.attr("href", href);
                            } else if (href.indexOf("./") == 0) {
                                href = url.substring(0, url.lastIndexOf("/") + 1) + href.substring(href.lastIndexOf("./") + 1);
                                a.attr("href", href);
                            } else {
                                href = url.substring(0, url.lastIndexOf("/") + 1) + href;
                                a.attr("href", href);
                            }
                        }
                    }
                    Elements imgList = doc.select("IMG");
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
                                href = url.substring(0, url.lastIndexOf("/") + 1) + href.substring(href.lastIndexOf("./") + 1);
                                img.attr("src", href);
                            } else if (href.startsWith("//www")) {
                                href = baseUrl.substring(0, baseUrl.indexOf(":") + 1) + href;
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
                    Element contentElement = doc.select("div.main_content").first();
                    if (contentElement != null) {
                        Element titleElement = contentElement.select("div.main_contit h2").first();
                        if (titleElement != null) {
                            title = titleElement.text().trim();
                        }
                        if (title.startsWith("【") && title.contains("】"))
                            title = title.substring(title.indexOf("】") + 1);
                        contentElement.select("div.main_contit p").remove();
                        contentElement.select("div.main_art").remove();
                        contentElement.select("script").remove();
                        contentElement.select("style").remove();
                        content = contentElement.outerHtml();
                    }
                    if (url.contains(".doc") || url.contains(".pdf") || url.contains(".zip") || url.contains(".xls")) {
                        content = "<div>附件下载：<a href='" + url + "'>" + branch.getTitle() + "</a></div>";
                        detailHtml = Jsoup.parse(content).toString();
                    }
                    RecordVO recordVO = new RecordVO();
                    recordVO.setId(branch.getId());
                    recordVO.setListTitle(branch.getTitle());
                    recordVO.setTitle(title);
                    recordVO.setDetailLink(url);
                    recordVO.setDetailHtml(detailHtml);
                    recordVO.setDdid(SpecialUtil.stringMd5(detailHtml));
                    recordVO.setDate(date);
                    recordVO.setContent(content);
                    dataStorage(serviceContext, recordVO, branch.getType());
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            dealWithError(url, serviceContext, e);
        }
    }

    public String getContent(String href) {
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = null;
        String toString = "";
        try {
            httpClient = getHttpClient(true, true);
            HttpGet httpGet = new HttpGet(href);
            httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36");
            response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                toString = EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                try {
                    response.close();
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return toString;
    }

}
