package com.bidizhaobiao.data.Crawl.service.impl.DX006806;

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
import us.codecraft.webmagic.model.HttpRequestBody;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.utils.HttpConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 程序员：郑坚荣
 * 日期：2020-05-06
 * 原网站：http://6j.powerchina.cn/col/col4462/index.html
 * 主页：http://6j.powerchina.cn
 **/
@Service
public class DX006806_ZhaobGgService extends SpiderService implements PageProcessor {
    public  Spider spider = null;

    public  String listUrl = "http://6j.powerchina.cn/module/web/jpage/dataproxy.jsp?startrecord=1&endrecord=27&perpage=9";
    public  String baseUrl = "http://6j.powerchina.cn";
    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(3).setTimeOut(20000).setSleepTime(20);

    // 网站编号
    public String sourceNum = "DX006806";
    // 网站名称
    public String sourceName = "水电六局";
    // 信息源
    public  String infoSource = "企业采购";

    // 设置地区
    public String area = "全国";
    // 设置省份
    public String province;
    // 设置城市
    public String city;
    // 设置县
    public String district;
    // 设置县
    public String createBy = "郑坚荣";

    public  Pattern p = Pattern.compile("(\\d{4})(年|/|-|\\.)(\\d{1,2})(月|/|-|\\.)(\\d{1,2})");
    //是否需要入广联达
    public  boolean isNeedInsertGonggxinxi = false;
    //站源类型
    public  String taskType;
    //站源名称
    public  String taskName;


    public Site getSite() {
        return this.site.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36");
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
        spider.addRequest(request(listUrl));

        serviceContext.setSpider(spider);
        spider.run();
        // 爬虫状态监控部分
        saveCrawlResult(serviceContext);
    }

    public void process(Page page) {
        String url = page.getUrl().toString();
        try {
            List<BranchNew> detailList = new ArrayList<BranchNew>();
            Thread.sleep(500);
            if (url.contains("/dataproxy.jsp")) {
                Document document = Jsoup.parse(page.getRawText().replace("&nbsp;", " ").replace("&amp;", "&").replace("<![CDATA[", "").replace("]]>", ""));
                Elements list = document.select("record");
                if (list.size() > 0) {
                    for (int i = 0; i < list.size(); i++) {
                        Element li = list.get(i);
                        String link = li.select("a").attr("href");
                        String id = link;
                        link = baseUrl + link;

                        Matcher m = p.matcher(li.text());
                        String date = "";
                        if (m.find()) {
                            String month = m.group(3).length() == 2 ? m.group(3) : "0" + m.group(3);
                            String day = m.group(5).length() == 2 ? m.group(5) : "0" + m.group(5);
                            date = m.group(1) + "-" + month + "-" + day;
                        }
                        String title = li.select("a").attr("title");
                        if (title.length() < 2) {
                            title = li.select("a").text();
                        }
                        String key = "设备、出售、交易、机构、需求、废旧、废置、处置、报废、供应商、承销商、服务商、调研、优选、择选、择优、选取、公选、选定、摇选、摇号、摇珠、抽选、定选、定点、招标、采购、询价、询标、询比、竞标、竞价、竞谈、竞拍、竞卖、竞买、竞投、竞租、比选、比价、竞争性、谈判、磋商、投标、邀标、议标、议价、单一来源、标段、明标、明投、出让、转让、拍卖、招租、出租、预审、发包、承包、分包、外包、开标、遴选、答疑、补遗、澄清、延期、挂牌、变更、预公告、监理、改造工程、报价、小额、零星、自采、商谈";
                        String[] keys = key.split("、");
                        if (!CheckProclamationUtil.isProclamationValuable(title, keys)) {
                            continue;
                        }

                        BranchNew branch = new BranchNew();
                        branch.setId(id);
                        branch.setLink(link);
                        branch.setTitle(title);
                        branch.setDate(date);
                        detailList.add(branch);
                    }
                    // 校验数据,判断是否需要继续触发爬虫
                    List<BranchNew> needCrawlList = checkData(detailList, serviceContext);
                    for (BranchNew branch : needCrawlList) {
                        map.put(branch.getLink(), branch);
                        page.addTargetRequest(branch.getLink());
                    }
                } else {
                    dealWithNullListPage(serviceContext);
                }
                Element total = document.select("totalrecord").first();
                if (total != null && serviceContext.getPageNum() == 1) {
                    String totalPage = total.text();
                    int count = Integer.parseInt(totalPage);
                    count = count % 27 == 0 ? count / 27 : count / 27 + 1;
                    serviceContext.setMaxPage(count);
                }
                if (serviceContext.getPageNum() < serviceContext.getMaxPage() && serviceContext.isNeedCrawl()) {
                    serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                    String nextPage = listUrl.replace("startrecord=1&endrecord=27", "startrecord=" + ((serviceContext.getPageNum() - 1) * 27 + 1) + "&endrecord=" + serviceContext.getPageNum() * 27);
                    page.addTargetRequest(request(nextPage));
                }

            } else {
                String detailHtml = page.getHtml().toString();
                String Content = "";
                BranchNew bn = map.get(url);
                if (bn != null) {
                    String Title = bn.getTitle();
                    String date = bn.getDate();
                    Document doc = Jsoup.parse(page.getRawText());
                    Element div = doc.select("table[width=\"90%\"]").first();
                    if (div != null) {
                        Element titleText = div.select("td[class=title]").first();
                        if (titleText != null) {
                            Title = titleText.text().trim();
                        }
                        div.select("script").remove();
                        div.select("style").remove();
                        div.select("table[style=margin:20px 0 20px 0;]").remove();
                        Elements aList = div.select("a");
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
                        Elements imgList = div.select("IMG");
                        for (Element img : imgList) {
                            String href = img.attr("src");
                            if (href.contains("8606http")) {
                                href = href.substring(href.lastIndexOf("http"));
                                img.attr("src", href);
                            }
                            if (href.length() > 10 && href.indexOf("http") != 0) {
                                if (href.indexOf("../") == 0) {
                                    href = href.replace("../", "");
                                    href = baseUrl + "/" + href;
                                    img.attr("src", href);
                                } else if (href.indexOf("./") == 0) {
                                    href = url.substring(0, url.lastIndexOf("/") + 1) + href.substring(2);
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
                        Content += div.html().replace("display:none", "");

                    }
                    if (url.contains(".doc") || url.contains(".pdf") || url.contains(".zip") || url.contains(".xls")) {
                        Content = "<div>附件下载：<a href='" + url + "'>" + Title + "</a></div>";
                        detailHtml = Jsoup.parse(Content).toString();
                    }
                    RecordVO recordVO = new RecordVO();
                    recordVO.setId(bn.getId());
                    recordVO.setListTitle(bn.getTitle());
                    recordVO.setTitle(Title);
                    recordVO.setDetailLink(url);
                    recordVO.setDetailHtml(detailHtml);
                    recordVO.setDdid(SpecialUtil.stringMd5(detailHtml));
                    recordVO.setDate(date);
                    recordVO.setContent(Content);
                    dataStorage(serviceContext, recordVO, bn.getType());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            dealWithError(url, serviceContext, e);
        }
    }

    public Request request(String listUrl) {
        Request request = new Request(listUrl);
        request.setMethod(HttpConstant.Method.POST);
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("col", "1");
            map.put("appid", "1");
            map.put("webid", "51");
            map.put("path", "/");
            map.put("columnid", "4462");
            map.put("sourceContentType", "1");
            map.put("unitid", "21859");
            map.put("webname", "水电六局中文版");
            map.put("permissiontype", "0");
            request.setRequestBody(HttpRequestBody.form(map, "UTF-8"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return request;
    }


}
