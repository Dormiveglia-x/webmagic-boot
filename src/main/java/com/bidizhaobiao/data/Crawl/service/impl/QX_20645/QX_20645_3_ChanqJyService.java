package com.bidizhaobiao.data.Crawl.service.impl.QX_20645;

import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
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
 * 程序员：许广衡 日期：2022-02-15
 * 原网站：首页/供应信息/林地林权/仅抓取首页标题
 * 主页：http://www.gtxnjs.com
 **/
@Service
public class QX_20645_3_ChanqJyService extends SpiderService implements PageProcessor {
    public Spider spider = null;

    public String listUrl = "http://www.gtxnjs.com/";
    public String baseUrl = "http://www.gtxnjs.com/";
    public Pattern datePat = Pattern.compile("(\\d{4})(年|/|-|\\.)(\\d{1,2})(月|/|-|\\.)(\\d{1,2})");

    // 网站编号
    public String sourceNum = "20645-3";
    // 网站名称
    public String sourceName = "馆陶县农村产权交易所";
    // 信息源
    public String infoSource = "政府采购";
    // 设置地区
    public String area = "华北";
    // 设置省份
    public String province = "河北";
    // 设置城市
    public String city = "邯郸市";
    // 设置县
    public String district = "馆陶县";
    public String createBy = "许广衡";
    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(2).setTimeOut(30000).setSleepTime(20);

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

    private Pattern pattern = Pattern.compile("totalPage = parseInt\\('(.*?)'");
    private Pattern p = Pattern.compile("20\\d{2}-\\d{1,2}-\\d{1,2}");

    @Override
    public void process(Page page) {
        Document doc = page.getHtml().getDocument();
        String url = page.getUrl().toString();
        try {
            Thread.sleep(1000);
            if (url.equals(listUrl)) {
                Element conTag = doc.select("div#productTextList640").first();
                Elements eachTags = conTag.select("div[class=propDiv productName propAlignLeft]:has(a)");
                List<BranchNew> detailList = new ArrayList<>();
                if (eachTags.size() > 0) {
                    for (Element eachTag : eachTags) {
                        if (eachTag.select("a").first() == null) continue;
                        String title = eachTag.select("a").first().attr("title").trim();
                        if (title.length() < 2) {
                            title = eachTag.select("a").first().text().trim();
                        }
                        String date = eachTag.text().trim();
                        date = date.replaceAll("[./年月]", "-");
                        Matcher m = p.matcher(date);
                        if (m.find()) {
                            date = sdf.get().format(sdf.get().parse(m.group()));
                        } else {
                            date = sdf.get().format(new Date());
                        }
                        String id = eachTag.select("a").first().attr("href").trim();
                        String link = baseUrl + id;
                        BranchNew bn = new BranchNew();
                        bn.setTitle(title);
                        bn.setId(id);
                        serviceContext.setCurrentRecord(id);
                        bn.setDate(date);
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
                    dealWithNullListPage(serviceContext);
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
                String path = baseUrl;
                String path1 = bn.getLink();
                path1 = path1.substring(0, path1.lastIndexOf("/"));
                doc.select("input").remove();
                doc.select("meta").remove();
                doc.select("script").remove();
                doc.select("link").remove();
                doc.select("style").remove();
                doc.outputSettings().prettyPrint(true);//允许格式化文档格式
                String content = "";
                Element conTag = doc.select("div[class=pdLayoutR pdLayoutR_J]").first();
                if (conTag != null) {
                    conTag.select("span[id=productQrCode]").remove();
                    conTag.select("*[style~=^.*display\\s*:\\s*none\\s*(;\\s*[0-9A-Za-z]+|;\\s*)?$]").remove();//删除隐藏格式
                    conTag.select("iframe").remove();
                    Elements as = conTag.select("a");
                    for (Element ae : as) {
                        String href = ae.attr("href");
                        if (href.startsWith("mailto")) {
                            continue;
                        }
                        if (href.startsWith("javascript")) {
                            ae.remove();
                            continue;
                        }
                        if (href.contains("javascript") || href.equals("#")) {
                            if (ae.attr("onclick").contains("window.open('http")) {
                                String onclick = ae.attr("onclick");
                                onclick = onclick.substring(onclick.indexOf("'") + 1, onclick.lastIndexOf("'"));
                                ae.attr("href", onclick);
                                ae.removeAttr("onclick");
                            } else {
                                ae.removeAttr("href");
                                ae.removeAttr("onclick");
                            }
                            continue;
                        }
                        if (!"#".equals(href) && !href.contains("http") && href.length() > 0 && !href.contains("HTTP")) {
                            if (href.indexOf("../../..") == 0) {
                                href = path + href.replace("../../..", "");
                            } else if (href.indexOf("../") == 0) {
                                href = path + href.replace("../", "/");
                            } else if (href.indexOf("./") == 0) {
                                href = path1 + href.substring(1);
                            } else if (href.indexOf("/") == 0) {
                                href = path + href;
                            } else {
                                href = path1 + href;
                            }
                            ae.attr("rel", "noreferrer");
                            ae.attr("href", href);
                        }

                        if (href.contains("#")) {
                            ae.remove();
                        }
                    }
                    Elements imgs = conTag.select("img");
                    for (Element imge : imgs) {
                        String src = imge.attr("src");
                        if (src.contains("file://C:")) {
                            imge.remove();
                            continue;
                        }
                        if (!src.contains("http") && !src.contains("HTTP") && !src.startsWith("data")) {
                            if (src.indexOf("../") == 0) {
                                src = path + src.replace("../", "/");
                            } else if (src.indexOf("./") == 0) {
                                src = path1 + src.substring(1);
                            } else if (src.indexOf("/") == 0) {
                                src = path + src;
                            } else {
                                src = path1 + src;
                            }
                            imge.attr("rel", "noreferrer");
                            imge.attr("src", src);
                        }

                    }

                    content = "<div>" + Title + "</div>" + conTag.outerHtml();
                } else if (url.contains(".doc") || url.contains(".rar") || url.contains(".pdf") || url.contains(".zip") || url.contains(".xls")) {
                    content = "<div>附件下载：<a href='" + url + "'>" + Title + "</a></div>";
                }
                RecordVO recordVo = new RecordVO();
                recordVo.setTitle(Title);
                recordVo.setDetailLink(url);
                recordVo.setListTitle(Title);
                recordVo.setContent(content);
                recordVo.setId(recordId);
                recordVo.setDate(Time);
                //System.out.println(title + content);
                // 入库操作（包括数据校验和入库）
                dataStorage(serviceContext, recordVo, bn.getType());
            }
        } catch (Exception e) {
            e.printStackTrace();//输出报错
            dealWithError(url, serviceContext, e);
        }
    }


}
