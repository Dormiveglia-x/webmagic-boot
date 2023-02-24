package com.bidizhaobiao.data.Crawl.service.impl.DX002429;

import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
import com.bidizhaobiao.data.Crawl.utils.CheckProclamationUtil;
import com.bidizhaobiao.data.Crawl.utils.SpecialUtil;
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
import us.codecraft.webmagic.selector.Selectable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 程序员：许广衡
 * 日期：2022-03-11
 * 原网站：http://www.zsebank.com/zxgg/index.html
 * 主页：http://www.zsebank.com/
 **/
@Service
public class DX002429_ZhaobGgService extends SpiderService implements PageProcessor {
    public Spider spider = null;
    public String listUrl = "http://www.zsebank.com/zxgg/index.html";
    public String baseUrl = "http://www.zsebank.com";
    public String[] keys = "项目、招标、采购、询价、询比、竞标、竞价、竞谈、竞拍、竞卖、竞买、竞投、竞租、比选、比价、竞争性、谈判、磋商、投标、邀标、议标、议价、单一来源、遴选、标段、明标、明投、出让、转让、拍卖、招租、预审、发包、开标、答疑、补遗、澄清、挂牌".split("、");
    // 网站编号
    public String sourceNum = "DX002429";
    // 网站名称
    public String sourceName = "中山农村商业银行股份有限公司";
    // 设置地区
    public String area = "华南";
    // 设置省份
    public String province = "广东";
    // 设置城市
    public String city = "中山市";
    // 设置县
    public String district;
    // 设置县
    public String createBy = "许广衡";
    // 信息源
    public String infoSource = "企业采购";
    //站源类型
    public String taskType;
    //站源名称
    public String taskName;
    public Pattern p = Pattern.compile("20\\d{2}-\\d{1,2}-\\d{1,2}");
    public Pattern pd = Pattern.compile("(\\d{4})(年|/|-|\\.)(\\d{1,2})(月|/|-|\\.)(\\d{1,2})");
    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数
    Site site = Site.me().setCycleRetryTimes(2).setTimeOut(30000).setSleepTime(20);

    public Site getSite() {
        return this.site.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36");
    }

    public void startCrawl(int ThreadNum, int crawlType) {
        serviceContextEvaluation();
        saveCrawlLog(serviceContext);
        serviceContext.setCrawlType(crawlType);
        spider = Spider.create(this).thread(ThreadNum).setDownloader(new MyDownloader(serviceContext, true, listUrl));
        spider.addRequest(new Request(listUrl));
        serviceContext.setSpider(spider);
        spider.run();
        saveCrawlResult(serviceContext);
    }

    public void process(Page page) {
        String url = page.getUrl().toString();
        try {
            Thread.sleep(1000);
            Html html = page.getHtml();
            if (url.contains("index")) {
                List<BranchNew> branchNewList = new ArrayList<BranchNew>();
                List<Selectable> eleList = html.xpath("//div[@class='news_list']/ul/dl").nodes();
                for (Selectable node : eleList) {
                    String title = node.xpath("//h5/a/text()").get();
                    if (title == null || !CheckProclamationUtil.isProclamationValuable(title, keys)) {
                        continue;
                    }
                    String link = node.xpath("//h5/a").links().get();
                    String id = link.substring(link.lastIndexOf("/") + 1);
                    BranchNew branchNew = new BranchNew();
                    branchNew.setId(id);
                    serviceContext.setCurrentRecord(branchNew.getId());
                    branchNew.setTitle(title);
                    // branchNew.setDate(date);
                    branchNew.setLink(link);
                    branchNewList.add(branchNew);
                }
                List<Selectable> nodes = html.xpath("//div[@class='news_list']/ul/li").nodes();
                if (nodes.size() > 0) {
                    for (Selectable node : nodes) {
                        String title = node.xpath("//a/text()").get();
                        if (title == null || !CheckProclamationUtil.isProclamationValuable(title, keys)) {
                            if(!title.contains("招标公告")) {
                                continue;
                            }
                        }
                        String link = node.xpath("//a").links().get();
                        String dateStr = node.xpath("//span/text()").all().toString();
                        String date = "";
                        Matcher m = p.matcher(dateStr.replaceAll("[./年月]", "-"));
                        if (m.find()) {
                            date = sdf.get().format(sdf.get().parse(m.group()));
                        }
                        String id = link.substring(link.lastIndexOf("/") + 1);
                        BranchNew branchNew = new BranchNew();
                        branchNew.setId(id);
                        serviceContext.setCurrentRecord(branchNew.getId());
                        branchNew.setTitle(title);
                        branchNew.setDate(date);
                        branchNew.setLink(link);
                        branchNewList.add(branchNew);
                    }
                    // 检查数据
                    List<BranchNew> needCrawlList = checkData(branchNewList, serviceContext);
                    for (BranchNew branch : needCrawlList) {
                        map.put(branch.getLink(), branch);
                        page.addTargetRequest(branch.getLink());
                    }
                    if (serviceContext.isNeedCrawl()) {
                        Element nextUrlEle = html.getDocument().select("a:contains(下一页)").first();
                        if (nextUrlEle != null) {
                            String nextUrl = nextUrlEle.attr("abs:href");
                            if (!nextUrl.contains("javascript")) {
                                serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                                page.addTargetRequest(nextUrl);
                            }
                        }
                    }
                } else {
                    dealWithNullListPage(serviceContext);
                }
            } else {
                BranchNew branchNew = map.get(url);
                map.remove(url);
                String detailHtml = page.getHtml().toString();
                if (branchNew == null) {
                    return;
                }
                serviceContext.setCurrentRecord(branchNew.getId());
                Document doc = html.getDocument();
                String title = branchNew.getTitle().replace("..", "");
                Element titleEle = doc.select("h6").first();
                if (titleEle != null) {
                    title = titleEle.text();
                }
                if (branchNew.getDate() == null) {
                    Matcher m = pd.matcher(doc.select("article[class=news-info]").text());
                    String date = "";
                    if (m.find()) {
                        String month = m.group(3).length() == 2 ? m.group(3) : "0" + m.group(3);
                        String day = m.group(5).length() == 2 ? m.group(5) : "0" + m.group(5);
                        date = m.group(1) + "-" + month + "-" + day;
                        // 第一页有几条2015数据，有时新数据入不来
                        branchNew.setDate(date);
                    }
                }
                Element contentDiv = doc.select("div.info").first();

                if (contentDiv == null) {
                    return;
                }
                contentDiv.select("script").remove();
                contentDiv.select("style").remove();
                Elements iframes = contentDiv.select("iframe");
                for (Element iframe : iframes) {
                    String src = iframe.attr("src");
                    iframe.removeAttr("src");
                    iframe.attr("href", src);
                    iframe.text("附件查看");
                    iframe.tagName("a");
                }
                Elements aList = contentDiv.select("a");
                for (Element a : aList) {
                    String href = a.attr("abs:href");
                    if (href.length() < 1 || href.contains("javascript") || href.endsWith("#")) {
                        a.remove();
                        continue;
                    }
                    a.attr("href", href);
                }
                Elements imgList = contentDiv.select("img");
                boolean hasDataImg = false;
                for (Element img : imgList) {
                    String src = img.attr("abs:src");
                    if (src.contains("data:image")) {
                        hasDataImg = true;
                        img.remove();
                        continue;
                    }
                    img.attr("src", src);
                }
                String content = title + contentDiv.outerHtml().replaceAll("\\u2002", " ");
                if (hasDataImg) { // 包含base64图片
                    content += "<br><div>更多咨询报价请点击：<a href='" + url + "'>" + url + "</a></div>";
                }
                RecordVO recordVO = new RecordVO();
                recordVO.setId(branchNew.getId());
                recordVO.setListTitle(branchNew.getTitle());
                recordVO.setTitle(title);
                recordVO.setDetailLink(url);
                recordVO.setDetailHtml(detailHtml);
                recordVO.setDdid(SpecialUtil.stringMd5(detailHtml));
                recordVO.setDate(branchNew.getDate());
                recordVO.setContent(content);
                dataStorage(serviceContext, recordVO, branchNew.getType());
            }
        } catch (Exception e) {
            dealWithError(url, serviceContext, e);
        }
    }
}
