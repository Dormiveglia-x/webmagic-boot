package com.bidizhaobiao.data.Crawl.service.impl.DX010949;

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
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 创建时间: 2023/2/27
 * 原网站: http://www.zjbopp.com/Tendering/
 * 主页: http://www.zjbopp.com
 */
@Service
public class DX010949_ZhaobGgService extends SpiderService implements PageProcessor {


    public Spider spider = null;

    public String listUrl = "http://www.zjbopp.com/Tendering";
    public String baseUrl = "http://www.zjbopp.com";
    // 网站编号
    public String sourceNum = "DX010949";
    // 网站名称
    public String sourceName = "湛江包装材料企业有限公司";
    // 信息源
    public String infoSource = "企业采购";
    // 设置地区
    public String area = "华南";
    // 设置省份
    public String province = "广东";
    // 设置城市
    public String city = "湛江";
    // 设置县
    public String district = "";
    // 设置CreateBy
    public String createBy = "何康";

    public boolean isNeedSaveFileAddSSL = true;

    public Pattern datePat = Pattern.compile("(\\d{4})(\\.|年|/|-)(\\d{1,2})(\\.|月|/|-)(\\d{1,2})");

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
            if (url.contains("Tendering")) {
                Document listDoc = Jsoup.parse(html);
                Elements trList = listDoc.select("div.ArticleList tr");
                trList.remove(trList.last());
                if (trList.size() > 0) {
                    List<BranchNew> branchNewList = new ArrayList<>();
                    for (Element tr : trList) {
                        Element a = tr.select("a").first();
                        String listDate = tr.select("td.fw_s").first().text().replace("[", "").replace("]", "");
                        String listTitle = a.text();
                        String hrefVal = a.attr("href");
                        String id = hrefVal.substring(1);
                        String detailLink = baseUrl + hrefVal;
                        if (!CheckProclamationUtil.isProclamationValuable(listTitle)) {
                            continue;
                        }
                        BranchNew branchNew = new BranchNew();
                        branchNew.setLink(hrefVal);
                        branchNew.setId(id);
                        serviceContext.setCurrentRecord(id);
                        String[] split = listDate.split("-");
                        for (int i =0;i<split.length;i++){
                            if(split[i].length() == 1){
                                split[i] = "0" + i;
                            }
                        }
                        listDate = split[0] + "-" + split[1] + "-" + split[2];
                        branchNew.setDate(listDate);
                        branchNew.setTitle(listTitle);
                        branchNew.setDetailLink(detailLink);
                        branchNewList.add(branchNew);
                    }
                    List<BranchNew> needCrawlList = checkData(branchNewList, serviceContext);
                    for (BranchNew branchNew : needCrawlList) {
                        map.put(branchNew.getDetailLink(), branchNew);
                        page.addTargetRequest(branchNew.getDetailLink());
                    }
                } else {
                    dealWithNullListPage(serviceContext);
                }

            } else {
                BranchNew branch = map.get(url);
                if (branch != null) {
                    Document detailDoc = Jsoup.parse(html);
                    Element mainContent = detailDoc.select("div.content").first();
                    String content = null;
                    String detailHtml = Jsoup.parse(html).toString();
                    String pageTitle = mainContent.select("div.title h3").first().text();
                    if (mainContent != null) {
                        mainContent.select("iframe").remove();
                        mainContent.select("input").remove();
                        mainContent.select("button").remove();
                        mainContent.select("script").remove();
                        if (mainContent.select("a").first() != null) {
                            Elements as = mainContent.select("a");
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
                        if (mainContent.select("img").first() != null) {
                            Elements imgs = mainContent.select("img");
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
                        content = mainContent.toString();
                    }
                    if (url.contains(".doc") || url.contains(".pdf") || url.contains(".zip") || url.contains(".xls")) {
                        content = "<div>附件下载：<a href='" + url + "'>" + pageTitle + "</a></div>";
                        html = Jsoup.parse(content).toString();
                    }

                    RecordVO recordVO = new RecordVO();
                    recordVO.setDdid(SpecialUtil.stringMd5(html));
                    recordVO.setId(branch.getId());

                    recordVO.setListTitle(branch.getTitle());
                    recordVO.setDate(branch.getDate());

                    recordVO.setDetailLink(url);
                    recordVO.setDetailHtml(detailHtml);
                    recordVO.setTitle(pageTitle);
                    recordVO.setContent(content);
                    logger.info("准备入库的id为{}" + branch.getId());
                    dataStorage(serviceContext,recordVO,branch.getType());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
