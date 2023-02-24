package com.bidizhaobiao.data.Crawl.service.impl.QX_02338;

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
import us.codecraft.webmagic.selector.Html;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 程序员： 赖晓晖  日期：2022-03-11
 * 日期：2020-07-06
 * 原网站：http://www.anrenzf.gov.cn/15/34/41/default.htm
 * 主页：http://www.anrenzf.gov.cn
 **/
@Service
public class QX_02338_ZhaobGgService extends SpiderService implements PageProcessor {
    public Spider spider = null;
    public String listUrl = "http://www.anrenzf.gov.cn/15/34/41/default.htm";
    public String homeUrl = "http://www.anrenzf.gov.cn/";
    public String[] keys = "招商、询标、交易、机构、需求、废旧、废置、处置、报废、供应商、承销商、服务商、调研、优选、择选、择优、选取、公选、选定、摇选、摇号、摇珠、抽选、定选、定点、招标、采购、询价、询比、竞标、竞价、竞谈、竞拍、竞卖、竞买、竞投、竞租、比选、比价、竞争性、谈判、磋商、投标、邀标、议标、议价、单一来源、标段、明标、明投、出让、转让、拍卖、招租、出租、预审、发包、承包、分包、外包、开标、遴选、答疑、补遗、澄清、延期、挂牌、变更、预公告、监理、改造工程、报价、小额、零星、自采、商谈".split("、");

    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数
    public Site site = Site.me().setCycleRetryTimes(2).setTimeOut(30000).setSleepTime(20);
    public Pattern p = Pattern.compile("(?<year>\\d{2})-(?<month>\\d{1,2})-(?<day>\\d{1,2})");
    // 网站编号
    public String sourceNum = "02338";
    // 网站名称
    public String sourceName = "安仁县人民政府";
    // 信息源
    public String infoSource = "政府采购";
    // 设置地区
    public String area = "华中";
    // 设置省份
    public String province = "湖南";
    // 设置城市
    public String city = "郴州市";
    // 设置县
    public String district = "安仁县";
    // 设置县
    public String createBy = "白嘉全";
    //站源类型
    public String taskType;
    //站源名称
    public String taskName;


    public Site getSite() {
        return this.site;
    }

    public void startCrawl(int ThreadNum, int crawlType) {
        // 赋值
        serviceContextEvaluation();
        // 保存日志
        saveCrawlLog(serviceContext);
        serviceContext.setCrawlType(crawlType);
        // 启动爬虫
        spider = Spider.create(this).thread(ThreadNum).setDownloader(new MyDownloader(serviceContext, true, listUrl));
        spider.addRequest(new Request(listUrl));
        serviceContext.setSpider(spider);
        spider.run();
        // 爬虫状态监控部分
        saveCrawlResult(serviceContext);
    }

    public void process(Page page) {
        Html html = page.getHtml();
        String url = page.getUrl().toString();
        List<BranchNew> detailList = new ArrayList<>();
        Document doc = page.getHtml().getDocument();
        try {
            Thread.sleep(500);
            // 判断是否是翻页连接
            if (url.contains("default")) {
                List<String> times = page.getHtml().xpath("//ul[@class='article']/li[@class='date']/text()").all();
                Elements lis = doc.select(".lbcc-nr li");
                if (lis.size() > 0) {
                    for (Element li : lis) {
                        String title = li.select("a").attr("title");
                        if ("".equals(title)) {
                            title = li.select("a").text();
                        }
                        if (!CheckProclamationUtil.isProclamationValuable(title, keys)) {
                            continue;
                        }
                        String href = li.select("a").attr("href");
                        href = listUrl.substring(0, listUrl.lastIndexOf("/") + 1) + href;
                        String id = href.substring(href.lastIndexOf("_") + 1);
                        String listTime = li.select("span").text();
                        BranchNew branch = new BranchNew();
                        branch.setId(id);
                        branch.setTitle(title);
                        branch.setLink(href);
                        branch.setDate(listTime);
                        detailList.add(branch);
                    }
                    // 校验数据,判断是否需要继续触发爬虫
                    List<BranchNew> needCrawlList = checkData(detailList, serviceContext);
                    for (BranchNew branch : needCrawlList) {
                        map.put(branch.getLink(), branch);
                        page.addTargetRequest(branch.getLink());
                    }
                    String href = doc.select("a:contains(>)").attr("href");
                    if (!"".equals(href) && href != null && serviceContext.isNeedCrawl()) {
                        serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                        if (href.contains("default")) {
                            href = listUrl.substring(0, listUrl.lastIndexOf("/") + 1) + href;
                            page.addTargetRequest(href);
                        }
                    }
                } else {
                    dealWithNullListPage(serviceContext);
                }
            } else {
                BranchNew branch = map.get(url);
                if (branch != null) {
                    Elements content = doc.select(".xl-xqnr");
                    String detailTitle = null;
                    String detailContent = null;
                    if (content.size() > 0) {
                        //补全附件链接
                        Elements as = content.select("a");
                        for (Element a : as) {
                            //当href出现 / ../  ../../
                            String link = a.attr("href");
                            if (link.indexOf("http") != 0) {
                                if (link.indexOf("/") == 0) {
                                    link = homeUrl + link.substring(link.indexOf("/") + 1);
                                } else if (link.indexOf("../") == 0) {
                                    link = link.replace("../", "");
                                    link = homeUrl + link;//禁止盗链， 请从本网站上下载!
                                } else if (link.indexOf("./") == 0) {
                                    link = homeUrl + link.replace("./", "");
                                } else {// 否则 直接文件名
                                    if (!"".equals(link)) {
                                        link = homeUrl + link;
                                    }
                                }
                                a.attr("href", link);
                            }
                        }
                        //补全图片链接
                        Elements imgs = content.select("img");
                        for (Element img : imgs) {
                            //当src出现 / ../  ../../
                            String src = img.attr("src");
                            if (src.indexOf("http") != 0) {
                                if (src.indexOf("/") == 0) {
                                    src = homeUrl + src.substring(src.indexOf("/") + 1);
                                } else if (src.indexOf("../") == 0) {
                                    src = src.replace("../", "");
                                    src = homeUrl + src;//禁止盗链， 请从本网站上下载!
                                } else if (src.indexOf("./") == 0) {
                                    src = homeUrl + src.replace("./", "");
                                } else {// 否则 直接文件名
                                    if (!"".equals(src)) {
                                        src = homeUrl + src;
                                    }
                                }
                                img.attr("src", src);
                            }
                        }
                        /*body里有一下东西需要清除**/
                        content.select("script").remove();
                        content.select("style").remove();
                        content.select("#div_div").remove();
                        detailTitle = doc.select(".xlnk h2").text();
                        detailContent = detailTitle + content.html();
                    }
                    RecordVO recordVO = new RecordVO();
                    recordVO.setId(branch.getId());
                    recordVO.setTitle(detailTitle);
                    recordVO.setListTitle(branch.getTitle());
                    recordVO.setDetailLink(url);
                    recordVO.setDate(branch.getDate());
                    recordVO.setContent(detailContent);
                    recordVO.setDetailHtml(html != null ? html.toString() : Jsoup.parse(detailContent).html());
                    recordVO.setDdid(SpecialUtil.stringMd5(html != null ? html.toString() : Jsoup.parse(content.html()).html()));
                    dataStorage(serviceContext, recordVO, branch.getType());//入库操作（包括数据校验和入库）
                }
            }
        } catch (Exception e) {
            dealWithError(url, serviceContext, e);
        }
    }
}
