package com.bidizhaobiao.data.Crawl.service.impl.QX_14000;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: 史炜立
 * @DateTime: 2020/10/22
 * @Description:乌鲁木齐市米东区政府
 * 原网站：http://www.xjmd.gov.cn/info/iList.jsp?cat_id=10081&cur_page=1
 * 主页：http://www.xjmd.gov.cn/
 */
@Service("QX_14000_ZhaobGgService")
public class QX_14000_ZhaobGgService extends SpiderService implements PageProcessor {


    public Spider spider = null;

    public String listUrl = "http://www.xjmd.gov.cn/info/iList.jsp?cat_id=10081&cur_page=1";
    public String baseUrl = "http://www.xjmd.gov.cn/";
    public Pattern datePat = Pattern.compile("(\\d{4})(年|/|-|\\.)(\\d{1,2})(月|/|-|\\.)(\\d{1,2})");
    public Pattern pagePat = Pattern.compile("(?<=共有).*?(?=条)");

    // 网站编号
    public String sourceNum = "14000";
    // 网站名称
    public String sourceName = "乌鲁木齐市米东区政府";
    // 信息源
    public String infoSource = "政府采购";
    // 设置地区
    public String area = "西北";
    // 设置省份
    public String province = "新疆";
    // 设置城市
    public String city = "乌鲁木齐";
    // 设置县
    public String district = "沙依巴克区";
    //创建人
    public String createBy = "史炜立";

    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(2).setTimeOut(30000).setSleepTime(20);
    // 是否需要入广联达
    public boolean isNeedInsertGonggxinxi = false;

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

    public void process(Page page) {
        String url = page.getUrl().toString();
        String detailHtml = page.getHtml().toString();
        Document doc = Jsoup.parse(detailHtml);
        try {
            List<BranchNew> detailList = new ArrayList<BranchNew>();
            Thread.sleep(500);
            if (url.contains("List")) {
                if (serviceContext.getPageNum() == 1) {
                    String text = doc.select("ul.am-pagination-centered li").first().text();
                    if (text != null) {
                        Matcher pageMat = pagePat.matcher(text);//匹配最大页数
                        if (pageMat.find()) {
                            if (pageMat.group() != null) {
                                int maxPage = Integer.parseInt(pageMat.group());
                                maxPage = (int) Math.ceil(maxPage / 20d);
                                serviceContext.setMaxPage(maxPage);
                            }
                        }
                    }


                }
                Elements listElement = doc.select("li.am-list-item-dated");//匹配列表
                if (listElement.size() > 0) {
                    String key = "项目、邀请、招标、采购、询价、询比、竞标、竞价、竞谈、竞拍、竞卖、竞买、竞投、竞租、比选、比价、竞争性、谈判、磋商、投标、邀标、议标、议价、单一来源、遴选、标段、明标、明投、出让、转让、拍卖、招租、预审、发包、开标、答疑、补遗、澄清、挂牌";
                    String[] keys = key.split("、");
                    for (Element element : listElement) {
                        Element a = element.select("a").first();

                        String link = a.attr("href");//详情链接
                        String title = a.text().trim();//详情title
                        String date = element.select("span").text();//详情时间
                        Matcher dateMat = datePat.matcher(date);
                        if (dateMat.find()) {
                            date = dateMat.group(1);
                            date += dateMat.group(3).length() == 2 ? "-" + dateMat.group(3) : "-0" + dateMat.group(3);
                            date += dateMat.group(5).length() == 2 ? "-" + dateMat.group(5) : "-0" + dateMat.group(5);
                        }

                        String id = link.substring(link.lastIndexOf("/"));
                        link = baseUrl + link.substring(link.indexOf("/") + 1);

                        if (!CheckProclamationUtil.isProclamationValuable(title, keys)) {//招标
                            continue;
                        }
                        BranchNew branch = new BranchNew();
                        branch.setId(id);
                        branch.setLink(link);
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
                    int pageNum = serviceContext.getPageNum() + 1;
                    page.addTargetRequest(listUrl.replace("cur_page=1", "cur_page=" + pageNum));
                    serviceContext.setPageNum(pageNum);
                }
            } else {
                BranchNew branch = map.get(url);
                if (branch != null) {
                    map.remove(url);
                    String title = branch.getTitle().replace("...", "");
                    String date = branch.getDate();
                    String content = "";
                    Element contentElement = doc.select("div#text").first();//详情页content
                    if (contentElement != null) {
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
                        Element titleElement = doc.select("h1#title").first();//详情title
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
                    recordVO.setDetailLink(url);
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
