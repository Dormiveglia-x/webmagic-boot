package com.bidizhaobiao.data.Crawl.service.impl.DX010946;

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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * demo 2023/2/24
 * 原网站: http://www.ycepstc.com/index.php?c=category&id=20
 * 主页: http://www.ycepstc.com
 */
@Service
public class DX010946_ZhongbXxService extends SpiderService implements PageProcessor {

    public Spider spider = null;

    public String listUrl = "http://www.ycepstc.com/index.php?c=category&id=20";
    public String baseUrl = "http://www.ycepstc.com";
    public Pattern datePat = Pattern.compile("(\\d{4})(年|/|-|\\.)(\\d{1,2})(月|/|-|\\.)(\\d{1,2})");

    // 网站编号
    public String sourceNum = "DX010946";
    // 网站名称
    public String sourceName = "江苏盐城环保科技城";
    // 信息源
    public String infoSource = "企业采购";
    // 设置地区
    public String area = "华东";
    // 设置省份
    public String province = "江苏";
    // 设置城市
    public String city = "盐城";
    // 设置县
    public String district;
    public String createBy = "何康";
    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(2).setTimeOut(30000).setSleepTime(20);

    @Override
    public Site getSite() {
        return site;
    }

    @Override
    public void startCrawl(int threadNum, int crawlType) {
        // 赋值
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
            if (url.contains("category")) {
                Document document = Jsoup.parse(page.getHtml().toString());
                Elements liList = document.select("ul.nlist-pic li");
                if (liList.size() > 0) {
                    for (Element li : liList) {
                        Element a = li.select("div.txtbox a").first();
                        String hrefVal = a.attr("href");
                        String id = hrefVal.substring(hrefVal.lastIndexOf("/") + 1);
                        String detailLink = baseUrl + hrefVal;
                        String listTitle = a.text();
                        String month = li.select("div.date em").text().replace("/", "-");
                        String listDate = li.select("div.date span").text() + "-" + month;

                        if (!CheckProclamationUtil.isProclamationValuable(listTitle)) {
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
                    dealWithNullListPage(serviceContext);
                }

                if (serviceContext.getPageNum() == 1) {
                    Element a = document.select("div.pagelist li").last().select("a").first();
                    String hrefVal = a.attr("href");
                    String maxPage = hrefVal.substring(hrefVal.length() - 2);
                    serviceContext.setMaxPage(Integer.valueOf(maxPage));
                }

                if(serviceContext.getPageNum() <= serviceContext.getMaxPage()){
                    serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                    String nextPage = listUrl + "&page=" + serviceContext.getPageNum();
                    page.addTargetRequest(nextPage);
                }

            } else {
                BranchNew branch = map.get(url);
                if (branch != null) {
                    serviceContext.setCurrentRecord(branch.getId());
                    Document detailDoc = Jsoup.parse(page.getHtml().toString());
                    Element mainContent = detailDoc.select("div.mcontent").first();
                    String content = null;
                    String detailTitle = null;
                    RecordVO recordVo = new RecordVO();
                    if (mainContent != null) {
                        detailTitle = mainContent.select("h1.tith1").text();
                        Elements aList = mainContent.select("a");
                        for (Element a : aList) {
                            String href = a.attr("href");
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
                            String src = img.attr("src");
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
                        mainContent.select("script").remove();
                        mainContent.select("style").remove();
                        content = "<div>" + detailTitle + "</div>" + mainContent.outerHtml();
                        recordVo.setContent(content);
                    } else if (url.contains(".doc") || url.contains(".pdf") || url.contains(".zip") || url.contains(".xls")) {
                        content = "<div>附件下载：<a href='" + url + "'>" + branch.getTitle() + "</a></div>";
                        recordVo.setDetailHtml(Jsoup.parse(content).toString());
                        recordVo.setContent(content);
                    }
                    recordVo.setArea(area);
                    recordVo.setDate(branch.getDate());
                    recordVo.setDetailHtml(detailDoc.toString());
                    recordVo.setCity(city);
                    recordVo.setId(branch.getId());
                    recordVo.setListTitle(branch.getTitle());
                    recordVo.setTitle(detailTitle);
                    recordVo.setDetailHtml(detailDoc.toString());
                    recordVo.setDdid(SpecialUtil.stringMd5(detailDoc.toString()));
                    recordVo.setDetailLink(branch.getDetailLink());
                    logger.info("入库id----->{}" + branch.getId());
                    dataStorage(serviceContext, recordVo, branch.getType());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
