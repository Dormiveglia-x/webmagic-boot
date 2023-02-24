package com.bidizhaobiao.data.Crawl.service.impl.QX_01516;

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
import us.codecraft.webmagic.selector.Html;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 程序员：郭建婷 日期：2022-02-15

 * 原网站：http://www.xmta.gov.cn/zc/tzgg/
 * 主页：http://www.xmta.gov.cn
 **/
@Service
public class QX_01516_ZhaobGgService extends SpiderService implements PageProcessor {
    public Spider spider = null;

    public String listUrl = "http://www.xmta.gov.cn/zc/tzgg/index.htm";
    public String baseUrl = "http://www.xmta.gov.cn";
    public Pattern p_time = Pattern.compile("(\\d+)[\\./年-](\\d+)[\\./月-](\\d{1,2})");

    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    public Site site = Site.me().setCycleRetryTimes(2).setTimeOut(30000).setSleepTime(20).setCharset("utf-8");
    // 网站编号
    public String sourceNum = "01516";
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
    // 设置县
    public String createBy = "郭建婷";
    //站源类型
    public String taskType;
    //站源名称
    public String taskName;
    //是否需要入广联达
    public boolean isNeedInsertGonggxinxi = false;

    public Site getSite() {
        return this.site.setUserAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36");
    }

    public void startCrawl(int ThreadNum, int crawlType) {
        // 赋值
        serviceContextEvaluation();
        // 保存日志
        saveCrawlLog(serviceContext);
        serviceContext.setCrawlType(crawlType);
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
        Document doc = page.getHtml().getDocument();
        try {
            List<BranchNew> detailList = new ArrayList<>();
            Thread.sleep(500);
            // 判断是否是翻页连接
            if (url.contains("/index")) {
                Elements lists = doc.select(".gl_r_ul li");
                if (lists.size() > 0) {
                    for (Element list : lists) {
                        String title = list.select("a").attr("title");
                        String str = "抽取、更正、招标、采购、询价、询比、竞标、竞价、竞谈、竞拍、竞卖、竞买、竞投、竞租、比选、比价、竞争性、谈判、磋商、投标、邀标、议标、议价、单一来源、遴选、标段、明标、明投、出让、转让、拍卖、招租、预审、发包、开标、答疑、补遗、澄清、挂牌";
                        String[] split = str.split("、");
                        if (!CheckProclamationUtil.isProclamationValuable(title, split)) {
                            continue;
                        }
                        String id = "/zc/tzgg/" + list.select("a").attr("href").replaceAll("\\./", "");
                        String link = baseUrl + id;
                        String date = list.select("span").text();
                        Matcher m = p_time.matcher(date);
                        if (m.find()) {
                            date = m.group(1) + "-" + m.group(2) + "-" + m.group(3);
                        }
                        BranchNew branch = new BranchNew();
                        branch.setId(id);
                        branch.setTitle(title);
                        branch.setLink(link);
                        branch.setDate(date);
                        detailList.add(branch);
                    }
                    // 校验数据,判断是否需要继续触发爬虫
                    List<BranchNew> needCrawlList = checkData(detailList, serviceContext);
                    for (BranchNew branch : needCrawlList) {
                        map.put(branch.getLink(), branch);
                        page.addTargetRequest(branch.getLink());
                    }
                    if (serviceContext.getMaxPage() == 1) {
                        String maxPage = doc.toString();
                        Pattern c = Pattern.compile("pageCount:'(\\d+)'");
                        Matcher m = c.matcher(maxPage);
                        if (m.find()) {
                            int total = Integer.parseInt(m.group(1));
                            serviceContext.setMaxPage(total);
                        }
                    }
                    if (serviceContext.getPageNum() < serviceContext.getMaxPage() && serviceContext.isNeedCrawl()) {
                        serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                        String nextPage = listUrl.replace("/index", "/index_" + (serviceContext.getPageNum() - 1));
                        page.addTargetRequest(nextPage);
                    }
                } else {
                    dealWithNullListPage(serviceContext);
                }
            } else {
                Html html = page.getHtml();
                BranchNew branch = map.get(url);
                if (branch != null) {
                    String content = "";
                    String detailTitle = doc.select(".xl_top h1").text();
                    if ("".equals(detailTitle) || detailTitle == null) {
                        detailTitle = branch.getTitle();
                    }
                    Element contentE = doc.select(".xl_tit").first();
                    if (contentE != null) {
                        setUrl(contentE, baseUrl, url);
                        content = detailTitle + contentE.outerHtml();
                    } else {
                        if (url.contains(".pdf") || url.contains(".doc") || url.contains(".zip")) {
                            content = "<a href='" + branch.getLink() + "'>" + detailTitle + "</a>";
                        }
                    }
                    RecordVO recordVO = new RecordVO();
                    recordVO.setId(branch.getId());
                    recordVO.setTitle(detailTitle);
                    recordVO.setListTitle(branch.getTitle());
                    recordVO.setDetailLink(branch.getLink());
                    recordVO.setDate(branch.getDate());
                    recordVO.setContent(content);
                    recordVO.setDetailHtml(html != null ? html.toString() : Jsoup.parse(content).html());
                    recordVO.setDdid(SpecialUtil.stringMd5(html != null ? html.toString() : Jsoup.parse(content).html()));
                    dataStorage(serviceContext, recordVO, branch.getType());
                }
            }
        } catch (Exception e) {
            dealWithError(url, serviceContext, e);
        }
    }

    public void setUrl(Element conE, String baseUrl, String url) {

        Element contentE = conE;
        contentE.removeAttr("style");
        contentE.select("iframe").remove();
        contentE.select("meta").remove();
        contentE.select("link").remove();
        contentE.select("button").remove();
        contentE.select("input").remove();
        contentE.select("*[style~=^.*display\\s*:\\s*none\\s*(;\\s*[0-9A-Za-z]+|;\\s*)?$]").remove();
        contentE.select("script").remove();
        contentE.select("style").remove();

        Elements as = contentE.select("a");
        for (Element a : as) {
            String href = a.attr("href");
            if ("".equals(href) || href == null
                    || href.indexOf("#") == 0
                    || href.contains("javascript:")) {
                a.removeAttr("href");
                continue;
            }
            if (href.indexOf("http") != 0) {
                if (href.indexOf("../") == 0) {
                    href = baseUrl + "/" + href.replace("../", "");
                } else if (href.indexOf("./") == 0) {
                    href = url.substring(0, url.lastIndexOf("/")) + href.replace("./", "/");
                } else if (href.indexOf("/") == 0) {
                    href = baseUrl + href;
                } else {
                    if (!"".equals(href)) {
                        href = baseUrl + "/" + href;
                    }
                }
                a.attr("href", href);
            }
        }

        Elements imgs = contentE.select("img");
        for (Element img : imgs) {
            String src = img.attr("src");
            if (!src.contains("javascript") && !"".equals(src) && !src.contains("http")) {
                if (src.indexOf("../") == 0) {
                    src = baseUrl + "/" + src.replace("../", "");
                } else if (src.indexOf("./") == 0) {
                    src = url.substring(0, url.lastIndexOf("/")) + src.replace("./", "/");
                } else if (src.indexOf("/") == 0) {
                    src = baseUrl + src;
                } else {
                    if (!"".equals(src)) {
                        src = baseUrl + "/" + src;
                    }
                }
                img.attr("src", src);
            }
        }
    }
}
