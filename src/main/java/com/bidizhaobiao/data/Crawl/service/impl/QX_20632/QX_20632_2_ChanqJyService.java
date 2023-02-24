package com.bidizhaobiao.data.Crawl.service.impl.QX_20632;

import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 程序员：唐家逸 日期：2022-02-15
 * 原网站：https://jinpingqu.nongjiao.com/node/98664/landDataMore/1
 * 主页：https://jinpingqu.nongjiao.com
 **/
@Service
public class QX_20632_2_ChanqJyService extends SpiderService implements PageProcessor {
    public Spider spider = null;

    public String listUrl = "https://jinpingqu.nongjiao.com/node/98664/landSearchMore/1";
    public String baseUrl = "https://jinpingqu.nongjiao.com";
    public Pattern datePat = Pattern.compile("(\\d{4})(年|/|-|\\.)(\\d{1,2})(月|/|-|\\.)(\\d{1,2})");

    // 网站编号
    public String sourceNum = "20632-2";
    // 网站名称
    public String sourceName = "金平区农村产权交易所";
    // 信息源
    public String infoSource = "政府采购";
    // 设置地区
    public String area = "华南";
    // 设置省份
    public String province = "广东";
    // 设置城市
    public String city = "汕头市";
    // 设置县
    public String district = "金平区";
    public String createBy = "唐家逸";
    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(2).setTimeOut(30000).setSleepTime(20);

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
        spider.addRequest(new Request(listUrl));
        serviceContext.setSpider(spider);
        spider.run();
        // 爬虫状态监控部分
        saveCrawlResult(serviceContext);
    }

    private Pattern pattern = Pattern.compile("totalPages = '(.*?)';");
    public Pattern p = Pattern.compile("(20\\d{2})(年|/|-|\\.)(\\d{1,2})(月|/|-|\\.)(\\d{1,2})");


    public void process(Page page) {
        String url = page.getUrl().toString();
        try {

            List<BranchNew> detailList = new ArrayList<BranchNew>();
            Thread.sleep(500);
            if (url.equals(listUrl)) {
                Document document = Jsoup.parse(page.getRawText());
                Elements list = document.select(".tl_transferee");

                if (list.size() > 0) {
                    for (Element li : list) {

                        Element a = li.select("a").first();
                        String href = a.attr("href").trim();
                        String id = href.substring(href.lastIndexOf("/") + 1);
                        String link = href;

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
                if (list.size() >= 10 && serviceContext.isNeedCrawl()) {
                    serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                    int nextPage = serviceContext.getPageNum();
                    String nextUrl = "http://himc.org.cn/News/news/cid/2/p/"+nextPage+".html";
                    page.addTargetRequest(nextUrl);
                }

            } else {
                String detailHtml = page.getHtml().toString();
                String Content = "";
                BranchNew bn = map.get(url);
                if (bn != null) {
                    serviceContext.setCurrentRecord(bn.getId());
                    String Title = bn.getTitle().replace("...","");
                    String date = bn.getDate();
                    Document doc = Jsoup.parse(page.getRawText());
                    Element basecontent = doc.select("div.sub-page-content").first();
                    if (basecontent != null) {
                        //Title = doc.select("title").first().text().trim();

                        if (date.equals("")  || date == null) {
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
