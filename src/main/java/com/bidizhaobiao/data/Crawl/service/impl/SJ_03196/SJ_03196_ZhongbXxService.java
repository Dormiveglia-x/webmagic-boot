package com.bidizhaobiao.data.Crawl.service.impl.SJ_03196;

import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
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
 * 程序员：莫浩
 * 日期：2021-03-04
 * 主页：http://www.sxnxzb.cn/news/8/
 **/

@Service
public class SJ_03196_ZhongbXxService extends SpiderService implements PageProcessor {

    public Spider spider = null;

    public String listUrl = "http://www.sxnxzb.cn/news/8/#c_portalResNews_list-15612569680821949-1";
    public String baseUrl = "http://www.sxnxzb.cn";
    // 网站编号
    public String sourceNum = "069831";
    // 网站名称
    public String sourceName = "山西能鑫工程招标代理有限公司";
    // 信息源
    public String infoSource = "政府采购";
    // 设置地区
    public String area = "华北";
    // 设置省份
    public String province = "山西";
    // 设置城市
    public String city = "";
    // 设置县
    public String district = "";
    public String createBy = "莫浩";
    public Pattern pDate = Pattern.compile("(\\d{4})(年|/|-|\\.)(\\d{1,2})(月|/|-|\\.)(\\d{1,2})");
    public Pattern pMaxPage = Pattern.compile("totalPage:\"4\",");
    // 抓取网站的相关配置，包括：编码","抓取间隔","重试次数等
    Site site = Site.me().setCycleRetryTimes(2).setTimeOut(30000).setSleepTime(20);

    // 信息源
    public Site getSite() {
        return this.site.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36");
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
        try {
            List<BranchNew> detailList = new ArrayList<BranchNew>();
            Thread.sleep(500);
            if (url.contains("c_portalResNews_list")) {
                Document doc = Jsoup.parse(page.getRawText());
                    Elements lis = doc.select("div.newList");
                    Elements lis1 = doc.select("div[class=  newList1  ]");
                    if (lis.size() > 0) {
                        for (int i = 0; i < lis.size(); i++) {
                            Element a = lis.get(i).select("a").first();
                            String id = a.attr("href").trim();
                            String href = baseUrl + id;
                            String title = "";
                            if (a.hasAttr("title")) {
                                title = a.attr("title").trim();
                            } else {
                                title = a.text().trim().replace("...", "");
                            }
                            String date = lis.get(i).text().trim().replace("--","-");
                            Matcher m = pDate.matcher(date);
                            if (m.find()) {
                                String month = m.group(3).length() == 2 ? m.group(3) : "0" + m.group(3);
                                String day = m.group(5).length() == 2 ? m.group(5) : "0" + m.group(5);
                                date = m.group(1) + "-" + month + "-" + day;
                                if (Integer.parseInt(m.group(1)) < 2016) {
                                    continue;
                                }
                            }
                            BranchNew branch = new BranchNew();
                            branch.setId(id);
                            branch.setLink(href);
                            branch.setTitle(title);
                            branch.setDate(date);
                            serviceContext.setCurrentRecord(id);
                            detailList.add(branch);
                        }if (lis1.size() > 0) {
                            for (int i = 0; i < lis1.size(); i++) {
                                Element a = lis1.get(i).select("a").first();
                                String id = a.attr("href").trim();
                                String href = baseUrl + id;
                                String title = lis1.get(i).select(".newTitle").text();
                                String date = lis.get(i).text().trim().replace("--", "-");
                                Matcher m = pDate.matcher(date);
                                if (m.find()) {
                                    String month = m.group(3).length() == 2 ? m.group(3) : "0" + m.group(3);
                                    String day = m.group(5).length() == 2 ? m.group(5) : "0" + m.group(5);
                                    date = m.group(1) + "-" + month + "-" + day;
                                    if (Integer.parseInt(m.group(1)) < 2016) {
                                        continue;
                                    }
                                }
                                BranchNew branch = new BranchNew();
                                branch.setId(id);
                                branch.setLink(href);
                                branch.setTitle(title);
                                branch.setDate(date);
                                serviceContext.setCurrentRecord(id);
                                detailList.add(branch);
                            }
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
                String detailHtml = page.getHtml().toString();
                Document doc = Jsoup.parse(page.getRawText());
                String content = "";
                BranchNew bn = map.get(url);
                if (bn != null) {
                    String title = bn.getTitle();
                    String date = bn.getDate();
                    Element subject = doc.select("div[class=e_box p_articles]").first();
                    if (subject != null) {
                        Element time = doc.select("li.date").first();
                        Matcher m = pDate.matcher(time.text());
                        if (m.find()) {
                            String month = m.group(3).length() == 2 ? m.group(3) : "0" + m.group(3);
                            String day = m.group(5).length() == 2 ? m.group(5) : "0" + m.group(5);
                            date = m.group(1) + "-" + month + "-" + day;
                        }
                        Elements aList = subject.select("a");
                        for (Element a : aList) {
                            String href = a.attr("href");
                            if (href.startsWith("mail") || href.startsWith("HTTP")) {
                                continue;
                            }
                            if (href.startsWith("javascript") || href.contains("file:///C:/")) {
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
                        Elements imgList = subject.select("IMG");
                        for (Element img : imgList) {
                            String href = img.attr("src");
                            if (href.contains("data:image")) {
                                try {
                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                                    String dateString = formatter.format(new Date());
                                    String path = imgPath + "/" + dateString + "/" + bn.getDate() + "/" + sourceNum;
                                    String uuid = UUID.randomUUID().toString();
                                    String fileName = uuid + ".jpg";
                                    String newLink = "http://www.bidizhaobiao.com/file/" + dateString + "/" + bn.getDate()
                                            + "/" + sourceNum + "/" + fileName;
                                    // 文件保存位置
                                    File saveDir = new File(path);
                                    if (!saveDir.exists()) {
                                        saveDir.mkdirs();
                                    }
                                    byte[] imagedata = DatatypeConverter
                                            .parseBase64Binary(href.substring(href.indexOf(",") + 1));
                                    BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imagedata));
                                    ImageIO.write(bufferedImage, "png", new File(path + "/" + fileName));
                                    img.attr("src", newLink);
                                } catch (Exception e) {
                                    img.remove();
                                }
                                continue;
                            }
                            if (href.contains("file:///C:/")) {
                                img.remove();
                                continue;
                            }
                            if (href.startsWith("HTTP:")) {
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
                        subject.select("iframe").remove();
                        content = title + "<br>" + subject.html();
                    } else if (url.contains(".pdf")) {
                        content = title + "<br>详情链接：<a href='" + url + "'>" + title + "</a>";
                    }
                    RecordVO recordVO = new RecordVO();
                    recordVO.setId(bn.getId());
                    recordVO.setListTitle(title);
                    recordVO.setTitle(title);
                    recordVO.setDetailLink(url);
                    recordVO.setDetailHtml(detailHtml);
                    recordVO.setDate(date);
                    recordVO.setContent(content);
                    serviceContext.setCurrentRecord(bn.getId());
                    dataStorage(serviceContext, recordVO, bn.getType());
                }
            }
        } catch (Exception e) {
            dealWithError(url, serviceContext, e);
        }
    }
}
