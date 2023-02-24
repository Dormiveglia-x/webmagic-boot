package com.bidizhaobiao.data.Crawl.service.impl.DX007528;

import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
import com.bidizhaobiao.data.Crawl.utils.CheckProclamationUtil;
import com.bidizhaobiao.data.Crawl.utils.SpecialUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.model.HttpRequestBody;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.utils.HttpConstant;

import java.util.*;

/**
 * 程序员：郭建婷 日期：2022-02-15
 * 原网站：https://bidding.e-cbest.com:8000/index.do#page=1
 * 主页：https://bidding.e-cbest.com:8000/
 */
@Service
public class DX007528_1_ZhaobGgService extends SpiderService implements PageProcessor {
    public Spider spider = null;
    public String listUrl = "https://bidding.e-cbest.com:8000/LoginAction!noticeList.do";
    public String baseUrl = "https://bidding.e-cbest.com:8000";
    // 网站编号
    public String sourceNum = "DX007528-1";
    // 网站名称
    public String sourceName = "重庆百货招采平台";
    // 信息源
    public String infoSource = "企业采购";
    // 设置地区
    public String area = "西南";
    // 设置省份
    public String province = "重庆";
    //创建人
    public String createBy = "郭建婷";
    // 是否需要入广联达
    public boolean isNeedInsertGonggxinxi = false;
    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(2).setTimeOut(30000).setSleepTime(20);

    public static Request getListRequest(int page) {
        String url = "https://bidding.e-cbest.com:8000/LoginAction!noticeList.do";
        Request request = new Request(url);
        request.setMethod(HttpConstant.Method.POST);
        //request.setRequestBody(HttpRequestBody.xml(params, "UTF-8"))
        Map parmMap = new HashMap();
        String parmJson = "{\"page\":" + page + ",\"size\":10,\"type\":\"bidNotice\"}";
        parmMap.put("paramJson", parmJson);

        request.setRequestBody(HttpRequestBody.form(parmMap, "UTF-8"));

        return request;
    }

    public Site getSite() {
        return this.site;
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
        spider.addRequest(getListRequest(1));
        serviceContext.setSpider(spider);
        spider.run();
        // 爬虫状态监控部分
        saveCrawlResult(serviceContext);
    }

    public void process(Page page) {
        String url = page.getUrl().toString();
        String detailHtml = page.getRawText();
        try {
            List<BranchNew> detailList = new ArrayList<BranchNew>();
            Thread.sleep(500);
            if (url.contains("List")) {
                JSONObject dataJsonObject = new JSONObject(detailHtml);
                if (dataJsonObject.length() > 0) {
                    if (dataJsonObject.has("total") && serviceContext.getPageNum() == 1) {
                        int totalPage = dataJsonObject.getInt("total");//total pages
                        int maxPage = totalPage / 10 == 0 ? totalPage / 10 : totalPage / 10 + 1;
                        serviceContext.setMaxPage(maxPage);
                    }
                    JSONArray dataJsonArray = dataJsonObject.getJSONArray("dataList");
                    for (int i = 0; i < dataJsonArray.length(); i++) {
                        JSONObject datailContent = dataJsonArray.getJSONObject(i);
                        int codeMode = datailContent.getInt("codeMode");//详情title
                        String title = datailContent.getString("title");//详情title
                        Long sendTime = datailContent.getLong("sendTime");//详情时间
                        String id = datailContent.getString("docId");//id
                        String link = "";
                        if (codeMode == 10) {
                            link = baseUrl + "/LoginAction!bidNoticInfo.do?announceCode=" + id;//详情链接
                        } else {
                            link = baseUrl + "/LoginAction!jumpSuppFeed.do?inquCode=" + id;//详情链接
                        }
                        String date = SpecialUtil.date2Str(new Date(sendTime));
                        if (!CheckProclamationUtil.isProclamationValuable(title, null)) {//招标
                            continue;
                        }
                        BranchNew branch = new BranchNew();
                        branch.setId(id);
                        branch.setLink(link);
                        branch.setDetailLink(link);
                        branch.setTitle(title);
                        branch.setDate(date);
                        detailList.add(branch);
                    }
                    List<BranchNew> branchNewList = checkData(detailList, serviceContext);
                    for (BranchNew branch : branchNewList) {
                        map.put(branch.getLink(), branch);
                        page.addTargetRequest(branch.getLink());
                    }
                } else {
                    dealWithNullListPage(serviceContext);
                }
                if (serviceContext.getPageNum() < serviceContext.getMaxPage() && serviceContext.isNeedCrawl()) {
                    //翻页规则
                    serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                    page.addTargetRequest(getListRequest(serviceContext.getPageNum()));
                }
            } else {
                BranchNew branch = map.get(url);
                if (branch != null) {
                    map.remove(url);
                    String title = branch.getTitle().replace("...", "");
                    String date = branch.getDate();
                    String content = "";
                    Document doc = Jsoup.parse(detailHtml);
                    Element contentElement = doc.select("div.content-detail").first();//详情页content
                    if (contentElement != null) {
                        contentElement.select("div.details-title").remove();
                        Elements aList = contentElement.select("a");
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
                                if (href.indexOf("/") == 0) {
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
                        Elements imgList = doc.select("IMG");
                        for (Element img : imgList) {
                            String href = img.attr("src");
                            if (href.length() > 10 && href.indexOf("http") != 0) {
                                if (href.indexOf("../") == 0) {
                                    href = href.replace("../", "");
                                    href = baseUrl + "/" + href;
                                    img.attr("src", href);
                                } else if (href.indexOf("./") == 0) {
                                    href = url.substring(0, url.lastIndexOf("/") + 1) + href.substring(2);
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
                        Element titleElement = doc.select("div.content-title").first();//详情title
                        if (titleElement != null) {
                            title = titleElement.text().trim();
                        }
                        contentElement.select("script").remove();
                        contentElement.select("style").remove();
                        content = title + contentElement.outerHtml();
                    } else if (url.contains(".doc") || url.contains(".pdf") || url.contains(".zip") || url.contains(".xls")) {
                        content = "<div>附件下载：<a href='" + url + "'>" + branch.getTitle() + "</a></div>";
                        detailHtml = Jsoup.parse(content).toString();
                    }
                    RecordVO recordVO = new RecordVO();
                    recordVO.setId(branch.getId());
                    recordVO.setListTitle(branch.getTitle());
                    recordVO.setTitle(title);
                    recordVO.setDetailLink(branch.getDetailLink());
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



