package com.bidizhaobiao.data.Crawl.service.impl.DS_19289;

import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
import com.bidizhaobiao.data.Crawl.utils.CheckProclamationUtil;
import com.bidizhaobiao.data.Crawl.utils.SpecialUtil;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 程序员：郭建婷  日期：2021-10-29
 * 原网站：http://www.aqzy.gov.cn/content/channel/529fe19e259534f80e000004/
 * 主页：http://www.aqzy.gov.cn
 **/

@Service
public class DS_19289_ZhaobGgService extends SpiderService implements PageProcessor {
    public Spider spider = null;
    public String listUrl = "http://www.aqzy.gov.cn/content/channel/529fe19e259534f80e000004/page-1/";
    public String homeUrl = "http://www.aqzy.gov.cn";
    public Pattern datePat = Pattern.compile("(\\d{4})(年|/|-|\\.)(\\d{1,2})(月|/|-|\\.)(\\d{1,2})");
    // 网站编号
    public String sourceNum = "19289";
    // 网站名称
    public String sourceName = "安庆市中级人民法院";
    // 信息源
    public String infoSource = "政府采购";
    // 设置地区
    public String area = "华东";
    // 设置省份
    public String province = "安徽";
    // 设置城市
    public String city = "安庆市";
    // 设置县
    public String district ;
    // 设置CreateBy
    public String createBy = "郭建婷";
    public String nextHref = "";
    //附件
    Site site = Site.me().setCycleRetryTimes(2).setTimeOut(30000).setSleepTime(20).setCharset("UTF-8");

      public Site getSite() {
        return this.site.setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
      }

      @Override
      public void startCrawl(int threadNum, int crawlType) {
              // 赋值
              serviceContextEvaluation();
              // 保存日志
              serviceContext.setCrawlType(crawlType);
              saveCrawlLog(serviceContext);
              // 启动爬虫
              spider = Spider.create(this).thread(threadNum)
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
            if (url.contains("/page-")) {
                Document doc = Jsoup.parse(page.getRawText());
                Elements listElement = doc.select("ul.is-listnews>li:has(a)");
                if (listElement.size() > 0) {
                    for (Element element : listElement) {
                        Element a = element.select("a").first();
                        String link = a.attr("href").trim();
                        String id = link.substring(link.lastIndexOf("/") + 1);
                        if (link.startsWith("/")) {
                            link = homeUrl + link;
                        } else if (link.contains("http") && !link.contains(homeUrl.replaceAll("https|http", ""))) {
                            continue;
                        } else if (link.contains("./")) {
                            link = homeUrl + link.substring(link.lastIndexOf("./") + 1);
                        } else if (!link.contains("http") && !link.startsWith("/")) {
                            link = homeUrl + "/" + link;
                        }
                        String detailLink = link;
                        String date = "";
                        Matcher dateMat = datePat.matcher(element.select("span").text());
                        if (dateMat.find()) {
                            date = dateMat.group(1);
                            date += dateMat.group(3).length() == 2 ? "-" + dateMat.group(3) : "-0" + dateMat.group(3);
                            date += dateMat.group(5).length() == 2 ? "-" + dateMat.group(5) : "-0" + dateMat.group(5);
                        }
                        String title = a.attr("title").trim();
                        if (title.length() < 2) title = a.text().trim();
                        String key = "报名、委托、交易、机构、需求、废旧、废置、处置、报废、供应商、承销商、服务商、调研、择选、择优、选取、优选、公选、选定、摇选、摇号、摇珠、抽选、定选、定点、招标、采购、询价、询标、询比、竞标、竞价、竞谈、竞拍、竞卖、竞买、竞投、竞租、比选、比价、竞争性、谈判、磋商、投标、邀标、议标、议价、单一来源、标段、明标、明投、出让、转让、拍卖、招租、出租、预审、发包、承包、分包、外包、开标、遴选、答疑、补遗、澄清、延期、挂牌、变更、预公告、监理、改造工程、报价、小额、零星、自采、商谈";
                        String[] rules = key.split("、");
                        if (!title.contains("...") && !CheckProclamationUtil.isProclamationValuable(title, rules)) {
                             continue;
                        }
                        BranchNew branch = new BranchNew();
                        branch.setId(id);
                        serviceContext.setCurrentRecord(branch.getId());
                        branch.setLink(link);
                        branch.setDetailLink(detailLink);
                        branch.setDate(date);
                        branch.setTitle(title);
                        detailList.add(branch);
                    }
                    List<BranchNew> branchNewList = checkData(detailList, serviceContext);
                    for (BranchNew branch : branchNewList) {
                        map.put(branch.getLink(), branch);
                        page.addTargetRequest(new Request(branch.getLink()));
                    }
                } else {
                    dealWithNullListPage(serviceContext);
                }

               if (serviceContext.getPageNum()==1){
                   Element pageEle = doc.select("span.currentpage").first();
                   if (pageEle!=null){
                       Matcher pageMat = Pattern.compile("1 / (\\d+)").matcher(page.getRawText());
                       if (pageMat.find()){
                           int maxPage = Integer.parseInt(pageMat.group(1));
                           serviceContext.setMaxPage(maxPage);
                       }
                   }

                }
                if (serviceContext.getPageNum()<serviceContext.getMaxPage() && serviceContext.isNeedCrawl()) {
                    serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                    page.addTargetRequest(listUrl.replace("page-1", "page-" + serviceContext.getPageNum()));
                }

            } else {
                BranchNew branch = map.get(url);
                if (branch != null) {
                    map.remove(url);
                    serviceContext.setCurrentRecord(branch.getId());
                    String detailHtml = page.getRawText();
                    Document doc = Jsoup.parse(detailHtml);
                    String title = branch.getTitle().trim().replace("...", "");
                    String date = branch.getDate();
                    String content = "";
                    Element contentElement = doc.select("div.is-newscontnet").first();
                    if (contentElement != null) {
                        saveFile(contentElement,url,date);
                        Element titleElement = doc.select("div.is-newstitle").first();
                        if (titleElement != null) {
                            title = titleElement.text().trim().replace("...", "");
                        }
                        String key = "报名、委托、交易、机构、需求、废旧、废置、处置、报废、供应商、承销商、服务商、调研、择选、择优、选取、优选、公选、选定、摇选、摇号、摇珠、抽选、定选、定点、招标、采购、询价、询标、询比、竞标、竞价、竞谈、竞拍、竞卖、竞买、竞投、竞租、比选、比价、竞争性、谈判、磋商、投标、邀标、议标、议价、单一来源、标段、明标、明投、出让、转让、拍卖、招租、出租、预审、发包、承包、分包、外包、开标、遴选、答疑、补遗、澄清、延期、挂牌、变更、预公告、监理、改造工程、报价、小额、零星、自采、商谈";
                        String[] rules = key.split("、");
                        if (!CheckProclamationUtil.isProclamationValuable(title, rules)) {
                             return;
                        }
                        contentElement.select("div.is-tipsr").remove();
                        contentElement.select("script").remove();
                        contentElement.select("style").remove();
                        contentElement.select("*[style~=^.*display\\s*:\\s*none\\s*(;\\s*[0-9A-Za-z]+|;\\s*)?$]").remove();
                        content = "<div>" + title + "</div>" + contentElement.outerHtml();
                    } else if (url.contains(".doc") || url.contains(".pdf") || url.contains(".zip") || url.contains(".xls") || url.contains(".xlsx")) {
                        content = "<div>附件下载：<a href='" + url + "'>" + branch.getTitle() + "</a></div>";
                        detailHtml = Jsoup.parse(content).toString();
                    }
                    content = content.replaceAll("\\ufeff|\\u2002|\\u200b|\\u2003", "");
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
            dealWithError(url, serviceContext, e);
        }
    }

    public void saveFile(Element contentElement,String url,String date) {
        Elements iframes = contentElement.select("iframe");
        for (Element iframe : iframes) {
            String src= iframe.attr("src").trim();
            String a = "<a href=\""+src+"\">"+"详见附件</a>";
            iframe.after(a);
            iframe.remove();
        }
        Elements aList = contentElement.select("a");
        for (Element a : aList) {
            String href = a.attr("href");
            a.attr("rel", "noreferrer");
            if (href.startsWith("mailto")) {
                continue;
            }
            if (a.hasAttr("data-download")) {
                href = a.attr("data-download");
                a.attr("href", href);
                a.removeAttr("data-download");
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
                    href = homeUrl.substring(0, homeUrl.indexOf(":") + 1) + href;
                    a.attr("href", href);
                } else if (href.indexOf("../") == 0) {
                    href = href.replace("../", "");
                    href = homeUrl + "/" + href;
                    a.attr("href", href);
                } else if (href.startsWith("/")) {
                    href = homeUrl + href;
                    a.attr("href", href);
                } else if (href.indexOf("./") == 0) {
                    href = url.substring(0, url.lastIndexOf("/")) + href.substring(href.lastIndexOf("./") + 1);
                    a.attr("href", href);
                } else {
                    href = url.substring(0, url.lastIndexOf("/") + 1) + href;
                    a.attr("href", href);
                }
            }
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
                    src = homeUrl + "/" + src;
                    img.attr("src", src);
                } else if (src.indexOf("./") == 0) {
                    src = url.substring(0, url.lastIndexOf("/")) + src.substring(src.lastIndexOf("./") + 1);
                    img.attr("src", src);
                } else if (src.startsWith("//")) {
                    src = homeUrl.substring(0, homeUrl.indexOf(":") + 1) + src;
                    img.attr("src", src);
                } else if (src.indexOf("/") == 0) {
                    src = homeUrl + src;
                    img.attr("src", src);
                } else {
                    src = url.substring(0, url.lastIndexOf("/") + 1) + src;
                    img.attr("src", src);
                }
            }
        }
    }


}
