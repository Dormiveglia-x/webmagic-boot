package com.bidizhaobiao.data.Crawl.service.impl.DS_18888;


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

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 程序员：白嘉全  日期：2022-03-11
 * 原网站：http://www.aqnews.com.cn/aqxw/msxw/list.shtml
 * 主页：http://www.aqnews.com.cn
 **/

@Service("DS_18888_ZhaobGgService")
public class DS_18888_ZhaobGgService extends SpiderService implements PageProcessor {

    public Spider spider = null;

    public String listUrl = "http://www.aqnews.com.cn/aqxw/msxw/NewsList_0.json";

    public String baseUrl = "http://www.aqnews.com.cn";

    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(3).setTimeOut(30000).setSleepTime(20);
    // 网站编号
    public String sourceNum = "18888";
    // 网站名称
    public String sourceName = "安庆新闻网";
    // 信息源
    public String infoSource = "政府采购";
    // 设置地区
    public String area = "华东";
    // 设置省份
    public String province = "安徽";
    // 设置城市
    public String city = "安庆市";
    // 设置县
    public String district;
    // 设置CreateBy
    public String createBy = "白嘉全";


    public Pattern p = Pattern.compile("(\\d{4})(年|/|-|\\.)(\\d{1,2})(月|/|-|\\.)(\\d{1,2})");
    public Pattern p_page = Pattern.compile("pageCount:(\\d+),");

    public Site getSite() {
        return this.site.setUserAgent(
                "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
    }


    public void startCrawl(int ThreadNum, int crawlType) {
        // 赋值
        serviceContextEvaluation();
        serviceContext.setCrawlType(crawlType);
        serviceContext.setSaveFileAddSSL(false);
        serviceContext.setSaveFileAddRef(false);
        // 保存日志
        saveCrawlLog(serviceContext);
        // 启动爬虫
        spider = Spider.create(this).thread(ThreadNum)
                .setDownloader(new MyDownloader(serviceContext, false, listUrl));
        Request request = new Request(listUrl);
        spider.addRequest(request);
        serviceContext.setSpider(spider);
        spider.run();
        // 爬虫状态监控部分
        saveCrawlResult(serviceContext);
    }

    public void process(Page page) {

        String html = page.getRawText();
        String url = page.getUrl().toString();
        try {
            List<BranchNew> detailList = new ArrayList<BranchNew>();
            if (url.contains("NewsList_")) {
                JSONObject json = new JSONObject(page.getRawText());
                if (json.has("info")) {
                    JSONArray arr = json.getJSONArray("info");
                    if (arr.length() > 0) {
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            String id = obj.get("id").toString();
                            String link = obj.getString("url");
                            String detailLink = link;
                            Matcher m = p.matcher(obj.getString("createTime"));
                            String date = "";
                            if (m.find()) {
                                String month = m.group(3).length() == 2 ? m.group(3) : "0" + m.group(3);
                                String day = m.group(5).length() == 2 ? m.group(5) : "0" + m.group(5);
                                date = m.group(1) + "-" + month + "-" + day;
                            }
                            String title = obj.getString("title").trim();
                            String key = "询标、交易、机构、需求、废旧、废置、处置、报废、供应商、承销商、服务商、调研、优选、择选、择优、选取、公选、选定、摇选、摇号、摇珠、抽选、定选、定点、招标、采购、询价、询比、竞标、竞价、竞谈、竞拍、竞卖、竞买、竞投、竞租、比选、比价、竞争性、谈判、磋商、投标、邀标、议标、议价、单一来源、标段、明标、明投、出让、转让、拍卖、招租、出租、预审、发包、承包、分包、外包、开标、遴选、答疑、补遗、澄清、延期、挂牌、变更、预公告、监理、改造工程、报价、小额、零星、自采、商谈";
                            String[] keys = key.split("、");
                            if (!CheckProclamationUtil.isProclamationValuable(title, keys)) {
                                continue;
                            }
                            BranchNew branch = new BranchNew();
                            serviceContext.setCurrentRecord(id);
                            branch.setLink(link);
                            branch.setDetailLink(detailLink);
                            branch.setId(id);
                            branch.setDate(date);
                            branch.setTitle(title);
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
                } else {
                    dealWithNullListPage(serviceContext);
                }
                if (serviceContext.getPageNum() == 1) {
                    int pageNum = json.getInt("pageNum");
                    serviceContext.setMaxPage(pageNum);
                }
                if (serviceContext.getPageNum() < serviceContext.getMaxPage() && serviceContext.isNeedCrawl()) {
                    page.addTargetRequest(listUrl.replace("NewsList_0", "NewsList_" + serviceContext.getPageNum()));
                    serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                }

            } else {
                BranchNew branch = map.get(url);
                if (branch != null) {
                    map.remove(url);
                    serviceContext.setCurrentRecord(branch.getId());
                    String detailHtml = page.getRawText();
                    Document doc = Jsoup.parse(detailHtml.replace("&nbsp", ""));
                    String title = branch.getTitle().replace("...", "");
                    String date = branch.getDate();
                    String content = "";
                    Element contentElement = doc.select("div[class=article-main font-18]").first();
                    if (contentElement != null) {
                        Elements aList = contentElement.select("a");
                        for (Element a : aList) {
                            String href = a.attr("href");
                            a.attr("rel", "noreferrer");
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
                                if (href.indexOf("//www") == 0) {
                                    href = baseUrl.substring(0, baseUrl.indexOf(":") + 1) + href;
                                    a.attr("href", href);
                                } else if (href.indexOf("../") == 0) {
                                    href = href.replace("../", "");
                                    href = baseUrl + "/" + href;
                                    a.attr("href", href);
                                } else if (href.startsWith("/")) {
                                    href = baseUrl + href;
                                    a.attr("href", href);
                                } else if (href.indexOf("./") == 0) {
                                    href = url.substring(0, url.lastIndexOf("/")) + href.substring(href.lastIndexOf("./") + 1);
                                    a.attr("href", href);
                                } else {
                                    href = url.substring(0, url.lastIndexOf("/") + 1) + href;
                                    a.attr("href", href);
                                }
                            }
                            href = href.replace(" ", "%20");
                            a.attr("href", href);
                        }
                        Elements imgList = contentElement.select("IMG");
                        for (Element img : imgList) {
                            String src = img.attr("src");
                            if (src.startsWith("file://")) {
                                img.remove();
                                continue;
                            }
                            if (src.contains("data:image")) {
                                try {
                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                                    String dateString = formatter.format(new Date());
                                    String path = imgPath + "/" + dateString + "/" + date + "/" + sourceNum;
                                    String uuid = UUID.randomUUID().toString();
                                    String fileName = uuid + ".jpg";
                                    String newLink = "http://www.bidizhaobiao.com/file/" + dateString + "/" + date
                                            + "/" + sourceNum + "/" + fileName;
                                    // 文件保存位置
                                    File saveDir = new File(path);
                                    if (!saveDir.exists()) {
                                        saveDir.mkdirs();
                                    }
                                    byte[] imagedata = DatatypeConverter
                                            .parseBase64Binary(src.substring(src.indexOf(",") + 1));
                                    BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imagedata));
                                    ImageIO.write(bufferedImage, "png", new File(path + "/" + fileName));
                                    img.attr("src", newLink);
                                } catch (Exception e) {
                                    img.remove();
                                }
                                continue;
                            }
                            if (src.length() > 10 && src.indexOf("http") != 0) {
                                if (src.indexOf("../") == 0) {
                                    src = src.replace("../", "");
                                    src = baseUrl + "/" + src;
                                    img.attr("src", src);
                                } else if (src.indexOf("./") == 0) {
                                    src = url.substring(0, url.lastIndexOf("/")) + src.substring(src.lastIndexOf("./") + 1);
                                    img.attr("src", src);
                                } else if (src.startsWith("//")) {
                                    src = baseUrl.substring(0, baseUrl.indexOf(":") + 1) + src;
                                    img.attr("src", src);
                                } else if (src.indexOf("/") == 0) {
                                    src = baseUrl + src;
                                    img.attr("src", src);
                                } else {
                                    src = url.substring(0, url.lastIndexOf("/") + 1) + src;
                                    img.attr("src", src);
                                }
                            }
                        }
                        contentElement.select("script").remove();
                        contentElement.select("style").remove();
                        content = contentElement.outerHtml();
                    } else if (url.contains(".doc") || url.contains(".pdf") || url.contains(".zip") || url.contains(".xls")) {
                        content = "<div>附件下载：<a href='" + url + "'>" + branch.getTitle() + "</a></div>";
                        detailHtml = Jsoup.parse(content).toString();
                    }
                    content = content.replaceAll("\\ufeff|\\u2002|\\u200b|\\u2003", "");
                    content = content.replace(";;", "");
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
            e.printStackTrace();
            dealWithError(url, serviceContext, e);
        }
    }


}
