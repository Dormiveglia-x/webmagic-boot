package com.bidizhaobiao.data.Crawl.service.impl.SJ_09655;

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
 * 程序员：伍中林 日期：2022-01-04
 * 原网站：http://sft.hunan.gov.cn/sft/xxgk_71079/tzgg/index.html
 * 主页：http://sft.hunan.gov.cn/
 **/
@Service
public class SJ_09655_ZhaobGgService extends SpiderService implements PageProcessor {
    public Spider spider = null;

    public String listUrl = "http://sft.hunan.gov.cn/sft/xxgk_71079/tzgg/index.html";
    public String baseUrl = "http://sft.hunan.gov.cn";
    // 网站编号
    public String sourceNum = "09655";
    // 网站名称
    public String sourceName = "湖南省司法厅";
    // 设置地区
    public String area = "华中";
    // 设置省份
    public String province = "湖南";
    // 设置城市
    public String city;
    // 设置县
    public String district;
    // 设置县
    public String createBy = "伍中林";
    // 信息源
    public String infoSource = "政府采购";
    // 站源类型
    public String taskType;
    // 站源名称
    public String taskName;
    public Pattern pattern_page = Pattern.compile("createPageHTML\\('paging',(\\d+),");
    public Pattern p = Pattern.compile("(\\d{4})(年|/|-)(\\d{1,2})(月|/|-)(\\d{1,2})");
    // 是否需要入广联达
    public boolean isNeedInsertGonggxinxi = false;
    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数
    Site site = Site.me().setCycleRetryTimes(3).setTimeOut(30000).setSleepTime(20);

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
        String html = page.getHtml().toString();
        String url = page.getUrl().toString();
        try {
            Thread.sleep(500);
            if (url.contains("index")) {
                Matcher matcher;
                if (serviceContext.getPageNum() == 1) {
                    matcher = pattern_page.matcher(page.getRawText());
                    if (matcher.find()) {
                        String total = matcher.group(1);
                        int maxPage = Integer.parseInt(total);
                        serviceContext.setMaxPage(maxPage);
                    }
                }
                Document document = Jsoup.parse(html);
                Element divE = document.select("div[class=box]").first();
                if (divE != null) {
                    Elements lis = divE.select("li:has(a)");
                    List<BranchNew> detailList = new ArrayList<>();
                    if (lis.size() > 0) {
                        String key = "招标、采购、询价、询比、竞标、竞价、竞谈、竞拍、竞卖、竞买、竞投、竞租、比选、比价、竞争性、谈判、磋商、投标、邀标、议标、议价、单一来源、遴选、标段、明标、明投、出让、转让、拍卖、招租、预审、发包、开标、答疑、补遗、澄清、挂牌";
                        String[] keys = key.split("、");
                        for (Element li : lis) {
                            Element a = li.select("a").first();
                            String title = "";
                            if (a.hasAttr("title")) {
                                title = a.attr("title").trim();
                                if (title.equals("")) {
                                    title = a.text().trim();
                                }
                            } else {
                                title = a.text().trim();
                            }
                            if (!CheckProclamationUtil.isProclamationValuable(title, keys)) {
                                continue;
                            }
                            String href = a.attr("href");
                            String id = href.substring(href.lastIndexOf("/") + 1);
                            String link = baseUrl + href;

                            String date = li.select("span").first().text().replaceAll("[.|/|年|月]", "-");
                            Matcher m = p.matcher(date);
                            if (m.find()) {
                                date = SpecialUtil.date2Str(SpecialUtil.str2Date(m.group()));
                            }
                            dealWithNullTitleOrNullId(serviceContext, title, id);
                            BranchNew branchNew = new BranchNew();
                            branchNew.setId(id);
                            serviceContext.setCurrentRecord(id);
                            branchNew.setLink(link);
                            branchNew.setDate(date);
                            branchNew.setDetailLink(link);
                            branchNew.setTitle(title);
                            map.put(link, branchNew);
                            page.addTargetRequest(link);
                            detailList.add(branchNew);
                        }
                        // 校验数据
//                        List<BranchNew> branchNewList = checkData(detailList, serviceContext);
//                        for (BranchNew branch : branchNewList) {
//                        map.put(link, branchNew);
//                            page.addTargetRequest(branch.getLink());
//                        }
                    } else {
                        dealWithNullListPage(serviceContext);
                    }
                }
                // 翻页连接
                if (serviceContext.getPageNum() < serviceContext.getMaxPage() && serviceContext.isNeedCrawl()) {
                    serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                    page.addTargetRequest(listUrl.replace("index", "index_" + serviceContext.getPageNum()));
                }
            } else {
                BranchNew branchNew = map.get(url);
                serviceContext.setCurrentRecord(branchNew.getId());
                String title = Jsoup.parse(branchNew.getTitle()).text();
                String id = branchNew.getId();
                String date = branchNew.getDate();
                String detailLink = branchNew.getDetailLink();
                String detailTitle = title;
                String content = "";
                String path = detailLink.substring(0, detailLink.lastIndexOf("/"));
                Document document = Jsoup.parse(html.toString());
                Element contentE = document.select("div#j-show-body").first();
                contentE.select("div[class=tys-main-zt-c clearfix1]").remove();
                if (contentE == null)
                    return;
                // contentE.removeAttr("style");
                contentE.select("iframe").remove();
                contentE.select("style").remove();
                contentE.select("input").remove();
                contentE.select("script").remove();
                if (contentE.select("a") != null) {
                    Elements as = contentE.select("a");
                    for (Element a : as) {
                        String href = a.attr("href");
                        if ("".equals(href) || href == null || href.indexOf("#") == 0 || href.contains("javascript:")) {
                            a.removeAttr("href");
                            continue;
                        }
                        if (!href.contains("@") && !"".equals(href) && !href.contains("javascript")
                                && !href.contains("http") && !href.contains("#")) {
                            if (href.contains("../")) {
                                href = path + "/" + href.substring(href.lastIndexOf("./") + 1, href.length());
                                a.attr("href", href);
                                a.attr("rel", "noreferrer");
                            } else if (href.startsWith("./")) {
                                href = path + href.replace("./", "/");
                                a.attr("href", href);
                                a.attr("rel", "noreferrer");
                            } else if (href.startsWith("/")) {
                                href = path + href;
                                a.attr("href", href);
                                a.attr("rel", "noreferrer");
                            } else {
                                href = path + "/" + href;
                                a.attr("href", href);
                                a.attr("rel", "noreferrer");
                            }
                        }
                        // a.attr("rel", "noreferrer");
                    }
                }
                if (contentE.select("img").first() != null) {
                    Elements imgs = contentE.select("img");
                    for (Element img : imgs) {
                        String src = img.attr("src");
                        if (src.contains("file://")) {
                            img.remove();
                            continue;
                        }
                        if (!src.contains("javascript") && !"".equals(src) && !src.contains("http")) {
                            if (src.contains("../")) {
                                src = baseUrl + "/" + src.substring(src.lastIndexOf("./") + 1, src.length());
                                img.attr("src", src);
                            } else if (src.startsWith("./")) {
                                src = baseUrl + src.replace("./", "/");
                                img.attr("src", src);
                            } else if (src.startsWith("/")) {
                                src = baseUrl + src;
                                img.attr("src", src);
                            } else {
                                src = baseUrl + "/" + src;
                                img.attr("src", src);
                            }
                        }
                    }
                }
                content = "<div>" + title + "</div>" + contentE.outerHtml();
                RecordVO recordVO = new RecordVO();
                recordVO.setId(id);
                recordVO.setListTitle(title);
                recordVO.setDate(date);
                recordVO.setContent(content.replaceAll("\\u2002", " "));
                recordVO.setTitle(detailTitle);// 详情页标题
                recordVO.setDdid(SpecialUtil.stringMd5(html.toString()));// 详情页md5
                recordVO.setDetailLink(detailLink);// 详情页链接
                recordVO.setDetailHtml(html.toString());
                dataStorage(serviceContext, recordVO, branchNew.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
            dealWithError(url, serviceContext, e);
        }
    }
}
