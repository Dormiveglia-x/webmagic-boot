package com.bidizhaobiao.data.Crawl.service.impl.DS_14892;

import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
import com.bidizhaobiao.data.Crawl.utils.CheckProclamationUtil;
import com.bidizhaobiao.data.Crawl.utils.SpecialUtil;
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
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 程序员：邓敏 日期：2021-11-02
 * 原网站：
 * 主页：
 **/

@Service
public class DS_14892_1_ZhaobGgService extends SpiderService implements PageProcessor {

    public Spider spider = null;

    public String listUrl = "http://www.czxdzb.com/ajax/ajaxLoadModuleDom_h.jsp?cmd=getWafNotCk_getAjaxPageModuleInfo&_colId=111&_extId=0&moduleId=565&href=%2Fcol.jsp%3Fm565pageno%3D1%26id%3D111&newNextPage=false&needIncToVue=false";
    public String baseUrl = "http://www.czxdzb.com";
    // 网站编号
    public String sourceNum = "14892-1";
    // 网站名称
    public String sourceName = "信达招标";
    // 信息源
    public String infoSource = "政府采购";
    // 设置地区
    public String area = "华东";
    // 设置省份
    public String province = "江苏";
    // 设置城市
    public String city = "常州市";
    // 设置县
    public String district = "";
    public String createBy = "邓敏";

    // 是否需要入广联达
    public boolean isNeedInsertGonggxinxi = false;
    // 站源类型
    public String taskType = "";
    // 站源名称
    public String taskName = "";
    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(2).setTimeOut(30000).setSleepTime(20);

    // 信息源
    public Site getSite() {
        Set<Integer> set=new HashSet<>();
        set.add(200);
        set.add(404);
        return this.site
                .setAcceptStatCode(set)
                .setUserAgent(
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36");
    }
    
    

    // 启动爬虫
    public void startCrawl(int ThreadNum, int crawlType) {
        // 赋值
        serviceContextEvaluation();
        // 保存日志
        serviceContext.setCrawlType(crawlType);
        saveCrawlLog(serviceContext);
        // 启动爬虫
        spider = Spider.create(this).thread(ThreadNum).setDownloader(new MyDownloader(serviceContext, false, listUrl));

        spider.addRequest(new Request(listUrl));

        serviceContext.setSpider(spider);
        spider.run();
        // 爬虫状态监控部分
        saveCrawlResult(serviceContext);
    }
    



    public Pattern p = Pattern.compile("(20\\d{2})(年|/|-|\\.)(\\d{1,2})(月|/|-|\\.)(\\d{1,2})");


    public void process(Page page) {
        String url = page.getUrl().toString();
        try {
            List<BranchNew> detailList = new ArrayList<BranchNew>();
            Thread.sleep(500);
            if (url.equals(listUrl)) {
                String rawText = page.getRawText();
                JSONObject jsonObject = new JSONObject(rawText);
                String s = jsonObject.getJSONObject("moduleInfo").get("content").toString();
                Document document = Jsoup.parse(s);
                Elements list = document.select(".jz_fix_ue_img tr:has(a)");
                if (list.size() > 0) {
                    for (Element li : list) {
                        Element a = li.select("a").first();
                        String href = a.attr("href").trim();
                        String id = href.substring(href.lastIndexOf("/") + 1);
                        String link = "http://www.czxdzb.com/"+href;

                        Matcher m = p.matcher(li.text());
                        String date = "";
                        if (m.find()) {
                            String month = m.group(3).length() == 2 ? m.group(3) : "0" + m.group(3);
                            String day = m.group(5).length() == 2 ? m.group(5) : "0" + m.group(5);
                            date = m.group(1) + "-" + month + "-" + day;
                        }
                        String title = a.attr("title");
                        if (title == null || title.length() < 2) {
                            title = a.text().replaceAll("\\s*", "").trim();
                        }
                        //分解招标规则
                        BranchNew branch = new BranchNew();
                        branch.setId(id);
                        branch.setLink(link);
                        serviceContext.setCurrentRecord(branch.getId());
                        branch.setTitle(title);
                        branch.setDate(date);
                        detailList.add(branch);
                    }
                    // 校验数据,判断是否需要继续触发爬虫
                    List<BranchNew> needCrawlList = checkData(detailList, serviceContext);
                    for (BranchNew branch : needCrawlList) {
                        map.put(branch.getLink(), branch);
                        //访问详情页
                        page.addTargetRequest(branch.getLink());
                    }
                } else {
                    dealWithNullListPage(serviceContext);
                }
                //下一页
              /*  if (list.size() >= 10 && serviceContext.isNeedCrawl()) {
                    serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                    int nextPage = serviceContext.getPageNum();
                    String nextUrl = "http://himc.org.cn/News/news/cid/2/p/"+nextPage+".html";
                    page.addTargetRequest(nextUrl);
                }*/

            } else {
                if (url.contains("http://www.czxdzb.com/nd.jsp?id=64#_jcp=1"))return;
                if (url.equals("http://www.czxdzb.com/"))return;
                if (page.getStatusCode()==404)return;
                String detailHtml = page.getHtml().toString();
                String Content = "";
                BranchNew bn = map.get(url);
                if (bn != null) {
                    serviceContext.setCurrentRecord(bn.getId());
                    String Title = bn.getTitle();
                    String date = bn.getDate();
                    Document doc = Jsoup.parse(page.getRawText());
                    Element basecontent = doc.select(".newsDetail ").first();
                    if (basecontent != null) {
                        Title = doc.select(".title").first().text().trim();

                        if (date == "" || date == null) {
                            Matcher m = p.matcher(basecontent.outerHtml());
                            if (m.find()) {
                                String month = m.group(3).length() == 2 ? m.group(3) : "0" + m.group(3);
                                String day = m.group(5).length() == 2 ? m.group(5) : "0" + m.group(5);
                                date = m.group(1) + "-" + month + "-" + day;
                            } else {
                                date = SpecialUtil.date2Str(new Date());
                            }
                        }
                        Elements aList = basecontent.select("a");
                        for (Element a : aList) {

                            String href = a.attr("href");
                            if (href.startsWith("mailto")) {
                                continue;
                            }
                            if (href.startsWith("javascript")) {
                                a.remove();
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
                                if (href.indexOf("//") == 0) {
                                    href = "http:" + href;
                                    a.attr("href", href);
                                } else if (href.indexOf("/") == 0) {
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

                        Elements imgList = basecontent.select("IMG");
                        for (Element img : imgList) {
                            String href = img.attr("src");
                            if (href.contains("icon_pdf")) {
                                img.remove();
                                continue;
                            }
                            if (href.length() > 10 && href.indexOf("http") != 0) {
                                if (href.indexOf("//") == 0) {
                                    href = "http:" + href;
                                    img.attr("src", href);
                                } else if (href.indexOf("../") == 0) {
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
                        //详情内容
                        //删除部分
                        basecontent.select("script").remove();
                        basecontent.select("span:contains(一篇)").remove();

                        Content = Title + "<br>" + basecontent.outerHtml().replaceAll("\\ufeff|\\u2002|\\u200b|\\u2003", "");
                    } else if (url.contains(".doc") || url.contains(".pdf") || url.contains(".zip")
                            || url.contains(".xls")) {
                        Content = "<div>附件下载：<a href='" + url + "'>" + Title + "</a></div>";
                        detailHtml = Jsoup.parse(Content).toString();
                        date = SpecialUtil.date2Str(new Date());
                    } else if (url.contains(".jpg") || url.contains(".jpeg") || url.contains(".png")) {
                        Content = "<div>查看图片：<img src='" + url + "'></img></div>";
                        detailHtml = Jsoup.parse(Content).toString();
                        date = SpecialUtil.date2Str(new Date());
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
                    //入库
                    dataStorage(serviceContext, recordVO, bn.getType());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            dealWithError(url, serviceContext, e);
        }
    }
    
    
    
}
