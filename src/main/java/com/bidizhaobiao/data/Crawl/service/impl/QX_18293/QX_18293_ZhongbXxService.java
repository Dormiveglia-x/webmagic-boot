package com.bidizhaobiao.data.Crawl.service.impl.QX_18293;

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
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 程序员：刘伟伟  日期：2021-07-02
 * 原网站：http://www.qygqt.com/newslist.html
 * 主页：http://www.qygqt.com
 **/

@Service("QX_18293_ZhongbXxService")
public class QX_18293_ZhongbXxService extends SpiderService implements PageProcessor {

    public Spider spider = null;

    public String listUrl = "http://www.qygqt.com/handler/AjaxHeadler.aspx?OP=GetNewsList";

    public String baseUrl = "http://www.qygqt.com";
    // 网站编号
    public String sourceNum = "18293";
    // 网站名称
    public String sourceName = "青羊区团委";
    // 信息源
    public String infoSource = "政府采购";
    // 设置地区
    public String area = "西南";
    // 设置省份
    public String province = "四川";
    // 设置城市
    public String city = "成都市";
    // 设置县
    public String district = "青羊区";
    // 设置CreateBy
    public String createBy = "刘伟伟";
    public Pattern p = Pattern.compile("(\\d{4})(年|/|-)(\\d{1,2})(月|/|-)(\\d{1,2})");
    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(3).setTimeOut(30000).setSleepTime(20);

    public Site getSite() {
        return this.site.setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
    }

    public void startCrawl(int ThreadNum, int crawlType) {
        // 赋值
        serviceContextEvaluation();
        serviceContext.setCrawlType(crawlType);
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
            if (url.equals(listUrl)) {
                JSONObject object = new JSONObject(html);
                String R = object.get("R").toString();
                R = URLDecoder.decode(R, "UTF-8");
                R = R.substring(R.indexOf("["), R.lastIndexOf("]") + 1);
                R = R.replace("\\\\\"", "\\\"");
                JSONArray ds = new JSONArray(R);
                List<BranchNew> detailList = new ArrayList<BranchNew>();
                if (ds.length() > 0) {
                    for (int i = 0; i < ds.length(); i++) {
                        JSONObject info = ds.getJSONObject(i);
                        String title = info.get("N_Newsname").toString();
                        if (!CheckProclamationUtil.isProclamationValuable(title)) {
                            continue;
                        }
                        String id = "id=" + info.get("id").toString();
                        String link = "http://www.qygqt.com/news.html?" + id;
                        String date = info.get("N_Update").toString().replaceAll("[.|/|年|月]", "-");
                        Matcher m = p.matcher(date);
                        if (m.find()) {
                            date = SpecialUtil.date2Str(SpecialUtil.str2Date(m.group()));
                        }
                        String content = info.get("N_Newscontent").toString();
                        dealWithNullTitleOrNullId(serviceContext, title, id);
                        BranchNew branch = new BranchNew();
                        branch.setTitle(title);
                        branch.setId(id);
                        serviceContext.setCurrentRecord(id);
                        branch.setDetailLink(link);
                        branch.setLink(link);
                        branch.setDate(date);
                        branch.setContent(content);
                        detailList.add(branch);
                    }
                    // 校验数据,判断是否需要继续触发爬虫
                    List<BranchNew> needCrawlList = checkData(detailList, serviceContext);
                    for (BranchNew branch : needCrawlList) {
//                        map.put(branch.getLink(), branch);
//                        page.addTargetRequest(branch.getLink());
                        String title = Jsoup.parse(branch.getTitle()).text();
                        String id = branch.getId();
                        serviceContext.setCurrentRecord(id);
                        String date = branch.getDate();
                        String detailLink = branch.getDetailLink();
                        String detailTitle = title;
                        String content = branch.getContent();
                        Document document = Jsoup.parse(content);
                        Element contentE = document.select("body").first();
                        contentE.select("iframe").remove();
                        contentE.select("input").remove();
                        contentE.select("button").remove();
                        if (contentE.select("a") != null) {
                            Elements as = contentE.select("a");
                            for (Element a : as) {
                                String href = a.attr("href");
                                if ("".equals(href) || href == null
                                        || href.indexOf("#") == 0
                                        || href.contains("javascript:")) {
                                    a.removeAttr("href");
                                    continue;
                                }
                                if (!href.contains("@") && !"".equals(href) && !href.contains("javascript") && !href.contains("http") && !href.contains("#")) {
                                    if (href.contains("../")) {
                                        href = baseUrl + "/" + href.substring(href.lastIndexOf("./") + 1, href.length());
                                        a.attr("href", href);
                                        a.attr("rel", "noreferrer");
                                    } else if (href.startsWith("./")) {
                                        href = baseUrl + href.replace("./", "/");
                                        a.attr("href", href);
                                        a.attr("rel", "noreferrer");
                                    } else if (href.startsWith("/")) {
                                        href = baseUrl + href;
                                        a.attr("href", href);
                                        a.attr("rel", "noreferrer");
                                    } else {
                                        href = baseUrl + "/" + href;
                                        a.attr("href", href);
                                        a.attr("rel", "noreferrer");
                                    }
                                }
                            }
                        }
                        if (contentE.select("img").first() != null) {
                            Elements imgs = contentE.select("img");
                            for (Element img : imgs) {
                                String src = img.attr("src");
                                if (src.contains("data:image")) {
                                    try {
                                        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                                        String dateString = formatter.format(new Date());
                                        String path = imgPath + "/" + dateString + "/" + branch.getDate() + "/" + sourceNum;
                                        String uuid = UUID.randomUUID().toString();
                                        String fileName = uuid + ".jpg";
                                        String newLink = "http://www.bidizhaobiao.com/file/" + dateString + "/" + branch.getDate()
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
                                if (!src.contains("javascript") && !"".equals(src) && !src.contains("http")) {
                                    if (src.contains("../")) {
                                        src = baseUrl + "/" + src.substring(src.lastIndexOf("./") + 1, src.length());
                                        img.attr("src", src);
                                    } else if (src.startsWith("./")) {
                                        src = url.substring(0, url.lastIndexOf("/")) + src.replace("./", "/");
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
                        recordVO.setContent(content.replaceAll("\\ufeff|\\u2002|\\u200b|\\u2003", ""));
                        recordVO.setTitle(detailTitle);//详情页标题
                        recordVO.setDdid(SpecialUtil.stringMd5(html));//详情页md5
                        recordVO.setDetailLink(detailLink);//详情页链接
                        recordVO.setDetailHtml(html);
                        logger.info("入库id==={}", id);
                        dataStorage(serviceContext, recordVO, branch.getType());
                    }
                } else {
                    dealWithNullListPage(serviceContext);
                }
                if (serviceContext.getPageNum() < serviceContext.getMaxPage() && serviceContext.isNeedCrawl()) {
                    serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                    page.addTargetRequest(listUrl.replace("pageIndex=1", "pageIndex=" + serviceContext.getPageNum()));
                }
            } else {
                BranchNew branchNew = map.get(url);

            }
        } catch (Exception e) {
            e.printStackTrace();
            dealWithError(url, serviceContext, e);
        }
    }


}
