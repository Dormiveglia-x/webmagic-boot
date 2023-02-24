package com.bidizhaobiao.data.Crawl.service.impl.XX6396;

import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
import com.bidizhaobiao.data.Crawl.utils.CheckProclamationUtil;
import com.bidizhaobiao.data.Crawl.utils.SpecialUtil;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
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
 * 程序员：许广衡  日期：2022-02-09
 * 原网站：https://www.gxltu.edu.cn/ltdt/tzgg.html/1
 * 主页：https://www.gxltu.edu.cn
 **/

@Service("XX6396_ZhaobGgService")
public class XX6396_ZhaobGgService extends SpiderService implements PageProcessor {
    public Spider spider = null;

    public String listUrl = "https://www.gxltu.edu.cn/ltdt/tzgg.html/1";

    public String baseUrl = "https://www.gxltu.edu.cn";

    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(3).setTimeOut(30000).setSleepTime(20);
    // 网站编号
    public String sourceNum = "XX6396";
    // 网站名称
    public String sourceName = "广西蓝天航空职业学院";
    // 信息源
    public String infoSource = "政府采购";
    // 设置地区
    public String area = "华南";
    // 设置省份
    public String province = "广西";
    // 设置城市
    public String city = "来宾市";
    // 设置县
    public String district = "";
    // 设置CreateBy
    public String createBy = "许广衡";

    public Pattern p = Pattern.compile("(\\d{4})(年|/|-)(\\d{1,2})(月|/|-)(\\d{1,2})");

    @Override
    public void process(Page page) {
        String url = page.getUrl().toString();
        String html = page.getHtml().toString();
        Document document;
        try {
            url = url.substring(url.indexOf("=") + 1);
            if (url.contains("ltdt")) {
                String detailHtml = getContent(url);
                int count = 2;
                while (!detailHtml.contains("items") && count > 0) {
                    detailHtml = getContent(url);
                    count--;
                }
                document = Jsoup.parse(detailHtml);
                List<BranchNew> detailList = new ArrayList<BranchNew>();
                Elements items = document.select("div[class=items] div[class=item]");
                if (items.size() > 0) {
                    for (Element item : items) {
                        Element a = item.select("a").first();
                        String href = a.attr("href").trim();
                        String title = a.text();
                        title = title.replace(" ", "");
                        // 获取时间
                        String time = item.select("span[class=text-muted]").first().text();
                        String date = time.replaceAll("[.|/|年|月|]", "-");
                        Matcher m = p.matcher(date);
                        if (m.find()) {
                            date = SpecialUtil.date2Str(SpecialUtil.str2Date(m.group()));
                        }
                        String id = href;

                        String keys = "项目、工程、设备、招标、采购、询价、询比、竞标、竞价、竞谈、竞拍、竞卖、竞买、竞投、竞租、比选、比价、竞争性、谈判、磋商、投标、邀标、议标、议价、单一来源、遴选、标段、明标、明投、出让、转让、拍卖、招租、预审、发包、开标、答疑、补遗、澄清、挂牌";
                        String rules[] = keys.split("、");
                        if (!CheckProclamationUtil.isValuableByExceptTitleKeyWords(title, rules)) {
                            continue;
                        }
                        String detailLink = href;
                        if (href.startsWith("/")) {
                            detailLink = baseUrl + href;
                        } else if (href.startsWith("./")) {
                            href = href.replace("./", "");
                            detailLink = baseUrl + "/" + href;
                        } else if (href.startsWith("../")) {
                            href = href.replace("../", "");
                            detailLink = baseUrl + "/" + href;
                        } else if (href.startsWith("http")) {
                            id = href.substring(href.lastIndexOf("/"), href.length());
                        }
                        dealWithNullTitleOrNullId(serviceContext, title, id);
                        BranchNew branch = new BranchNew();
                        branch.setTitle(title);
                        branch.setId(id);
                        serviceContext.setCurrentRecord(id);
                        branch.setDetailLink(detailLink);
                        branch.setLink(detailLink);
                        branch.setDate(date);
                        detailList.add(branch);
                    }

                    // 校验数据,判断是否需要继续触发爬虫
                    List<BranchNew> needCrawlList = checkData(detailList, serviceContext);
                    for (BranchNew branch : needCrawlList) {
                        map.put(branch.getLink(), branch);
                        page.addTargetRequest("https://www.baidu.com?wd=" + branch.getDetailLink());
                    }

                    if (serviceContext.getPageNum() == 1) {
                        Pattern pattern = Pattern.compile("pages: '(\\d)'");
                        Matcher matcher = pattern.matcher(detailHtml);
                        int maxNum = 0;
                        if (matcher.find()) {
                            String max = matcher.group(1);
                            maxNum = Integer.parseInt(max);
                        }
                        serviceContext.setMaxPage(maxNum);
                    }
                    if (serviceContext.getPageNum() < serviceContext.getMaxPage() && serviceContext.isNeedCrawl()) {
                        serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                        page.addTargetRequest("https://www.baidu.com?wd=https://www.gxltu.edu.cn/ltdt/tzgg.html/" + serviceContext.getPageNum());
                    }
                } else {
                    dealWithNullListPage(serviceContext);
                }
            } else {
                String detailHtml = getContent(url);
                int count = 2;
                while (!detailHtml.contains("cm-body") && count > 0) {
                    detailHtml = getContent(url);
                    count--;
                }
                document = Jsoup.parse(detailHtml);
                BranchNew branchNew = map.get(url);
                String id = branchNew.getId();
                serviceContext.setCurrentRecord(id);
                String date = branchNew.getDate();
                String detailLink = branchNew.getDetailLink();
                String title = Jsoup.parse(branchNew.getTitle()).text();
                Element element = document.select("div[class=cm-body]").first();
                String detailTitle = document.select("div[class=cm-title] div").first().text();
                String content = null;
                if (element != null) {
                    Elements as = element.select("a");
                    String newUrl = url.substring(0, url.lastIndexOf("/"));
                    if (as.size() > 0) {
                        // 解析附件
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
                                    href = newUrl + "/" + href.substring(href.lastIndexOf("./") + 1, href.length());
                                    a.attr("href", href);
                                    a.attr("rel", "noreferrer");
                                } else if (href.startsWith("./")) {
                                    href = newUrl + href.replace("./", "/");
                                    a.attr("href", href);
                                    a.attr("rel", "noreferrer");
                                } else if (href.startsWith("/")) {
                                    href = baseUrl + href;
                                    a.attr("href", href);
                                    a.attr("rel", "noreferrer");
                                } else {
                                    href = newUrl + "/" + href;
                                    a.attr("href", href);
                                    a.attr("rel", "noreferrer");
                                }
                            }
                        }
                    }
                    Elements imgs = element.select("img");
                    if (imgs.size() > 0) {
                        // 解析图片
                        for (Element img : imgs) {
                            String src = img.attr("src");
                            if (src.contains("data:image")) {
                                try {
                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                                    String dateString = formatter.format(new Date());
                                    String path = imgPath + "/" + dateString + "/" + branchNew.getDate() + "/" + sourceNum;
                                    String uuid = UUID.randomUUID().toString();
                                    String fileName = uuid + ".jpg";
                                    String newLink = "http://www.bidizhaobiao.com/file/" + dateString + "/" + branchNew.getDate()
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
                                    src = newUrl + "/" + src.substring(src.lastIndexOf("./") + 1, src.length());
                                    img.attr("src", src);
                                } else if (src.startsWith("./")) {
                                    src = url.substring(0, url.lastIndexOf("/")) + src.replace("./", "/");
                                    img.attr("src", src);
                                } else if (src.startsWith("/")) {
                                    src = baseUrl + src;
                                    img.attr("src", src);
                                } else {
                                    src = newUrl + "/" + src;
                                    img.attr("src", src);
                                }
                            }
                        }
                    }
                    content = "<div>" + title + "</div>" + element.outerHtml();
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

    public String getContent(String path) {
        String result = "";
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        try {
            client = getHttpClient(true, true);
            HttpGet httpGet = new HttpGet(path);
            httpGet.addHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.107 Safari/537.36");
            httpGet.addHeader("Connection", "close");
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(20 * 1000)
                    .setSocketTimeout(30 * 1000).setRedirectsEnabled(false).build();
            httpGet.setConfig(requestConfig);
            response = client.execute(httpGet);
            response.addHeader("Connection", "close");
            if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 304) {
                result = EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                if (client != null) {
                    client.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public Site getSite() {
        return this.site.setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
    }

    public void startCrawl(int ThreadNum, int crawlType) {
        // 赋值
        serviceContextEvaluation();
        serviceContext.setCrawlType(crawlType);
        // 保存日志
        saveCrawlLog(serviceContext);
        serviceContext.setSaveFileAddSSL(true);
        // 启动爬虫
        spider = Spider.create(this).thread(ThreadNum)
                .setDownloader(new MyDownloader(serviceContext, false, listUrl));
        Request request = new Request("https://www.baidu.com?wd=" + listUrl);
        spider.addRequest(request);
        // 需要SSL证书验证，下载附件
        serviceContext.setSpider(spider);
        spider.run();
        // 爬虫状态监控部分
        saveCrawlResult(serviceContext);
    }


}
