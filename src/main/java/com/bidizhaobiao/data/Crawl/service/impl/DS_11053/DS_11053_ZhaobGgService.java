package com.bidizhaobiao.data.Crawl.service.impl.DS_11053;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 程序员：何子杰
 * 日期：2019-10-15
 * 原网站：http://jgj.nc.gov.cn/ncsjgswj/tzgg1/nav_list.shtml
 * 主页：
 */

@Service("DS_11053_ZhaobGgService")
public class DS_11053_ZhaobGgService extends SpiderService implements PageProcessor {

    public  Spider spider = null;

    public  String listUrl = "http://jgj.nc.gov.cn/ncsjgswj/tzgg1/nav_list.shtml";

    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(2).setTimeOut(30000).setSleepTime(20).setCharset("UTF-8");
    // 网站编号
    public String sourceNum = "11053";
    // 网站名称
    public String sourceName = "南昌市机关事务管理局";
    // 信息源
    public  String infoSource = "政府采购";

    // 设置地区
    public String area = "华东";
    // 设置省份
    public String province = "江西";
    // 设置城市
    public String city="南昌市";
    // 设置县
    public String district;
    // 设置县
    public String createBy = "何子杰";
    //是否需要入广联达
    public  boolean isNeedInsertGonggxinxi = false;
    //站源类型
    public  String taskType;
    //站源名称
    public  String taskName;

    public double priod = 4;

    // 网站名
    public  Pattern p = Pattern.compile("(\\d{4})(年|/|-)(\\d{1,2})(月|/|-)(\\d{1,2})");
    public  Pattern p_p = Pattern.compile("view\\('(.*?)','(.*?)','(.*?)'\\)");
    public  Pattern p_t = Pattern.compile("countPage=(\\d+)//共多少页");

     public Site getSite() {
            return this.site.setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
        }

    public void startCrawl(int ThreadNum, int crawlType) {
            // 赋值
            serviceContextEvaluation();
            // 保存日志
            saveCrawlLog(serviceContext);
            serviceContext.setCrawlType(crawlType);
            // 启动爬虫
            spider = Spider.create(this).thread(ThreadNum)
                    .setDownloader(new MyDownloader(serviceContext, true, listUrl));
            Request request = new Request(listUrl);
            spider.addRequest(request);
            serviceContext.setSpider(spider);
            spider.run();
            // 爬虫状态监控部分
            saveCrawlResult(serviceContext);
        }

    public void process(Page page) {
        Html html = page.getHtml();
        String url = page.getUrl().toString();
        try {
            // HtmlPage htmlPage = client.getPage(url);
            List<BranchNew> detailList = new ArrayList<BranchNew>();
            Thread.sleep(1000);
            // 判断是否是翻页连接
            if (url.contains("nav_list")) {
                Document document = Jsoup.parse(page.getRawText().replace("&nbsp;", "").replace("&amp;", "&").replace("&ensp;", "").replace("<![CDATA[", "").replace("]]>", "").replace("&lt;", "<").replace("&gt;", ">").replace("&#39;", "'"));
                if (serviceContext.getPageNum() == 1) {
                    //document.select("div.page").last().select("a").last().remove();
//                    Element pageElement = document.select("span.arrow").last().select("a").last();
//                    String pages = pageElement.attr("href").replaceAll("[\u00a0\u1680\u180e\u2000-\u200a\u2028\u2029\u202f\u205f\u3000\ufeff\\s+]", "");
//                    pages = pages.substring(pages.lastIndexOf("_") + 1, pages.lastIndexOf("."));
                    int maxPage = 4;
                    //pages = pages.substring(pages.lastIndexOf("_") + 1, pages.lastIndexOf("."));
                     /*Matcher m = p_t.matcher(pages);
                     if(m.find()){
                       pages = m.group();
                       pages = pages.substring(pages.lastIndexOf("=") + 1, pages.lastIndexOf("\"));
                     }*/

                    serviceContext.setMaxPage(maxPage);
                }
                Elements lis = document.select("ul.item").first().select("li:has(a)");
                //lis.select("li#PageNum").last().remove();
                if (lis.size() > 0) {
                    for (int i=0;i<lis.size();i++) {
                        Element li=lis.get(i);
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
                        title=title.replace("...","");
                        String key = "招标、采购、询价、询比、竞标、竞价、竞谈、竞拍、竞卖、竞买、竞投、竞租、比选、比价、竞争性、谈判、磋商、投标、邀标、议标、议价、单一来源、遴选、标段、明标、明投、出让、转让、拍卖、招租、预审、发包、开标、答疑、补遗、澄清、挂牌";
                        String[] rules = key.split("、");
                        if (!CheckProclamationUtil.isProclamationValuable(title, rules)) {
                             continue;
                        }
                        /*if (!CheckProclamationUtil.isProclamationValuable(title,null)) {
                            continue;
                           }*/
                        /*if (!CheckProclamationUtil.isProclamationValuable(title)) {
                              continue;
                          }*/
                        String href = a.attr("href").trim();;
                        //href=href.substring(href.lastIndexOf("?")+1,href.length());
                        if (href.contains("http")) {
                            continue;
                        }
                        String id = href;
                        String link = "http://jgj.nc.gov.cn" + id;
                        String detailLink = link;
                        String date = li.text().trim().replaceAll("[.|/|年|月]", "-");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Matcher m = p.matcher(date);
                        if(m.find()){
                            date = sdf.format(sdf.parse(m.group()));
                        }
                        dealWithNullTitleOrNullId(serviceContext, title, id);
                        BranchNew branch = new BranchNew();
                        branch.setTitle(title);
                        branch.setId(id);
                        serviceContext.setCurrentRecord(branch.getId());
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
                    page.addTargetRequest(listUrl.replace("nav_list", "nav_list_" + serviceContext.getPageNum()));
                }
            } else {
                // 列表页请求
                String baseUrl = "http://jgj.nc.gov.cn";
                BranchNew branchNew = map.get(url);
                if (branchNew == null) {
                    return;
                }
                String title = branchNew.getTitle();
                String id = branchNew.getId();
                serviceContext.setCurrentRecord(branchNew.getId());
                String date = branchNew.getDate();
                String detailLink = branchNew.getDetailLink();
                String detailTitle = "";
                String content = "";
                String detailContent = html.toString();
                Document document = Jsoup.parse(page.getRawText().replace("&nbsp;", "").replace("&amp;", "&").replace("&ensp;", "").replace("<![CDATA[", "").replace("]]>", "").replace("&lt;", "<").replace("&gt;", ">"));
                if (document.select("div.page").first() == null) {
                    detailTitle = title.trim().replace(" ", "").replace("...", "");
                } else {
                    detailTitle = document.select("div.page").first().text().trim().replace(" ", "").replace("...", "");
                    title=detailTitle;
                }
                Element contentE = document.select("div.content").first();
                if (contentE == null) {
                    return;
                }
                /*if(contentE.text().length()<3){
                    return;
                }*/
                setUrl(contentE, baseUrl, url);
                content = "<div>"+title+"</div>" +contentE.outerHtml();
                detailContent = detailContent != null ? detailContent : Jsoup.parse(content).html();
                RecordVO recordVO = new RecordVO();
                recordVO.setId(id);
                recordVO.setListTitle(title);
                recordVO.setDate(date);
                recordVO.setContent(content);
                recordVO.setTitle(detailTitle);//详情页标题
                recordVO.setDdid(SpecialUtil.stringMd5(detailContent));//详情页md5
                recordVO.setDetailLink(detailLink);//详情页链接
                recordVO.setDetailHtml(detailContent);
                dataStorage(serviceContext, recordVO, branchNew.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();
            dealWithError(url, serviceContext, e);
        }
    }

    public void setUrl(Element conE, String baseUrl, String url) {
        Element contentE = conE;
        contentE.removeAttr("style");
        contentE.select("iframe").remove();
        contentE.select("meta").remove();
        contentE.select("link").remove();
        contentE.select("button").remove();
        contentE.select("input").remove();
        contentE.select("*[style~=^.*display\\s*:\\s*none\\s*(;\\s*[0-9A-Za-z]+|;\\s*)?$]").remove();
        contentE.select("script").remove();
        contentE.select("style").remove();
        if (contentE.select("a") != null) {
            Elements as = contentE.select("a");
            for (Element a : as) {
                String href = a.attr("href");
                if (!href.contains("@") && !"".equals(href) && !href.contains("javascript") && !href.contains("http") && !href.contains("#")) {
                    if (href.contains("../")) {
                        href = baseUrl + "/" + href.replace("../", "");
                        a.attr("href", href);
                    } else if (href.startsWith("./")) {
                        href = url.substring(0, url.lastIndexOf("/")) + href.replace("./", "/");
                        a.attr("href", href);
                        a.removeAttr("rel");
                    } else if (href.startsWith("/")) {
                        href = baseUrl + href;
                        a.attr("href", href);
                        a.removeAttr("rel");
                    } else {
                        href = baseUrl + "/" + href;
                        a.attr("href", href);
                        a.removeAttr("rel");
                    }
                }else if(href.contains("#") && a.attr("onclick").length()>3){
                    String ur=a.attr("onclick");
                    ur=ur.substring(ur.lastIndexOf("http"),ur.lastIndexOf("'"));
                    a.removeAttr("onclick");
                    a.attr("href", ur);
                }
            }
        }
        if (contentE.select("img").first() != null) {
            Elements imgs = contentE.select("img");
            for (Element img : imgs) {
                String src = img.attr("src");
                if (!src.contains("javascript") && !"".equals(src) && !src.contains("http")) {
                    if (src.contains("../")) {
                        src = baseUrl + "/" + src.replace("../", "");
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
    }


}
