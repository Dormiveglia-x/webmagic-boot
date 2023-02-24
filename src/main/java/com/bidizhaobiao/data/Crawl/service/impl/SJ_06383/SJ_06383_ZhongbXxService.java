package com.bidizhaobiao.data.Crawl.service.impl.SJ_06383;

import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
import com.bidizhaobiao.data.Crawl.utils.CheckProclamationUtil;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 程序员：陈广艺
 * 日期：2022-02-09
 * 主页：https://cstm.cdstm.cn/bgs/gg/
 **/

@Service
public class SJ_06383_ZhongbXxService extends SpiderService implements PageProcessor {
    public Spider spider = null;

    public String listUrl = "https://cstm.cdstm.cn/bgs/gg/index.html";

    public String baseUrl = "https://cstm.cdstm.cn";

    public Map<String, BranchNew> map = new HashMap<>();
    // 网站编号
    public String sourceNum = "06383";
    // 网站名称
    public String sourceName = "中国科学技术馆";
    // 设置地区
    public String area = "华北";
    // 设置省份
    public String province = "北京";
    // 设置城市
    public String city;
    // 设置县
    public String district;
    // 设置县
    public String createBy = "陈广艺";
    // 信息源
    public String infoSource = "政府采购";
    public Pattern pDate = Pattern.compile("(\\d{4})(年|/|-|\\.)(\\d{1,2})(月|/|-|\\.)(\\d{1,2})");
    public Pattern pMaxPage = Pattern.compile("createPageHTML\\((\\d+),");
    // 抓取网站的相关配置，包括：编码","抓取间隔","重试次数等
    Site site = Site.me().setCycleRetryTimes(2).setTimeOut(30000).setSleepTime(20);

    // 信息源
    public Site getSite() {

        return this.site;//.setUserAgent("Mozilla/5.0 (Linux; Android 6.0.1; Nexus 5X Build/MMB29P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.96 Mobile Safari/537.36 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
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
            if (url.contains("index")) {
                Element div = doc.select("ul[class=fen-right-list]").first();
                if (div != null) {
                    Elements lis = div.select("li:has(a)");
                    if (lis.size() > 0) {
                        for (int i = 0; i < lis.size(); i++) {
                            Element a = lis.get(i).select("a").first();
                            String id = a.attr("href").trim().substring(1);
                            String href = url.substring(0, url.lastIndexOf("/")) + id;
                            String title = "";
                            if (a.hasAttr("title")) {
                                title = a.attr("title").trim();
                            } else {
                                title = a.text().trim().replace("...", "");
                            }
                            if (!CheckProclamationUtil.isProclamationValuable(title)) {
                            	continue;
                            }
                            String date = lis.get(i).text().trim();
                            Matcher m = pDate.matcher(date);
                            if (m.find()) {
                                String month = m.group(3).length() == 2 ? m.group(3) : "0" + m.group(3);
                                String day = m.group(5).length() == 2 ? m.group(5) : "0" + m.group(5);
                                date = m.group(1) + "-" + month + "-" + day;
                                if (Integer.parseInt(m.group(1)) < 2016) {
                                    continue;
                                }
                            }
                            BranchNew branch = new BranchNew();
                            branch.setId(id);
                            branch.setLink(href);
                            branch.setTitle(title);
                            branch.setDate(date);
                            serviceContext.setCurrentRecord(id);
                            detailList.add(branch);
                        }
                        // 校验数据,判断是否需要继续触发爬虫
                        List<BranchNew> needCrawlList = checkData(detailList, serviceContext);
                        for (BranchNew branch : needCrawlList) {
                            map.put(branch.getLink(), branch);
                            page.addTargetRequest("https://www.baidu.com/?wd=" + branch.getLink());
                        }
                    } else {
                        dealWithNullListPage(serviceContext);
                    }
                } else {
                    dealWithNullListPage(serviceContext);
                }
                if (serviceContext.getPageNum() == 1) {
                    int maxPage = 0;
                    Matcher m = pMaxPage.matcher(doc.toString());
                    if (m.find()) {
                        maxPage = Integer.parseInt(m.group(1));
                        serviceContext.setMaxPage(maxPage);//((maxPage-1)/15+1);
                    }
                }
                if (serviceContext.getPageNum() < serviceContext.getMaxPage() && serviceContext.isNeedCrawl()) {
                    String nextPage = listUrl.replace("index", "index_" + serviceContext.getPageNum());
                    serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                    page.addTargetRequest("https://www.baidu.com/?wd=" + nextPage);
                }
            } else {
                String content = "";
                BranchNew bn = map.get(url);
                if (bn != null) {
                    String title = bn.getTitle();
                    String date = bn.getDate();
                    Element subject = doc.select("div[class=fen-info-cont]").first();
                    if (subject != null) {
                        Elements aList = subject.select("a");
                        for (Element a : aList) {
                            String href = a.attr("href");
                            if (href.startsWith("mail") || href.startsWith("HTTP")) {
                                continue;
                            }
                            if (href.startsWith("javascript") || href.contains("file:///C:/")) {
                                a.remove();
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
                                if (href.indexOf("//") == 0) {
                                    href = "http:" + href;
                                    a.attr("href", href);
                                } else if (href.indexOf("/") == 0) {
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
                        Elements imgList = subject.select("IMG");
                        for (Element img : imgList) {
                            String href = img.attr("src");
                            if (href.contains("file:///C:/")) {
                                img.remove();
                                continue;
                            }
                            if (href.startsWith("HTTP:")) {
                                continue;
                            }
                            if (href.length() > 10 && href.indexOf("http") != 0) {
                                if (href.indexOf("//") == 0) {
                                    href = "http:" + href;
                                    img.attr("src", href);
                                } else if (href.indexOf("../") == 0) {
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
                        subject.select("iframe").remove();
                        content = title + "<br>" + subject.html();
                    } else if (url.contains(".pdf")) {
                        content = title + "<br>详情链接：<a href='" + url + "'>" + title + "</a>";
                    }
                    RecordVO recordVO = new RecordVO();
                    recordVO.setId(bn.getId());
                    recordVO.setListTitle(title);
                    recordVO.setTitle(title);
                    recordVO.setDetailLink(url);
                    recordVO.setDetailHtml(detailHtml);
                    recordVO.setDate(date);
                    recordVO.setContent(content);
                    serviceContext.setCurrentRecord(bn.getId());
                    dataStorage(serviceContext, recordVO, bn.getType());
                }
            }
        } catch (Exception e) {
            dealWithError(url, serviceContext, e);
        }
    }

    public String getContent(String href) {
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = null;
        String toString = "";
        try {
            httpClient = getHttpClient(true, false);
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
