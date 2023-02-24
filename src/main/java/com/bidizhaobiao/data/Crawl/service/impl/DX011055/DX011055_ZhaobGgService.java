package com.bidizhaobiao.data.Crawl.service.impl.DX011055
        ;

import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
import com.bidizhaobiao.data.Crawl.utils.CheckProclamationUtil;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @程序员: 潘嘉明 日期：2022-03-03 13:38
 * @原网站: http://www.gzxqgroup.cn/news/10/
 * @主页：TODO
 **/
@Service("DX011055_ZhaobGgService")
public class DX011055_ZhaobGgService extends SpiderService implements PageProcessor {

    public Spider spider = null;

    public String listUrl = "http://www.gzxqgroup.cn/comp/portalResNews/list.do?compId=portalResNews_list-16399655014628243&cid=10&pageSize=10&currentPage=1";

    public String baseUrl = "http://www.gzxqgroup.cn";
    // 网站编号
    public String sourceNum = "DX011055";
    // 网站名称
    public String sourceName = "贵州省兴泉实业（集团）有限公司 ";
    // 信息源
    public String infoSource = "企业采购";
    // 设置地区
    public String area = "西南";
    // 设置省份
    public String province = "贵州";
    // 设置城市
    public String city = "黔南布依族苗族自治州";
    // 设置县
    public String district = "";
    // 设置CreateBy
    public String createBy = "潘嘉明";

    public boolean isNeedSaveFileAddSSL = true;

    public Pattern p = Pattern.compile("(\\d{4})(\\.|年|/|-)(\\d{1,2})(\\.|月|/|-)(\\d{1,2})");
    public Pattern page_p = Pattern.compile("createPageHTML\\('pagination',(?<total>\\d+),");

    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(3).setTimeOut(30000).setSleepTime(20);

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
            if (url.contains("list")) {
                Document document = Jsoup.parse(html);
                if (serviceContext.getPageNum() == 1) {
                    Matcher m = Pattern.compile("totalPage:\"(?<total>\\d+)\",").matcher(html);
                    if (m.find()) {
                        String total = m.group("total");
                        int max = Integer.parseInt(total);
                        serviceContext.setMaxPage(max);
                    }
                }
                String keys = "招标、采购、询价、询比、竞标、竞价、竞谈、竞拍、竞卖、竞买、竞投、竞租、比选、比价、竞争性、谈判、磋商、投标、邀标、议标、议价、单一来源、遴选、标段、明标、明投、出让、转让、拍卖、招租、预审、发包、开标、答疑、补遗、澄清、挂牌";
                String[] rules = keys.split("、");
                Elements lis = document.select("div[class=p_news container]>div.newList:has(a)");
                List<BranchNew> detailList = new ArrayList<BranchNew>();
                if (lis.size() > 0) {
                    for (int i = 0; i < lis.size(); i++) {
                        Element li = lis.get(i);
                        Element a = li.select("a").first();
                        String title = a.select("h2.p_title").first().text().trim();
                        if (!CheckProclamationUtil.isProclamationValuable(title, rules)) {
                            continue;
                        }

                        String href = a.attr("href").trim();
                        if (href.startsWith("http") && !href.contains(baseUrl)) {
                            continue;
                        }
                        String id = href;
                        String link = baseUrl + id;
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
                if (serviceContext.getPageNum() < serviceContext.getMaxPage() && serviceContext.isNeedCrawl()) {
                    serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                    page.addTargetRequest(listUrl.replace("currentPage=1", "currentPage=" + serviceContext.getPageNum()));
                }
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
                Element dateE = document.select("li.date").first();
                if (dateE != null) {
                    String dateStr = dateE.text();
                    Matcher matcher = p.matcher(dateStr);
                    if (matcher.find()) {
                        date = matcher.group().replaceAll("[\\.|年|月|/]", "-");
                        date = SpecialUtil.date2Str(SpecialUtil.str2Date(date));
                    }
                }
                Element contentE = document.select("div[class=e_box p_articles]").first();
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
                                continue;
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

    public String getContent(String path) {
        String respStr = "";
        CloseableHttpClient client = getHttpClient(true, false);
        CloseableHttpResponse response = null;
        try {
            HttpGet httpGet = new HttpGet(path);
            httpGet.addHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(10 * 1000).setSocketTimeout(15 * 1000).build();
            httpGet.setConfig(requestConfig);
            response = client.execute(httpGet);
            response.addHeader("Connection", "close");
            if (response.getStatusLine().getStatusCode() == 200) {
                respStr = EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                if (client != null) {
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return respStr;
    }

}
