package com.bidizhaobiao.data.Crawl.service.impl.QX_10912;

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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 程序员：白嘉全  日期：2022-02-09
 * 原网站：http://www.zqdzfy.gov.cn/Item/list.asp?id=120
 * 主页：http://www.zqdzfy.gov.cn
 **/

@Service("QX_10912_PaimCrService")
public class QX_10912_PaimCrService extends SpiderService implements PageProcessor {

    public Spider spider = null;

    public String listUrl = "http://www.zqdzfy.gov.cn/html/ktgg/zxgg/";
    public String baseUrl = "http://www.zqdzfy.gov.cn";

    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    public Site site = Site.me().setCycleRetryTimes(2).setTimeOut(30000).setSleepTime(20);
    // 网站编号
    public String sourceNum = "10912";
    // 网站名称
    public String sourceName = "肇庆市端州区人民法院";
    // 信息源
    public String infoSource = "政府采购";
    // 设置地区
    public String area = "华南";
    // 设置省份
    public String province = "广东";
    // 设置城市
    public String city = "肇庆市";
    // 设置县
    public String district = "端州区";
    // 设置县
    public String createBy = "白嘉全";
    //是否需要入广联达
    public boolean isNeedInsertGonggxinxi = false;
    //站源类型
    public String taskType;
    //站源名称
    public String taskName;

    public Pattern p = Pattern.compile("(\\d{4})(年|/|-)(\\d{1,2})(月|/|-)(\\d{1,2})");

    public Pattern p_p = Pattern.compile("view\\('(.*?)','(.*?)','(.*?)'\\)");

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
        String url = page.getUrl().toString();
        try {
            Thread.sleep(500);
            if (url.equals(listUrl)) {
                Document doc = Jsoup.parse(page.getRawText());
                List<BranchNew> detailList = new ArrayList<>();
                //System.out.println(page.getHtml().toString());
                Elements eachTags = doc.select("div[class=article_list] > ul > li:has(a[href*=zxgg])");
                if (eachTags != null) {
                    if (eachTags.size() > 0) {
                        for (Element eachTag : eachTags) {
                            String id = eachTag.select("a").first().attr("href");
                            String link = baseUrl + id;
                            String title = eachTag.select("a").first().attr("title").trim();
                            if (title.length() < 2) {
                                title = eachTag.select("a").first().text().trim();
                            }
                            String date = "";
                            date = eachTag.text();
                            date = date.replaceAll("[./年月]", "-");
                            Matcher m = p.matcher(date);
                            if (m.find()) {
                                date = sdf.get().format(sdf.get().parse(m.group()));
                            }


                            BranchNew bn = new BranchNew();
                            bn.setTitle(title);
                            bn.setId(id);
                            serviceContext.setCurrentRecord(bn.getId());
                            bn.setDate(date);
                            bn.setDetailLink(link);
                            bn.setLink(link);
                            detailList.add(bn);
                        }
                        // 校验数据List<BranchNew> detailList,int pageNum,String
                        List<BranchNew> needCrawlList = checkData(detailList, serviceContext);
                        for (BranchNew branch : needCrawlList) {
                            map.put(branch.getLink(), branch);
                            page.addTargetRequest(branch.getLink());
                        }
                    } else {
                        //首页内容为空
                        dealWithNullListPage(serviceContext);
                    }
                } else {
                    dealWithNullListPage(serviceContext);
                }
            } else {
                BranchNew bn = map.get(url);
                if (bn == null) {
                    return;
                }
                Document doc = page.getHtml().getDocument();
                String Title = bn.getTitle();
                String date = bn.getDate();
                String id = bn.getId();
                serviceContext.setCurrentRecord(id);
                String content = "";
                map.remove(url);//清除冗余
                String path = baseUrl;
                String path1 = bn.getLink();
                path1 = path1.substring(0, path1.lastIndexOf("/") + 1);
                doc.select("input").remove();
                doc.select("meta").remove();
                doc.select("script").remove();
                doc.select("link").remove();
                doc.select("style").remove();
                doc.outputSettings().prettyPrint(true);


                Element conTag = doc.select("div#MyContent").first();
                if (conTag != null) {
                    conTag.select("*[style~=^.*display\\s*:\\s*none\\s*(;\\s*[0-9A-Za-z]+|;\\s*)?$]").remove();
                    conTag.select("iframe").remove();
                    Elements as = conTag.select("a");
                    for (Element ae : as) {
                        String href = ae.attr("href");
                        if (!"#".equals(href) && !href.contains("http") && href.length() > 0 && !href.contains("HTTP")) {
                            if (href.indexOf("../") == 0) {
                                href = path + href.replace("../", "");
                            } else if (href.indexOf("./") == 0) {
                                href = path1 + href.substring(2);
                            } else if (href.indexOf("/") == 0) {
                                href = path + href;
                            } else {
                                href = path1 + href;
                            }
                        }
                        ae.attr("rel", "noreferrer");
                        ae.attr("href", href);
                    }
                    Elements imgs = conTag.select("img");
                    for (Element imge : imgs) {
                        String src = imge.attr("src");
                        if (!src.contains("http") && !src.contains("HTTP") && !src.startsWith("data")) {
                            if (src.indexOf("../") == 0) {
                                src = path1 + src.replace("../", "");
                            } else if (src.indexOf("./") == 0) {
                                src = path1 + src.substring(2);
                            } else if (src.indexOf("/") == 0) {
                                src = path + src;
                            } else {
                                src = path1 + src;
                            }
                        }
                        imge.attr("rel", "noreferrer");
                        imge.attr("src", src);
                    }
                    content = conTag.outerHtml().replace("\u2002", "");
                }
                RecordVO recordVo = new RecordVO();
                recordVo.setTitle(Title);
                recordVo.setListTitle(Title);
                recordVo.setContent(content);
                recordVo.setId(bn.getId());
                recordVo.setDate(date);
                recordVo.setDetailLink(bn.getDetailLink());
                // 入库操作（包括数据校验和入库）
                dataStorage(serviceContext, recordVo, bn.getType());
            }
        } catch (Exception e) {
            dealWithError(url, serviceContext, e);
        }
    }


}
