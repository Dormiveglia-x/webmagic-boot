package com.bidizhaobiao.data.Crawl.service.impl.DX011686
        ;

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
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @程序员: 潘嘉明 日期：2022-02-15 13:38
 * @原网站: https://www.cmjt.com.cn/#/channellist?id=187
 * @主页：TODO
 **/
@Service("DX011686_ZhongbXxService")
public class DX011686_ZhongbXxService extends SpiderService implements PageProcessor {

    public Spider spider = null;

    public String listUrl = "https://www.cmjt.com.cn/api/v1/news/187?orgId=44&offset=0&limit=5";

    public String baseUrl = "https://www.cmjt.com.cn";
    // 网站编号
    public String sourceNum = "DX011686";
    // 网站名称
    public String sourceName = "四川省煤炭产业集团公司";
    // 信息源
    public String infoSource = "企业采购";
    // 设置地区
    public String area = "西南";
    // 设置省份
    public String province = "四川";
    // 设置城市
    public String city = "成都";
    // 设置县
    public String district = "";
    // 设置CreateBy
    public String createBy = "潘嘉明";

    public boolean isNeedSaveFileAddSSL = true;

    public Pattern p = Pattern.compile("(\\d{4})(年|/|-)(\\d{1,2})(月|/|-)(\\d{1,2})");

    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(3).setTimeOut(50000).setSleepTime(20);

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
                .setDownloader(new MyDownloader(serviceContext, true, listUrl));
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
        String html = page.getRawText();
        try {
            if (url.contains("offset")) {
                JSONObject root = new JSONObject(html);
                if (serviceContext.getPageNum() == 1) {
                    String total = root.get("total").toString();
                    int max = Integer.parseInt(total);
                    max = max % 5 == 0 ? max / 5 : max / 5 + 1;
                    serviceContext.setMaxPage(max);
                }
                JSONArray list = root.getJSONArray("type");
                List<BranchNew> detailList = new ArrayList<BranchNew>();
                if (list.length() > 0) {
                    for (int i = 0; i < list.length(); i++) {
                        JSONObject item = list.getJSONObject(i);
                        String title = item.get("title").toString();
                        if (!CheckProclamationUtil.isProclamationValuable(title)) {
                            continue;
                        }

                        String id = item.get("id").toString();
                        String link = "https://www.cmjt.com.cn/api/v1/news/getArticle/" + id;
                        String detailLink = "https://www.cmjt.com.cn/#/leftlist?id=" + id;
                        String date = item.get("check_time").toString();
                        Matcher matcher = p.matcher(date);
                        if (matcher.find()) {
                            date = matcher.group().replaceAll("[\\.|年|月|/]", "-");
                            date = SpecialUtil.date2Str(SpecialUtil.str2Date(date));
                        }

                        dealWithNullTitleOrNullId(serviceContext, title, id);
                        BranchNew branch = new BranchNew();
                        branch.setId(id);
                        branch.setTitle(title);
                        serviceContext.setCurrentRecord(id);
                        branch.setDetailLink(detailLink);
                        branch.setLink(link);
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

                if (serviceContext.getPageNum() < serviceContext.getMaxPage() && serviceContext.isNeedCrawl()) {
                    serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                    int offset = serviceContext.getPageNum() - 1;
                    page.addTargetRequest(listUrl.replace("offset=0", "offset=" + offset));
                }
            } else {
                BranchNew branchNew = map.get(url);
                if (branchNew == null) {
                    return;
                }
                String title = Jsoup.parse(branchNew.getTitle()).text().replace("...", "");
                String id = branchNew.getId();
                serviceContext.setCurrentRecord(id);
                String detailLink = branchNew.getDetailLink();
                String detailTitle = title;
                String date = branchNew.getDate();
                String content = "";
                JSONObject root = new JSONObject(html);
                String detailHtml = root.getJSONObject("article").get("text").toString();
                Document document = Jsoup.parse(detailHtml);
                Element contentE = document.select("body").first();
                if (contentE != null) {
                    contentE.select("iframe").remove();
                    contentE.select("input").remove();
                    contentE.select("button").remove();
                    contentE.select("script").remove();
                    if (contentE.select("a").first() != null) {
                        Elements as = contentE.select("a");
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
                    if (contentE.select("img").first() != null) {
                        Elements imgs = contentE.select("img");
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
                    content = "<div>" + title + "</div>" + contentE.outerHtml();
                }
                if (url.contains(".doc") || url.contains(".pdf") || url.contains(".zip") || url.contains(".xls")) {
                    content = "<div>附件下载：<a href='" + url + "'>" + detailTitle + "</a></div>";
                    html = Jsoup.parse(content).toString();
                    date = SpecialUtil.date2Str(new Date());
                }
                RecordVO recordVO = new RecordVO();
                recordVO.setId(id);
                recordVO.setListTitle(title);
                recordVO.setDate(date);
                recordVO.setContent(content.replaceAll("\\ufeff|\\u2002|\\u200b|\\u2003", ""));
                recordVO.setTitle(detailTitle);//详情页标题
                recordVO.setDdid(SpecialUtil.stringMd5(html));//详情页md5
                recordVO.setDetailLink(detailLink);//详情页链接
                recordVO.setDetailHtml(html);
                logger.info("入库id==={}", id);
                dataStorage(serviceContext, recordVO, branchNew.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
            dealWithError(url, serviceContext, e);
        }
    }

}
