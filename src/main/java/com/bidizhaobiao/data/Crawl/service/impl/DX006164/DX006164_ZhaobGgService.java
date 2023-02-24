package com.bidizhaobiao.data.Crawl.service.impl.DX006164;

import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
import com.bidizhaobiao.data.Crawl.utils.SpecialUtil;
import org.apache.http.client.config.RequestConfig;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 程序员：陈广艺 日期：2022-02-15
 * 原网站：https://www.cpic.com.cn/aboutUs/gsdt/zxgg/
 * 主页：https://www.cpic.com.cn
 **/

@Service
public class DX006164_ZhaobGgService extends SpiderService implements PageProcessor {

    public Spider spider = null;

    public String listUrl = "https://www.cpic.com.cn/aboutUs/gsdt/zxgg/index.shtml";
    public String baseUrl = "https://www.cpic.com.cn";
    public Pattern dateRex = Pattern.compile("(\\d{4})(年|/|-|\\.)(\\d{1,2})(月|/|-|\\.)(\\d{1,2})");

    // 网站编号
    public String sourceNum = "DX006164";
    // 网站名称
    public String sourceName = "中国太平洋保险官网";
    // 信息源
    public String infoSource = "企业采购";
    // 设置地区
    public String area = "全国";
    // 设置省份
    public String province;
    // 设置城市
    public String city;
    // 设置县
    public String district;
    public String createBy = "陈广艺";
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
        spider = Spider.create(this).thread(ThreadNum).setDownloader(new MyDownloader(serviceContext, false, listUrl));
        spider.addRequest(new Request("https://www.baidu.com/?wd=" + listUrl));

        serviceContext.setSpider(spider);
        spider.run();
        // 爬虫状态监控部分
        saveCrawlResult(serviceContext);
    }

    public String getContent(String path) {
        String respStr = "";
        CloseableHttpClient client = null;
        CloseableHttpResponse res = null;
        HttpGet httpGet = new HttpGet(path);
        httpGet.addHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36");
        httpGet.addHeader("Connection", "close");
        httpGet.addHeader("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        try {
            client = getHttpClient(true, false);
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10 * 1000)
                    .setSocketTimeout(15 * 1000).build();
            httpGet.setConfig(requestConfig);
            res = client.execute(httpGet);
            if (res.getStatusLine().getStatusCode() == 200) {
                respStr = EntityUtils.toString(res.getEntity(), "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (res != null) {
                    res.close();
                }
                if (client != null) {
                    client.close();
                }
            } catch (IOException e) {

            }
        }
        return respStr;
    }

    public void process(Page page) {
        String url = page.getUrl().toString();
        String detailHtml = page.getHtml().toString();

        try {
            if (url.contains("wd=")) {
                url = url.substring(url.indexOf("wd=") + 3);
            }
            List<BranchNew> detailList = new ArrayList<BranchNew>();
            Thread.sleep(1000);
            String html = getContent(url);
            int times = 2;
            while ("".equals(html) && times > 0) {
                html = getContent(url);
                times--;
            }
            Document doc = Jsoup.parse(html);
            Thread.sleep(1000);
            if (url.contains("/index")) {
                Elements listElement = doc.select("ul.newsList li:has(a)");
                if (listElement.size() > 0) {
                    for (Element element : listElement) {
                        Element a = element.select("a").first();
                        String link = a.attr("href").trim();
                        String id = link.substring(link.lastIndexOf("/") + 1);
                        link = baseUrl + link;
                        String date = "";
                        Matcher dateMat = dateRex.matcher(link);
                        if (dateMat.find()) {
                            if (Integer.valueOf(dateMat.group(1)) < 2016) continue;
                            date = dateMat.group(1);
                            date += dateMat.group(3).length() == 2 ? "-" + dateMat.group(3) : "-0" + dateMat.group(3);
                            date += dateMat.group(5).length() == 2 ? "-" + dateMat.group(5) : "-0" + dateMat.group(5);
                        } else {
                            String dateStr = id.substring(0, id.indexOf("."));
                            date = new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("yyyyMMdd").parse(dateStr));
                        }
                        String title = a.attr("title").trim();
                        if ("".equals(title)) title = a.text().trim();
                        if (title.contains("增值税、声明")) continue;
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
                Element nextPage = doc.select("a:contains(下一页)").first();
                if (nextPage != null && serviceContext.isNeedCrawl()) {
                    String href = nextPage.attr("href").trim();
                    if (href.contains("index_") && !url.contains(href)) {
                        serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                        href = url.substring(0, url.lastIndexOf("/") + 1) + href;
                        page.addTargetRequest("https://www.baidu.com/?wd=" + href);
                    }
                }
            } else {
                BranchNew branch = map.get(url);
                if (branch != null) {
                    map.remove(url);
                    doc = Jsoup.parse(html);
                    String title = branch.getTitle().replace("...", "");
                    String date = branch.getDate();
                    String content = "";
                    if (doc.text().length() < 5) {
                        return;
                    }
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
                                href = url.substring(0, url.lastIndexOf("/")) + href.substring(href.lastIndexOf("./") + 1);
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
                                href = url.substring(0, url.lastIndexOf("/")) + href.substring(href.lastIndexOf("./") + 1);
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
                    Element contentElement = doc.select("div.paragraph").first();
                    if (contentElement != null) {
                        Element titleElement = doc.select("p.conTitle").first();
                        if (titleElement != null) {
                            titleElement.select("a").remove();
                            title = titleElement.text().trim();
                        }
                        contentElement.select("script").remove();
                        contentElement.select("style").remove();
                        content = title + contentElement.outerHtml();
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

}
