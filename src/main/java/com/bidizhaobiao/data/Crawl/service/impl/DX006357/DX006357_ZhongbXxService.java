package com.bidizhaobiao.data.Crawl.service.impl.DX006357;

import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
import com.bidizhaobiao.data.Crawl.utils.CheckProclamationUtil;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 程序员： 赖晓晖  日期：2020-08-06
 * 原网站：https://www.changtougaoke.com/notice/index.html
 * 主页：https://www.changtougaoke.com
 **/

@Service("DX006357_ZhongbXxService")
public class DX006357_ZhongbXxService extends SpiderService implements PageProcessor {

    public Spider spider = null;

    public String listUrl = "https://www.changtougaoke.com/notice/index.html";

    public String baseUrl = "https://www.changtougaoke.com";

    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(3).setTimeOut(30000).setSleepTime(20);
    // 网站编号
    public String sourceNum = "DX006357";
    // 网站名称
    public String sourceName = "湖北长投高科产业投资集团有限公司";
    // 信息源
    public String infoSource = "企业采购";
    // 设置地区
    public String area = "华中";
    // 设置省份
    public String province = "湖北";
    // 设置城市
    public String city ;
    // 设置县
    public String district ;
    // 设置CreateBy
    public String createBy = "赖晓晖";

    public Pattern p = Pattern.compile("(\\d{4})(年|/|-)(\\d{1,2})(月|/|-)(\\d{1,2})");

    public Pattern p_p = Pattern.compile("view\\('(.*?)','(.*?)','(.*?)'\\)");

    public Site getSite() {
       return this.site.setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
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
        spider.addRequest(new Request("https://www.baidu.com/?wd=1"));
        serviceContext.setSpider(spider);
        spider.run();
        // 爬虫状态监控部分
        saveCrawlResult(serviceContext);
    }

    public void process(Page page) {
        String url = page.getUrl().toString();
        try {
            List<BranchNew> detailList = new ArrayList<>();
            Thread.sleep(1000);
            // 判断是否是翻页连接
            if (url.contains("https://www.baidu.com/")) {
                listUrl = "https://www.changtougaoke.com/notice/index.html";
                if (serviceContext.getPageNum() != 1) {
                    listUrl = listUrl.replace("index", "index_" + serviceContext.getPageNum());
                }
                String pageCout = getContent(listUrl);
                int num = 3;
                while (!pageCout.contains("list-notice") && num > 0) {
                    pageCout = getContent(listUrl);
                    num--;
                }
                Document document = Jsoup.parse(pageCout);
                if (serviceContext.getPageNum() == 1) {
                    Element pageElement = document.select(".epages").last().select("a").last();
                    String pages = pageElement.attr("href").replaceAll("[\u00a0\u1680\u180e\u2000-\u200a\u2028\u2029\u202f\u205f\u3000\ufeff\\s+]", "");
                    pages = pages.substring(pages.lastIndexOf("_") + 1, pages.lastIndexOf("."));
                    int maxPage = Integer.parseInt(pages);
                    serviceContext.setMaxPage(maxPage);
                }
                Elements lis = document.select(".list-notice").first().select("li:has(a)");
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
                        title = title.replace("...", "");
                        if (!CheckProclamationUtil.isProclamationValuable(title)) {
                            continue;
                        }
                        String href = a.attr("href").trim();
                        String link = "";
                        if (href.contains("http")) {
                            link = href;
                            href = href.substring(href.indexOf("?") + 1, href.length());
                        } else {
                            link = baseUrl + href;
                        }
                        String id = href;
                        String detailLink = link;
                        String date = li.text().trim().replaceAll("[.|/|年|月]", "-");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Matcher m = p.matcher(date);
                        if (m.find()) {
                            date = sdf.format(sdf.parse(m.group()));
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
                        String Content = getContent(branchNew.getLink());
                        int nums = 3;
                        while (!Content.contains("detail-title") && nums > 0) {
                            Content = getContent(branchNew.getLink());
                            nums--;
                        }
                        String title = Jsoup.parse(branchNew.getTitle()).text();
                        String id = branchNew.getId();
                        String date = branchNew.getDate();
                        String detailLink = branchNew.getDetailLink();
                        String detailTitle = title;
                        String content = "";
                        String detailContent = Content;
                        Document docc = Jsoup.parse(Content);
                        if (docc.select(".detail-title").first() == null) {
                            detailTitle = title.trim().replace(" ", "").replace("...", "");
                        } else {
                            detailTitle = docc.select(".detail-title").first().text().trim().replace(" ", "").replace("...", "");
                            title = detailTitle;
                        }
                        Element contentE = docc.select(".detail-content").first();
                        contentE.select("*[style~=^.*display\\s*:\\s*none\\s*(;\\s*[0-9A-Za-z]+|;\\s*)?$]").remove();
                        contentE.select("script").remove();
                        contentE.select("style").remove();
                        contentE.select("iframe").remove();
                        if (contentE.select("a") != null) {
                            Elements as = contentE.select("a");
                            for (Element a : as) {
                                String href = a.attr("href");
                                if (!href.contains("@") && !"".equals(href) && !href.contains("javascript") && !href.contains("http") && !href.contains("#")) {
                                    if (href.contains("./")) {
                                        href = baseUrl + "/" + href.substring(href.lastIndexOf("./") + 1, href.length());
                                        a.attr("href", href);
                                    } else if (href.startsWith("/")) {
                                        href = baseUrl + href;
                                        a.attr("href", href);
                                    } else {
                                        href = baseUrl + "/" + href;
                                        a.attr("href", href);
                                    }
                                } else if (href.contains("#") && a.attr("onclick").length() > 3) {
                                    String ur = a.attr("onclick");
                                    ur = ur.substring(ur.lastIndexOf("http"), ur.lastIndexOf("'"));
                                    a.removeAttr("onclick");
                                    a.attr("href", ur);
                                }
                            }
                        }
                        if (contentE.select("img").first() != null) {
                            Elements imgs = contentE.select("img");
                            for (Element img : imgs) {
                                String src = img.attr("src");
                                if (!src.contains("javascript") && !"".equals(src) && !src.contains("http")) {
                                    if (src.contains("./")) {
                                        src = baseUrl + "/" + src.substring(src.lastIndexOf("./") + 1, src.length());
                                        img.attr("src", src);
                                    } else if (src.startsWith("/")) {
                                        src = baseUrl + src;
                                        img.attr("src", src);
                                    } else {
                                        src = baseUrl + "/" + src;
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
                        recordVO.setContent(content);
                        recordVO.setTitle(detailTitle);//详情页标题
                        //recordVO.setDdid(SpecialUtil.stringMd5(detailContent));//详情页md5
                        recordVO.setDetailLink(detailLink);//详情页链接
                        recordVO.setDetailHtml(detailContent);
                        dataStorage(serviceContext, recordVO, branchNew.getType());
                    }
                    if (serviceContext.getPageNum() < serviceContext.getMaxPage() && serviceContext.isNeedCrawl()) {
                        serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                        String nextPage = "https://www.baidu.com/?wd=" + serviceContext.getPageNum();
                        page.addTargetRequest(nextPage);
                    }
                } else {
                    dealWithNullListPage(serviceContext);
                }
            }
        } catch (Exception e) {
            dealWithError(url, serviceContext, e);
        }
    }

    public String getContent(String path) {
        String respStr = "";
        CloseableHttpClient client = null;
        CloseableHttpResponse res = null;
        try {
            client = getHttpClient(true, true);
            HttpGet httpGet = new HttpGet(path);
            httpGet.addHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
            httpGet.addHeader("Accept-Language", "zh-CN,zh;q=0.9");
            httpGet.addHeader("Connection", "keep-alive");
            httpGet.addHeader("Cache-Control", "max-age=0");
            httpGet.addHeader("Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30 * 1000)
                    .setSocketTimeout(30 * 1000).setRedirectsEnabled(false).build();
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return respStr;
    }

}
