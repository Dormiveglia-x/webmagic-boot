package com.bidizhaobiao.data.Crawl.service.impl.QX_02302;

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
 * 程序员：白嘉全 日期：2022-03-11
 * 原网站：https://www.anxiang.gov.cn/axzx/gsgg_1
 * 主页：https://www.anxiang.gov.cn
 **/
@Service("QX_02302_ZhaobGgService")
public class QX_02302_ZhaobGgService extends SpiderService implements PageProcessor {

    public Spider spider = null;

    public String listUrl = "https://www.anxiang.gov.cn/axzx/gsgg_1";

    public String baseUrl = "https://www.anxiang.gov.cn";

    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(3).setTimeOut(30000).setSleepTime(20);
    // 网站编号
    public String sourceNum = "02302";
    // 网站名称
    public String sourceName = "安乡县人民政府";
    // 信息源
    public String infoSource = "政府采购";
    // 设置地区
    public String area = "华中";
    // 设置省份
    public String province = "湖南";
    // 设置城市
    public String city = "常德市";
    // 设置县
    public String district = "安乡县";
    // 设置CreateBy
    public String createBy = "白嘉全";
    //附件
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
        Request request = new Request("https://www.baidu.com/?wd=" + listUrl);
        spider.addRequest(request);
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
            Document document = Jsoup.parse(pageHtml);
            // 判断是否是翻页连接
            if (!url.contains("content")) {
                Elements lis = null;
                if (document.select(".newsList").first() != null) {
                    lis = document.select(".newsList").first().select("li:has(a)");
                    if (lis.size() > 0) {
                        for (int i = 0; i < lis.size(); i++) {
                            Element li = lis.get(i);
                            Element a = li.select("a").first();
                            String title = "";
                            if (a.hasAttr("title")) {
                                title = a.attr("title").trim();
                            } else {
                                title = a.text().trim();
                            }
                            String str = "招商、询标、交易、机构、需求、废旧、废置、处置、报废、供应商、承销商、服务商、调研、优选、择选、择优、选取、公选、选定、摇选、摇号、摇珠、抽选、定选、定点、招标、采购、询价、询比、竞标、竞价、竞谈、竞拍、竞卖、竞买、竞投、竞租、比选、比价、竞争性、谈判、磋商、投标、邀标、议标、议价、单一来源、标段、明标、明投、出让、转让、拍卖、招租、出租、预审、发包、承包、分包、外包、开标、遴选、答疑、补遗、澄清、延期、挂牌、变更、预公告、监理、改造工程、报价、小额、零星、自采、商谈";
                            String[] split = str.split("、");
                            if (!title.contains("...") && !CheckProclamationUtil.isProclamationValuable(title, split)) {
                                continue;
                            }
                            String href = a.attr("href").trim();
                            String link = "";
                            if (href.contains("http")) {
                                link = href;
                                href = href.substring(href.indexOf("?") + 1, href.length());
                            } else {
                                if (href.contains("./")) {
                                    href = href.substring(href.lastIndexOf("./") + 1, href.length());
                                }
                                link = baseUrl + href;
                            }
                            String id = href.substring(href.lastIndexOf("/") + 1);
                            String detailLink = link;
                            String date = li.text().trim().replaceAll("[.|/|年|月]", "-");
                            Matcher m = p.matcher(date);
                            if (m.find()) {
                                date = SpecialUtil.date2Str(SpecialUtil.str2Date(m.group()));
                            }
                            dealWithNullTitleOrNullId(serviceContext, title, id);
                            BranchNew branch = new BranchNew();
                            branch.setTitle(title);
                            branch.setId(id);
                            branch.setDetailLink(detailLink);
                            branch.setLink(link);
                            branch.setDate(date);
                            detailList.add(branch);
                        }
                        // 校验数据,判断是否需要继续触发爬虫
                        List<BranchNew> needCrawlList = checkData(detailList, serviceContext);
                        for (BranchNew branchNew : needCrawlList) {
                            map.put(branchNew.getLink(), branchNew);
                            page.addTargetRequest("https://www.baidu.com/?wd=" + branchNew.getLink());
                        }
                    }
                } else {
                    dealWithNullListPage(serviceContext);
                }
                if (serviceContext.getPageNum() == 1) {
                    //document.select("div.page_list").last().select("a").last().remove();
                    Element pageElement = document.select("a.last").last();
                    String pages = pageElement.attr("href").replaceAll("[\u00a0\u1680\u180e\u2000-\u200a\u2028\u2029\u202f\u205f\u3000\ufeff\\s+]", "");
                    pages = pages.substring(pages.lastIndexOf("_") + 1, pages.length());
                    int maxPage = Integer.parseInt(pages);
                    serviceContext.setMaxPage(maxPage);
                }
                if (serviceContext.getPageNum() < serviceContext.getMaxPage() && serviceContext.isNeedCrawl()) {
                    serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                    page.addTargetRequest("https://www.baidu.com/?wd=" + listUrl.replace("_1", "_" + serviceContext.getPageNum()));
                }
            } else {
                // 列表页请求
                BranchNew branchNew = map.get(url);
                if (branchNew == null) {
                    return;
                }
                String homeUrl = baseUrl;
                String title = Jsoup.parse(branchNew.getTitle()).text();
                String id = branchNew.getId();
                String date = branchNew.getDate();
                String detailLink = branchNew.getDetailLink();
                String content = "";
                String detailContent = page.getRawText();
                if (document.select("h2.title").first() == null) {
                    title = title.trim().replace(" ", "").replace("...", "");
                } else {
                    title = document.select("h2.title").first().text().trim().replace(" ", "").replace("...", "");
                }
                String str = "招商、询标、交易、机构、需求、废旧、废置、处置、报废、供应商、承销商、服务商、调研、优选、择选、择优、选取、公选、选定、摇选、摇号、摇珠、抽选、定选、定点、招标、采购、询价、询比、竞标、竞价、竞谈、竞拍、竞卖、竞买、竞投、竞租、比选、比价、竞争性、谈判、磋商、投标、邀标、议标、议价、单一来源、标段、明标、明投、出让、转让、拍卖、招租、出租、预审、发包、承包、分包、外包、开标、遴选、答疑、补遗、澄清、延期、挂牌、变更、预公告、监理、改造工程、报价、小额、零星、自采、商谈";
                String[] split = str.split("、");
                if (!title.contains("...") && !CheckProclamationUtil.isProclamationValuable(title, split)) {
                    return;
                }
                Element contentE = null;
                if (document.select(".conTxt").first() != null) {
                    contentE = document.select(".conTxt").first();
                }
                contentE.select("*[style~=^.*display\\s*:\\s*none\\s*(;\\s*[0-9A-Za-z]+|;\\s*)?$]").remove();
                contentE.select("script").remove();
                contentE.select("style").remove();
                //contentE.select("iframe").remove();
                if (contentE.select("a") != null) {
                    Elements as = contentE.select("a");
                    for (Element a : as) {
                        String href = a.attr("href");
                        if (href.contains("C:")) {
                            a.remove();
                        }
                        if (!href.contains("@") && !"".equals(href) && !href.contains("javascript") && !href.contains("http") && !href.contains("#")) {
                            if (href.contains("./")) {
                                href = homeUrl + "/" + href.substring(href.lastIndexOf("./") + 1, href.length());
                                a.attr("href", href);
                            } else if (href.startsWith("/")) {
                                href = homeUrl + href;
                                a.attr("href", href);
                            } else {
                                href = homeUrl + "/" + href;
                                a.attr("href", href);
                            }
                        }
                        if (as.attr("onclick").length() > 4) {
                            String hrefs = as.attr("onclick").trim();
                            hrefs = hrefs.substring(hrefs.indexOf("'") + 1, hrefs.lastIndexOf("'"));
                            hrefs = homeUrl + hrefs;
                            a.removeAttr("onclick");
                            a.attr("href", hrefs);
                        }
                    }
                }
                if (contentE.select("img").first() != null) {
                    Elements imgs = contentE.select("img");
                    for (Element img : imgs) {
                        String src = img.attr("src");
                        if (src.contains("C:")) {
                            img.remove();
                        }
                        if (!src.contains("javascript") && !"".equals(src) && !src.contains("http")) {
                            if (src.contains("./")) {
                                src = homeUrl + "/" + src.substring(src.lastIndexOf("./") + 1, src.length());
                                img.attr("src", src);
                            } else if (src.startsWith("/")) {
                                src = homeUrl + src;
                                img.attr("src", src);
                            } else {
                                src = homeUrl + "/" + src;
                                img.attr("src", src);
                            }
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
                content = "<div>" + title + "</div><br>" + contentE.outerHtml();
                detailContent = detailContent != null ? detailContent : Jsoup.parse(content).html();
                RecordVO recordVO = new RecordVO();
                recordVO.setId(id);
                recordVO.setListTitle(title);
                recordVO.setDate(date);
                recordVO.setContent(content.replaceAll("\\u2002", ""));
                recordVO.setTitle(title);//详情页标题
                //recordVO.setDdid(SpecialUtil.stringMd5(detailContent));//详情页md5
                recordVO.setDetailLink(detailLink);//详情页链接
                recordVO.setDetailHtml(detailContent);
                dataStorage(serviceContext, recordVO, branchNew.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
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
