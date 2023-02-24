package com.bidizhaobiao.data.Crawl.service.impl.DX000307;import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.model.HttpRequestBody;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.utils.HttpConstant;

/**
 * @author 作者: 廉建林
 * @version 创建时间：2018年8月9日 下午2:05:15 类说明
 */
@Service
public class DX000307_2_ZhongbXxService extends SpiderService implements PageProcessor {
    public  Spider spider = null;
    public  int successNum = 0;
    public  String listUrl = "http://oa.qxtt.com:8002/zhongbgg.aspx";
    // 入库状态
    public  int servicesStatus = 0;
    // 设置县
    public  Pattern p = Pattern.compile("(\\d{4})(年|/|-)(\\d{1,2})(月|/|-)(\\d{1,2})");
    public  String __VIEWSTATE = "";
    public  String __EVENTVALIDATION = "";
    public  String __EVENTTARGET = "GridView1";
    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(2).setTimeOut(30000).setSleepTime(20);
    // 网站编号
    public String sourceNum = "DX000307-2";
    // 网站名称
    public String sourceName = "山东齐星铁塔科技股份有限公司招投标平台";
    // 过时时间分割点
    public String SplitPointStr = "2016-01-01";
    // 信息源
    public String infoSource = "企业采购";
    // 设置地区
    public String area = "华东";
    // 设置省份
    public String province = "山东";
    // 设置城市
    public String city;
    // 设置县
    public String district;
    public String createBy = "唐家逸";
    public  double priod = 4;

    

    public Site getSite() {
        return this.site;
    }


    public void startCrawl(int ThreadNum, int crawlType) {
        // 赋值
        serviceContextEvaluation();
        serviceContext.setCrawlType(crawlType);
        saveCrawlLog(serviceContext);
        // 保存日志
        serviceContext.setMaxPage(106);
        // 启动爬虫
        spider = Spider.create(this).thread(ThreadNum)
                .setDownloader(new MyDownloader(serviceContext, false, listUrl));
        spider.addRequest(new Request(listUrl));
        serviceContext.setSpider(spider);
        spider.run();
        serviceContext.setSpider(spider);
        // 爬虫状态监控部分
        saveCrawlResult(serviceContext);
    }

    public void process(Page page) {
        String url = page.getUrl().toString();
        try {
            // HtmlPage htmlPage = client.getPage(url);
            List<BranchNew> detailList = new ArrayList<BranchNew>();
            Thread.sleep(500);
            // 判断是否是翻页连接
            if (url.contains(listUrl)) {
                Document doc = Jsoup.parse(page.getRawText());
                Element tab = doc.select("table[id=GridView1] tbody").first();
                if (tab != null) {
                    tab.select("table").remove();
                    Elements list = tab.select("tr:has(a)");
                    for (Element li : list) {
                        Element a = li.select("a").first();
                        String id = a.attr("href");
                        String link = "http://oa.qxtt.com:8002/" + id;
                        link = link.replace(" ", "%20");
                        // id = id.substring(id.indexOf("../") + 2);
                        id = id.substring(id.indexOf("num=") + 4);
                        String title = a.text().trim();
                        Matcher m = p.matcher(li.text());
                        String date = "";
                        while (m.find()) {
                            String month = m.group(3).length() == 2 ? m.group(3) : "0" + m.group(3);
                            String day = m.group(5).length() == 2 ? m.group(5) : "0" + m.group(5);
                            date = m.group(1) + "-" + month + "-" + day;
                        }
                        BranchNew branch = new BranchNew();
                        branch.setId(id);
                        branch.setLink(link);
                        branch.setTitle(title);
                        branch.setDate(date);
                        detailList.add(branch);
                    }
                    if (doc.getElementById("__VIEWSTATE") != null) {
                        __VIEWSTATE = doc.getElementById("__VIEWSTATE").attr("value");
                        __EVENTVALIDATION = doc.getElementById("__EVENTVALIDATION").attr("value");
                    }
                    // 校验数据List<BranchNew> detailList,int pageNum,String
                    // sourceNum
                    List<BranchNew> needCrawlList = checkData(detailList, serviceContext);
                    for (BranchNew branch : needCrawlList) {
                        map.put(branch.getLink(), branch);
                        page.addTargetRequest(branch.getLink());
                    }

                } else {
                    dealWithNullListPage(serviceContext);
                }
                if (serviceContext.getPageNum() < serviceContext.getMaxPage() && serviceContext.isNeedCrawl()) {
                    serviceContext.setPageNum((serviceContext.getPageNum() + 1));
                    // String nextPage = listUrl.replaceAll("page=(\\d+)&",
                    // "&page=" + serviceContext.getPageNum() + "&");
                    page.addTargetRequest(getRequest(serviceContext.getPageNum()));
                }
            } else {
                String Content = "";
                BranchNew bn = map.get(url);
                map.remove(url);
                String Title = bn.getTitle();
                String recordId = bn.getId();
                String Time = bn.getDate();
                // String detailCon = getContext(detailUrl);
                Document doc = Jsoup.parse(page.getRawText());
                Element tit = doc.select("span#lblTitle").first();
                if (tit != null) {
                    Content = tit.outerHtml();
                    Title = tit.text().trim();
                }
                // String content =doc.select("span#lblContent").html();
                Element div = doc.select("span#lblContent").first();
                if (div != null) {
                    div.select("input").remove();
                    Elements aList = div.select("a");
                    for (Element li : aList) {
                        String href = li.attr("href");
                        if (href.startsWith("/")) {
                            href = "http://oa.qxtt.com:8002" + href;
                            li.attr("href", href);
                        } else if (href.startsWith("../")) {
                            href = "http://oa.qxtt.com:8002" + href.substring(href.lastIndexOf("../") + 2);
                            li.attr("href", href);
                        }
                    }
                    Elements imgList = div.select("img");
                    for (Element li : imgList) {
                        String src = li.attr("src");
                        if (src.startsWith("/")) {
                            src = "http://oa.qxtt.com:8002" + src;
                            li.attr("src", src);
                        } else if (src.startsWith("../")) {
                            src = "http://oa.qxtt.com:8002" + src.substring(src.lastIndexOf("../") + 2);
                            li.attr("src", src);
                        }
                    }
                    Content += div.outerHtml();
                }
                Element file = doc.select("div[id=Panel1]").first();
				if (file != null) {
					Elements fList = file.select("a");
					for (Element f : fList) {
						String href = f.attr("href");
						if (href.startsWith("/")) {
							href = "http://oa.qxtt.com:8002" + href;
							f.attr("href", href);
						}
					}
					Content += file.outerHtml();
				}

                // Title = "";
                RecordVO recordVo = new RecordVO();
                        recordVo.setDetailLink(url);
                recordVo.setTitle(Title);
                recordVo.setContent(Content);
                recordVo.setId(recordId);
                recordVo.setDate(Time);
                // 入库操作（包括数据校验和入库）
                dataStorage(serviceContext, recordVo, bn.getType());
                //
            }
        } catch (Exception e) {
            e.printStackTrace();
            dealWithError(url, serviceContext, e);
            // e.printStackTrace();
        }
    }

    public Request getRequest(int page) {
        Request request = new Request(listUrl);
        request.setMethod(HttpConstant.Method.POST);
        // request.addHeader("Content-Type",
        // "application/x-www-form-urlencoded");
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("__EVENTTARGET", __EVENTTARGET);
            params.put("__EVENTARGUMENT", "Page$" + page);
            params.put("__VIEWSTATEGENERATOR", "83BEAC06");
            params.put("__VIEWSTATE", __VIEWSTATE);
            params.put("__EVENTVALIDATION", __EVENTVALIDATION);
            params.put("TextBox1", "");
            request.setRequestBody(HttpRequestBody.form(params, "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }
        return request;
    }
}
