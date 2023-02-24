package com.bidizhaobiao.data.Crawl.service.impl.QX_05614;

import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
import com.bidizhaobiao.data.Crawl.utils.CheckProclamationUtil;
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
import java.util.List;
import java.util.regex.Pattern;

/**
 * 程序员：伍中林  日期：2022-01-04
 * 原网站：http://www.myx.gov.cn/list.html?channel=district&pages=30
 * 主页：http://www.myx.gov.cn
 **/

@Service("QX_05614_1_ZhaobGgService")
public class QX_05614_1_ZhaobGgService extends SpiderService implements PageProcessor {

    public Spider spider = null;

    public String listUrl = "http://api.myx.gov.cn/news?category=30&page=0&limit=15";

    public String baseUrl = "http://www.myx.gov.cn";
    // 网站编号
    public String sourceNum = "05614-1";
    // 网站名称
    public String sourceName = "墨玉县人民政府";
    // 信息源
    public String infoSource = "政府采购";
    // 设置地区
    public String area = "西北";
    // 设置省份
    public String province = "新疆";
    // 设置城市
    public String city = "和田地区";
    // 设置县
    public String district = "墨玉县";
    // 设置CreateBy
    public String createBy = "伍中林";
    public Pattern p = Pattern.compile("(\\d{4})(年|/|-)(\\d{1,2})(月|/|-)(\\d{1,2})");
    //附件
    // 
    public Pattern p_p = Pattern.compile("view\\('(.*?)','(.*?)','(.*?)'\\)");
    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(3).setTimeOut(30000).setSleepTime(20);

    public Site getSite() {
        return this.site.addHeader("Host", "api.myx.gov.cn").setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
    }

    public void startCrawl(int ThreadNum, int crawlType) {
        // 赋值
        serviceContextEvaluation();
        serviceContext.setCrawlType(crawlType);
        // 保存日志
        saveCrawlLog(serviceContext);
        // 启动爬虫
        spider = Spider.create(this).thread(ThreadNum)
                .setDownloader(new MyDownloader(serviceContext, true, listUrl));
        Request request = new Request(listUrl);
        spider.addRequest(request.addHeader("Host", "api.myx.gov.cn"));
        serviceContext.setSpider(spider);
        spider.run();
        // 爬虫状态监控部分
        saveCrawlResult(serviceContext);
    }

    public void process(Page page) {
        String url = page.getUrl().toString();
        try {
            List<BranchNew> detailList = new ArrayList<BranchNew>();
            Thread.sleep(2000);
            // 判断是否是翻页连接
            if (url.contains("page")) {
                JSONObject jsonObject = new JSONObject(page.getRawText());
                jsonObject = jsonObject.getJSONObject("data");
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                Elements lis = null;
                if (jsonArray != null) {
                    // lis = jsonArray.select("div#listCentents.list-contents").first().select("a");
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            //Element li = lis.get(i);
                            // Element a = lis.get(i);//li.select("a").first();
                            JSONObject li = jsonArray.getJSONObject(i);
                            String title = li.getString("title");
                           /* if (a.hasAttr("title")) {
                                title = a.attr("title").trim();
                            } else {
                                title = a.text().trim();
                            }*/
                            if (!title.contains("...") && !CheckProclamationUtil.isProclamationValuable(title, null)) {
                                continue;
                            }
                            String date = li.getString("publish_time");
                            date = date.substring(0, date.lastIndexOf(" "));
                            String href = li.getString("id").trim();
                            String link = "";
                            //http://www.myx.gov.cn/detail.html?did=246
                            if (href.contains("http")) {
                                link = href;
                                href = href.substring(href.indexOf("?") + 1, href.length());
                            } else {
                                if (href.contains("./")) {
                                    href = href.substring(href.lastIndexOf("./") + 1, href.length());
                                }
                                //http://api.myx.gov.cn/news/detail/245
                                link = "http://api.myx.gov.cn/news/detail/" + href;
                            }
                            if ("".equals(href)) {
                                href = link.substring(link.lastIndexOf("/"));
                            }
                            String id = href;
                            String detailLink = baseUrl + "/detail.html?did=" + href;

                            dealWithNullTitleOrNullId(serviceContext, title, id);
                            BranchNew branch = new BranchNew();
                            branch.setTitle(title);
                            branch.setId(id);
                            branch.setDate(date);
                            serviceContext.setCurrentRecord(id);
                            branch.setDetailLink(detailLink);
                            branch.setLink(link);
                            // branch.setDate(date);
                            detailList.add(branch);
                        }
                        // 校验数据,判断是否需要继续触发爬虫
                        List<BranchNew> needCrawlList = checkData(detailList, serviceContext);
                        for (BranchNew branchNew : needCrawlList) {
                            map.put(branchNew.getLink(), branchNew);
                            page.addTargetRequest(branchNew.getLink());
                        }
                    }
                } else {
                    dealWithNullListPage(serviceContext);
                }
            } else {
                // 列表页请求
                BranchNew branchNew = map.get(url);
                if (branchNew == null) {
                    return;
                }
                String homeUrl = baseUrl;
                String title = Jsoup.parse(branchNew.getTitle()).text();
                String id = branchNew.getId();
                serviceContext.setCurrentRecord(id);
                String date = branchNew.getDate();
                String detailLink = branchNew.getDetailLink();
                String content = "";
                String detailContent = page.getRawText();
                JSONObject object = new JSONObject(page.getRawText());
                object = object.getJSONObject("data");
                Document document = Jsoup.parse(object.getString("note").replace("&nbsp;", "").replace("&amp;", "&").replace("&ensp;", "").replace("<![CDATA[", "").replace("]]>", "").replace("&lt;", "<").replace("&gt;", ">"));
                if (title.contains("...")) {
                    title = object.get("title").toString().trim();
                    if (title.contains("...")) {
                        title = title.replace("...", "");
                    }
                    if (!CheckProclamationUtil.isProclamationValuable(title, null)) {
                        return;
                    }
                }
                Element contentE = null;
                if (document != null) {
                    contentE = document;
                }
                contentE.select("*[style~=^.*display\\s*:\\s*none\\s*(;\\s*[0-9A-Za-z]+|;\\s*)?$]").remove();
                contentE.select("script").remove();
                contentE.select("style").remove();

                //contentE.select("iframe").remove();
                if (contentE.select("a") != null) {
                    Elements as = contentE.select("a");
                    for (Element a : as) {
                        String href = a.attr("href");
                        a.attr("rel", "noreferrer");
                        if (href.contains("C:")) {
                            a.remove();
                        }
                        if (!href.contains("@") && !"".equals(href) && !href.contains("javascript") && !href.contains("http") && !href.contains("#")) {
                            if (href.contains("./")) {
                                href = homeUrl + "/" + href.substring(href.lastIndexOf("./") + 1, href.length());
                                a.attr("href", href);
                            } else if (href.startsWith("/")) {
                                href = homeUrl + href;
                                a.attr("href", href);
                            } else {
                                href = homeUrl + "/" + href;
                                a.attr("href", href);
                            }
                        }
                        if (as.attr("onclick").length() > 4) {
                            String hrefs = as.attr("onclick").trim();
                            hrefs = hrefs.substring(hrefs.indexOf("'") + 1, hrefs.lastIndexOf("'"));
                            hrefs = homeUrl + hrefs;
                            a.removeAttr("onclick");
                            a.attr("href", hrefs);
                        }
                    }
                }
                if (contentE.select("img").first() != null) {
                    Elements imgs = contentE.select("img");
                    for (Element img : imgs) {
                        String src = img.attr("src");
                        img.attr("rel", "noreferrer");
                        if (src.contains("C:")) {
                            img.remove();
                        }
                        if (!src.contains("javascript") && !"".equals(src) && !src.contains("http")) {
                            if (src.contains("./")) {
                                src = homeUrl + "/" + src.substring(src.lastIndexOf("./") + 1, src.length());
                                img.attr("src", src);
                            } else if (src.startsWith("/")) {
                                src = homeUrl + src;
                                img.attr("src", src);
                            } else {
                                src = homeUrl + "/" + src;
                                img.attr("src", src);
                            }
                        }
                    }
                }
                if (contentE.select("a[href*=javascript]").first() != null) {
                    Elements as = contentE.select("a[href*=javascript]");
                    for (Element a : as) {
                        a.removeAttr("href");
                    }
                }
                if (contentE.select("a[href*=#]").first() != null) {
                    Elements as = contentE.select("a[href*=#]");
                    for (Element a : as) {
                        a.removeAttr("href");
                    }
                }
                content = "<div>" + title + "</div><br>" + contentE.outerHtml();
                detailContent = detailContent != null ? detailContent : Jsoup.parse(content).html();
                RecordVO recordVO = new RecordVO();
                recordVO.setId(id);
                recordVO.setListTitle(title);
                recordVO.setDate(date);
                recordVO.setContent(content.replaceAll("\\u2002", ""));
                recordVO.setTitle(title);//详情页标题
                //recordVO.setDdid(SpecialUtil.stringMd5(detailContent));//详情页md5
                recordVO.setDetailLink(detailLink);//详情页链接
                recordVO.setDetailHtml(detailContent);
                dataStorage(serviceContext, recordVO, branchNew.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
            dealWithError(url, serviceContext, e);
        }
    }


}
