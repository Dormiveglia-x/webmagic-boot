package com.bidizhaobiao.data.Crawl.service.impl.SJ_04668;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 程序员：唐家逸
 * 日期：2021-11-26
 * 原网站：http://www.gzyxjl.com/index.php/list-18
 * 主页：http://www.gzyxjl.com
 **/
@Service("SJ_04668_ZhaobGgService")
public class  SJ_04668_ZhaobGgService extends SpiderService implements PageProcessor {
    public Spider spider = null;

    public String listUrl = "http://www.gzyxjl.com/index.php/list-18";
    public String baseUrl = "http://www.gzyxjl.com";
    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    public Site site = Site.me().setCycleRetryTimes(2).setTimeOut(30000).setSleepTime(20);

    // 网站编号
    public String sourceNum = "04668";
    // 网站名称
    public String sourceName = "广州市云兴建设工程监理有限公司";
    // 信息源
    public String infoSource = "政府采购";
    // 设置地区
    public String area = "华南";
    // 设置省份
    public String province = "广东";
    // 设置城市
    public String city;
    // 设置县
    public String district;
    // 设置CreateBy
    public String createBy = "唐家逸";

    //private Pattern pattern_page = Pattern.compile("1/(\\d+)");
    //private Pattern pattern_page = Pattern.compile("countPage =(\\d+)");
    private Pattern pattern = Pattern.compile("totalPage = parseInt\\('(.*?)'");
    private Pattern p = Pattern.compile("20\\d{2}-\\d{1,2}-\\d{1,2}");
    //是否需要入广联达
    private boolean isNeedInsertGonggxinxi = false;
    //站源类型
    private String taskType;
    //站源名称
    private String taskName;


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


    @Override
    public void process(Page page) {
        Document doc = page.getHtml().getDocument();
        String url = page.getUrl().toString();
        try {
            Thread.sleep(1000);
            if (url.equals(listUrl)) {
                if (page.getRawText().contains("lh-3 border-bottom-dashed")) {
                    Elements eachTags = doc.select("li[class=lh-3 border-bottom-dashed]:has(a)");
                    List<BranchNew> detailList = new ArrayList<>();
                    if (eachTags.size() > 0) {
                        for (Element eachTag : eachTags) {
                            if (eachTag.select("a").first() == null) continue;
                            String title = eachTag.select("a").first().attr("title").trim();
                            if (title.length() < 2) {
                                title = eachTag.select("a").first().text().trim();
                            }
                            String date = eachTag.text().trim();
                            date = date.replaceAll("[./年月]", "-");
                            Matcher m = p.matcher(date);
                            if (m.find()) {
                                date = sdf.get().format(sdf.get().parse(m.group()));
                            }
                            if (!CheckProclamationUtil.isProclamationValuable(title, null)) {
                                continue;
                            }
                            String id = eachTag.select("a").first().attr("href").trim();
                            String link = "http://www.gzyxjl.com" + id;
                            String DetailLink = link;
                            BranchNew bn = new BranchNew();
                            bn.setTitle(title);
                            bn.setId(id);
                            serviceContext.setCurrentRecord(bn.getId());
                            bn.setDate(date);
                            bn.setLink(link);
                            bn.setDetailLink(DetailLink);
                            detailList.add(bn);
                        }
                        // 校验数据List<BranchNew> detailList,int pageNum,String
                        List<BranchNew> needCrawlList = checkData(detailList, serviceContext);
                        for (BranchNew branch : needCrawlList) {
                            map.put(branch.getLink(), branch);
                            page.addTargetRequest(branch.getLink());
                        }
                    }
                } else {
                    dealWithNullListPage(serviceContext);
                }
            } else {
                String detailHtml = page.getHtml().toString();
                String Content = "";
                BranchNew bn = map.get(url);
                if (bn != null) {
                    serviceContext.setCurrentRecord(bn.getId());
                    String Title = bn.getTitle().replace("...","");
                    String date = bn.getDate();
                    Element basecontent = doc.select("body > div.container.pages > div:nth-child(3)").first();
                    if (basecontent != null) {

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
