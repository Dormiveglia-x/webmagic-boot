package com.bidizhaobiao.data.Crawl.service.impl.DX010947;

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
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 程序员: 何康
 * 日期: 2023/2/14
 * 原网站: http://www.scgjgs.com/go.htm?k=gong_gao&url=wjgg_gg
 * 主页: http://www.scgjgs.com
 */
public class DX010947_ZhaobGgService extends SpiderService implements PageProcessor {
    public Spider spider = null;

    public String listUrl = "hhttp://www.scgjgs.com/go.htm?k=gong_gao&url=wjgg_gg";
    public String baseUrl = "http://www.scgjgs.com";
    public Pattern datePat = Pattern.compile("(\\d{4})(年|/|-|\\.)(\\d{1,2})(月|/|-|\\.)(\\d{1,2})");
    // 网站编号
    public String sourceNum = "DX010947";
    // 网站名称
    public String sourceName = "四川国检检测有限责任公司";
    // 信息源
    public String infoSource = "企业采购";
    // 设置地区
    public String area = "西南";
    // 设置省份
    public String province = "四川";
    // 设置城市
    public String city = "泸州";
    // 设置县
    public String district;
    public String createBy = "何康";
    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(2).setTimeOut(30000).setSleepTime(20);

    @Override
    public void startCrawl(int threadNum, int crawlType) {
        serviceContextEvaluation();
        // 保存日志
        serviceContext.setCrawlType(crawlType);
        saveCrawlLog(serviceContext);
        // 启动爬虫
        spider = Spider.create(this).thread(threadNum)
                .setDownloader(new MyDownloader(serviceContext, false, listUrl));
        spider.addRequest(new Request(listUrl));
        serviceContext.setSpider(spider);
        spider.run();
        // 爬虫状态监控部分
        saveCrawlResult(serviceContext);
    }

    @Override
    public void process(Page page) {
        String url = page.getUrl().toString();
        try {
            List<BranchNew> detailList = new ArrayList<BranchNew>();
            Thread.sleep(500);
            if (url.contains("wjgg_gg")) {
                Document listDoc = Jsoup.parse(page.getHtml().toString());
                Elements aList = listDoc.select("div.bslc_ul a");
                if (aList != null && aList.size() > 0) {
                    for (Element a : aList) {
                        String hrefVal = a.attr("href");
                        String detailLink = baseUrl + "/" + hrefVal;
                        Elements towSpan = a.select("div.li_text_bslc span");
                        String listTitle = towSpan.get(0).text();
                        String listDate = towSpan.get(1).text();
                        String id = hrefVal.substring(hrefVal.lastIndexOf("?" + 1));
                        String key = "招标、采购、询价、询比、竞标、竞价、竞谈、竞拍、竞卖、竞买、竞投、竞租、比选、比价、竞争性、谈判、磋商、投标、邀标、议标、议价、单一来源、遴选、标段、明标、明投、出让、转让、拍卖、招租、预审、发包、开标、答疑、补遗、澄清、挂牌";
                        String[] keyArray = key.split("、");
                        if (!CheckProclamationUtil.isProclamationValuable(listTitle,keyArray)) {
                            continue;
                        }
                        BranchNew branch = new BranchNew();
                        branch.setId(id);
                        serviceContext.setCurrentRecord(branch.getId());
                        branch.setDate(listDate);
                        branch.setDetailLink(detailLink);
                        branch.setTitle(listTitle);
                        branch.setLink(hrefVal);
                        detailList.add(branch);
                    }
                    List<BranchNew> branchNewList = checkData(detailList, serviceContext);
                    for (BranchNew branchNew : branchNewList) {
                        map.put(branchNew.getDetailLink(), branchNew);
                        page.addTargetRequest(branchNew.getDetailLink());
                    }
                } else {
                    logger.error("页面列表为空");
                }
            } else {
                BranchNew branch = map.get(url);
                if (branch != null) {
                    Document detailDoc = Jsoup.parse(page.getHtml().toString());
                    Element mainContent = detailDoc.select("div.ziliao").first();
                    String content = null;
                    String detailHtml = Jsoup.parse(page.getHtml().toString()).toString();
                    String pageDate = datePat.matcher(mainContent.select("div.dw_text_title").toString()).toString().replace("/", "-");
                    String pageTitle = mainContent.select("div.dw_js_title p").text();
                    if (mainContent != null) {
                        Elements aList = mainContent.select("a");
                        for (Element a : aList) {
                            String href = a.attr("href").replace(" ", "%20");
                            if (!href.startsWith("/") && !href.startsWith("http")) {
                                href = url.substring(0, url.lastIndexOf("/") + 1) + href;
                                a.attr("href", href);
                            }
                            a.attr("rel", "noreferrer");
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
                        Elements imgList = mainContent.select("IMG");
                        for (Element img : imgList) {
                            String src = img.attr("src").replace(" ", "%20");
                            if (src.startsWith("file://")) {
                                img.remove();
                                continue;
                            }
                            if (src.contains("data:image")) {
                                continue;
                            }
                            if (src.length() > 10 && src.indexOf("http") != 0) {
                                if (src.indexOf("../") == 0) {
                                    src = src.replace("../", "");
                                    src = baseUrl + "/" + src;
                                    img.attr("src", src);
                                } else if (src.indexOf("./") == 0) {
                                    src = url.substring(0, url.lastIndexOf("/")) + src.substring(src.lastIndexOf("./") + 1);
                                    img.attr("src", src);
                                } else if (src.startsWith("//")) {
                                    src = baseUrl.substring(0, baseUrl.indexOf(":") + 1) + src;
                                    img.attr("src", src);
                                } else if (src.indexOf("/") == 0) {
                                    src = baseUrl + src;
                                    img.attr("src", src);
                                } else {
                                    src = url.substring(0, url.lastIndexOf("/") + 1) + src;
                                    img.attr("src", src);
                                }
                            }
                        }
                        content = mainContent.toString();
                    } else if (url.contains(".doc") || url.contains(".pdf") || url.contains(".zip") || url.contains(".xls")) {
                        content = "<div>附件下载：<a href='" + url + "'>" + branch.getTitle() + "</a></div>";
                        detailHtml = Jsoup.parse(content).toString();
                    }
                    RecordVO recordVO = new RecordVO();
                    recordVO.setContent(content);
                    recordVO.setDetailHtml(detailHtml);
                    recordVO.setListTitle(branch.getTitle());
                    recordVO.setTitle(pageTitle);
                    recordVO.setDate(pageDate);
                    recordVO.setDetailLink(url);
                    recordVO.setDdid(SpecialUtil.stringMd5(detailHtml));
                    recordVO.setId(branch.getId());
                    dataStorage(serviceContext,recordVO,branch.getType());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public Site getSite() {
        return site;
    }
}
