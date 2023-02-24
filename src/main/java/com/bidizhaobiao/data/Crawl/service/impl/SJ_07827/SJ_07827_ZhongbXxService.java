package com.bidizhaobiao.data.Crawl.service.impl.SJ_07827;

import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
import com.bidizhaobiao.data.Crawl.utils.CheckProclamationUtil;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 程序员：许广衡
 * 日期：2022-01-12
 * 原网站：http://mzt.qinghai.gov.cn/announce/
 * 主页：http://mzt.qinghai.gov.cn
 **/
@Service("SJ_07827_ZhongbXxService")
public class SJ_07827_ZhongbXxService extends SpiderService implements PageProcessor {
    public Spider spider = null;

    public String listUrl = "https://www.baidu.com?wd=https://mzt.qinghai.gov.cn/announce/index-htm-page-1.html";
    public String baseUrl = "https://mzt.qinghai.gov.cn";
    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    public Site site = Site.me().setCycleRetryTimes(2).setTimeOut(30000).setSleepTime(20);

    // 网站编号
    public String sourceNum = "07827";
    // 网站名称
    public String sourceName = "青海省民政厅";
    // 信息源
    public String infoSource = "政府采购";
    // 设置地区
    public String area = "西北";
    // 设置省份
    public String province = "青海";
    // 设置城市
    public String city;
    // 设置县
    public String district;
    // 设置CreateBy
    public String createBy = "潘嘉明";

    //private Pattern pattern_page = Pattern.compile("1/(\\d+)");
    //private Pattern pattern_page = Pattern.compile("countPage =(\\d+)");
    private Pattern pattern = Pattern.compile("totalPage = parseInt\\('(.*?)'");
    public Pattern p = Pattern.compile("(?<year>\\d{4})(\\.|年|/|-)(\\d{1,2})(\\.|月|/|-)(\\d{1,2})");
    //是否需要入广联达
    private boolean isNeedSaveFileAddSSL = true;
    //站源类型
    private String taskType;
    //站源名称
    private String taskName;


    public Site getSite() {
        return this.site;
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

    public String getContent(String path) {
        String respStr = "";
        CloseableHttpClient client = getHttpClient(true, false);
        CloseableHttpResponse response = null;
        try {
            HttpGet httpGet = new HttpGet(path);
            httpGet.addHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(10 * 1000).setSocketTimeout(15 * 1000).build();
            httpGet.setConfig(requestConfig);
            response = client.execute(httpGet);
            response.addHeader("Connection", "close");
            if (response.getStatusLine().getStatusCode() == 200) {
                respStr = EntityUtils.toString(response.getEntity(), "UTF-8");
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return respStr;
    }


    @Override
    public void process(Page page) {
        String url = page.getUrl().toString();
        try {
            if (url.contains("wd=")) {
                url = url.substring(url.indexOf("wd=") + 3);
            }
            String html = getContent(url);
            int times = 3;
            while ("".equals(html) && times > 0) {
                html = getContent(url);
                times--;
            }

            Document doc = Jsoup.parse(html);
            Thread.sleep(1000);
            if (url.contains("page-")) {
                if (serviceContext.getPageNum() == 1) {
                    String max = doc.select("cite").text().trim().replaceAll("[\u00a0\u1680\u180e\u2000-\u200a\u2028\u2029\u202f\u205f\u3000\ufeff\\s+]+", "");
                    max = max.substring(max.indexOf("/") + 1, max.lastIndexOf("页"));
                    int maxPage = Integer.parseInt(max);
                    serviceContext.setMaxPage(maxPage);
                }
                Element conTag = doc.select("table[width='98%']").first();
                Elements eachTags = conTag.select("tr:has(a)");
                List<BranchNew> detailList = new ArrayList<>();
                if (eachTags.size() > 0) {
                    for (Element eachTag : eachTags) {
                        if (eachTag.select("a").last() == null) continue;
                        String title = eachTag.select("a").last().attr("title").trim();
                        if (title.length() < 2) {
                            title = eachTag.select("a").last().text().trim();
                        }
                        String date = eachTag.text().trim();
                        date = date.replaceAll("[\\.|/|年|月]", "-");
                        Matcher m = p.matcher(date);
                        if (m.find()) {
                            date = sdf.get().format(sdf.get().parse(m.group()));
                            String yearStr = m.group("year");
                            int year = Integer.parseInt(yearStr);
                            if (year < 2016) {
                                continue;
                            }
                        }
                        if (!CheckProclamationUtil.isProclamationValuable(title)) {
                            continue;
                        }

                        String id = eachTag.select("a").last().attr("href").trim();
                        String link = id;
                        String detailLink = link;
                        link = "https://www.baidu.com?wd=" + link;
                        id = id.substring(id.indexOf(".cn") + 3).trim();
                        BranchNew bn = new BranchNew();
                        bn.setTitle(title);
                        serviceContext.setCurrentRecord(id);
                        bn.setId(id);
                        serviceContext.setCurrentRecord(bn.getId());
                        bn.setDate(date);
                        bn.setLink(link);
                        bn.setDetailLink(detailLink);
                        detailList.add(bn);
                    }
                    // 校验数据List<BranchNew> detailList,int pageNum,String
                    List<BranchNew> needCrawlList = checkData(detailList, serviceContext);
                    for (BranchNew branch : needCrawlList) {
                        map.put(branch.getDetailLink(), branch);
                        page.addTargetRequest(branch.getLink());
                    }
                } else {
                    dealWithNullListPage(serviceContext);
                }

                if (serviceContext.getPageNum() < serviceContext.getMaxPage() && serviceContext.isNeedCrawl()) {
                    serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                    int index = serviceContext.getPageNum();
                    page.addTargetRequest(listUrl.replace("page-1", "page-" + index));
                }
            } else {
                if (page.getStatusCode() == 404) return;
                BranchNew bn = map.get(url);
                if (bn == null) {
                    return;
                }
                String Title = bn.getTitle();
                String recordId = bn.getId();
                serviceContext.setCurrentRecord(recordId);
                String Time = bn.getDate();
                map.remove(url);//清除冗余
                String path1 = bn.getLink();
                path1 = path1.substring(0, path1.lastIndexOf("/"));
                doc.select("input").remove();
                doc.select("meta").remove();
                doc.select("script").remove();
                doc.select("link").remove();
                doc.select("style").remove();
                doc.outputSettings().prettyPrint(true);//允许格式化文档格式
                String content = "";
                String title = "";
                Element conTag = doc.select("div[class=left_box]").first();
                if (conTag != null) {
                    Element titleTag = conTag.select("div[class=t_c px14]").first();
                    conTag.select("div.pos").remove();
                    conTag.select("div[class=info f_gray]").remove();
                    conTag.select("*[style~=^.*display\\s*:\\s*none\\s*(;\\s*[0-9A-Za-z]+|;\\s*)?$]").remove();//删除隐藏格式
                    conTag.select("iframe").remove();
                    Elements as = conTag.select("a");
                    for (Element ae : as) {
                        String href = ae.attr("href");
                        if (!"#".equals(href) && !href.contains("http") && href.length() > 0 && !href.contains("HTTP")) {
                            if (href.indexOf("../../..") == 0) {
                                href = baseUrl + href.replace("../../..", "");
                            } else if (href.indexOf("../") == 0) {
                                href = baseUrl + href.replace("../", "/");
                            } else if (href.indexOf("./") == 0) {
                                href = path1 + href.substring(1);
                            } else if (href.indexOf("/") == 0) {
                                href = baseUrl + href;
                            } else {
                                href = path1 + href;
                            }
                            ae.attr("rel", "noreferrer");
                            ae.attr("href", href);
                        }

                        if (href.contains("mailto:") || href.contains("#")) {
                            ae.remove();
                        }
                    }
                    Elements imgs = conTag.select("img");
                    for (Element imge : imgs) {
                        String src = imge.attr("src");
                        if (!src.contains("http") && !src.contains("HTTP") && !src.startsWith("data")) {
                            if (src.indexOf("../") == 0) {
                                src = baseUrl + src.replace("../", "/");
                            } else if (src.indexOf("./") == 0) {
                                src = path1 + src.substring(1);
                            } else if (src.indexOf("/") == 0) {
                                src = baseUrl + src;
                            } else {
                                src = path1 + src;
                            }
                            imge.attr("rel", "noreferrer");
                            imge.attr("src", src);
                        }

                    }

                    if (titleTag != null) {
                        title = titleTag.text().trim();
                        title = title.replaceAll("[\u00a0\u1680\u180e\u2000-\u200a\u2028\u2029\u202f\u205f\u3000\ufeff\\s+]+", "");
                        title = title.replace("【", "");
                        title = title.replace("】", "");
                        title = title.replace("…", "").replace("...", "");
                    }
                    content = "<div>" + title + "</div>" + conTag.outerHtml();
                } else if (url.contains(".doc") || url.contains(".rar") || url.contains(".pdf") || url.contains(".zip") || url.contains(".xls")) {
                    content = "<div>附件下载：<a href='" + url + "'>" + Title + "</a></div>";
                    title = Title;
                }
                RecordVO recordVo = new RecordVO();
                recordVo.setTitle(title);
                recordVo.setListTitle(Title);
                recordVo.setContent(content.replaceAll("\\u2002", ""));
                serviceContext.setCurrentRecord(recordId);
                recordVo.setId(recordId);
                recordVo.setDate(Time);
                recordVo.setDetailLink(bn.getDetailLink());//详情页链接
                dataStorage(serviceContext, recordVo, bn.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();//输出报错
            dealWithError(url, serviceContext, e);
        }
    }


}
