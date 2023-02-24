package com.bidizhaobiao.data.Crawl.service.impl.DX000307;

import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 作者: 唐家逸
 * @version 创建时间：2018年8月9日 下午2:05:15 类说明
 */
@Service
public class   DX000307_1_ZhaobGgService extends SpiderService implements PageProcessor {
	public Spider spider = null;
	public int successNum = 0;
	public String listUrl = "http://oa.qxtt.com:8002/zbgg.aspx";
	public String baseUrl = "http://oa.qxtt.com:8002/";
	// 入库状态
	public int servicesStatus = 0;
	// 设置县
	public String __VIEWSTATE = "";
	public String __EVENTVALIDATION = "";
	public String __EVENTTARGET = "GridView1";
	// 抓取网站的相关配置，包括：编码、抓取间隔、重试次数
	Site site = Site.me().setCycleRetryTimes(2).setTimeOut(30000).setSleepTime(20);
	// 网站编号
	public String sourceNum = "DX000307-1";
	// 网站名称
	public String sourceName = "山东齐星铁塔科技股份有限公司招投标平台";
	// 过时时间分割点
	public String SplitPointStr = "2016-01-01";
	// 信息源
	public String infoSource = "企业采购";
	// 设置地区
	public String area = "华东";
	// 设置省份
	public String province = "山东";
	// 设置城市
	public String city;
	// 设置县
	public String district;
	public String createBy = "唐家逸";
	public double priod = 4;

	public Site getSite() {
		return this.site;
	}

	public void startCrawl(int ThreadNum, int crawlType) {
		// 赋值
		serviceContextEvaluation();
		serviceContext.setCrawlType(crawlType);
		saveCrawlLog(serviceContext);
		// 保存日志
		serviceContext.setMaxPage(106);

		// 启动爬虫
		spider = Spider.create(this).thread(ThreadNum)
				.setDownloader(new MyDownloader(serviceContext, false, listUrl));
		spider.addRequest(new Request(listUrl));
		serviceContext.setSpider(spider);
		spider.run();
		serviceContext.setSpider(spider);
		// 爬虫状态监控部分
		saveCrawlResult(serviceContext);
	}

    public Pattern p = Pattern.compile("(20\\d{2})(年|/|-|\\.)(\\d{1,2})(月|/|-|\\.)(\\d{1,2})");


    public void process(Page page) {
        String url = page.getUrl().toString();
        try {
            List<BranchNew> detailList = new ArrayList<BranchNew>();
            Thread.sleep(500);
            if (url.equals(listUrl)) {
                Document document = Jsoup.parse(page.getRawText());
                Elements list = document.select("#GridView1 tr:has(a)");

                if (list.size() > 0) {
                    for (Element li : list) {

                        Element a = li.select("a").first();
                        String href = a.attr("href").trim();
                        if (href.contains("javascript"))continue;
                        String id = href.substring(href.lastIndexOf("/") + 1);
                        String link = "http://oa.qxtt.com:8002/"+href;

                        Matcher m = p.matcher(li.text());
                        String date = "";
                        if (m.find()) {
                            String month = m.group(3).length() == 2 ? m.group(3) : "0" + m.group(3);
                            String day = m.group(5).length() == 2 ? m.group(5) : "0" + m.group(5);
                            date = m.group(1) + "-" + month + "-" + day;
                        }

                        String title = a.attr("title");
                        if (title == null || title.length() < 2) {
                            title = a.text().replaceAll("\\s*", "").trim();
                        }
                        //分解招标规则
                        BranchNew branch = new BranchNew();
                        branch.setId(id);
                        branch.setLink(link);
                        serviceContext.setCurrentRecord(branch.getId());
                        branch.setTitle(title);
                        branch.setDate(date);
                        detailList.add(branch);
                    }
                    // 校验数据,判断是否需要继续触发爬虫
                    List<BranchNew> needCrawlList = checkData(detailList, serviceContext);
                    for (BranchNew branch : needCrawlList) {
                        map.put(branch.getLink(), branch);
                        //访问详情页
                        page.addTargetRequest(branch.getLink());
                    }
                } else {
                    dealWithNullListPage(serviceContext);
                }
                //下一页

            } else {
                String detailHtml = page.getHtml().toString();
                String Content = "";
                BranchNew bn = map.get(url);
                if (bn != null) {
                    serviceContext.setCurrentRecord(bn.getId());
                    String Title = bn.getTitle();
                    String date = bn.getDate();
                    Document doc = Jsoup.parse(page.getRawText());
                    Element basecontent = doc.select("table.style14").first();
                    if (basecontent != null) {
                        Title = doc.select("td.style20").first().text().trim();

                        if (date.equals("")  || date == null) {
                            Matcher m = p.matcher(basecontent.outerHtml());
                            if (m.find()) {
                                String month = m.group(3).length() == 2 ? m.group(3) : "0" + m.group(3);
                                String day = m.group(5).length() == 2 ? m.group(5) : "0" + m.group(5);
                                date = m.group(1) + "-" + month + "-" + day;
                            } else {
                                date = SpecialUtil.date2Str(new Date());
                            }
                        }
                        Elements aList = basecontent.select("a");
                        for (Element a : aList) {

                            String href = a.attr("href");
                            if (href.startsWith("mailto")) {
                                continue;
                            }
                            if (href.startsWith("javascript")) {
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

                        Elements imgList = basecontent.select("IMG");
                        for (Element img : imgList) {
                            String href = img.attr("src");
                            if (href.contains("icon_pdf")) {
                                img.remove();
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
                        //详情内容
                        //删除部分
                        basecontent.select("script").remove();

                        Content = Title + "<br>" + basecontent.outerHtml().replaceAll("\\ufeff|\\u2002|\\u200b|\\u2003", "");
                    } else if (url.contains(".doc") || url.contains(".pdf") || url.contains(".zip")
                            || url.contains(".xls")) {
                        Content = "<div>附件下载：<a href='" + url + "'>" + Title + "</a></div>";
                        detailHtml = Jsoup.parse(Content).toString();
                        date = SpecialUtil.date2Str(new Date());
                    } else if (url.contains(".jpg") || url.contains(".jpeg") || url.contains(".png")) {
                        Content = "<div>查看图片：<img src='" + url + "'></img></div>";
                        detailHtml = Jsoup.parse(Content).toString();
                        date = SpecialUtil.date2Str(new Date());
                    }

                    RecordVO recordVO = new RecordVO();
                    recordVO.setId(bn.getId());
                    recordVO.setListTitle(bn.getTitle());
                    recordVO.setTitle(Title);
                    recordVO.setDetailLink(url);
                    recordVO.setDetailHtml(detailHtml);
                    recordVO.setDdid(SpecialUtil.stringMd5(detailHtml));
                    recordVO.setDate(date);
                    recordVO.setContent(Content);
                    //入库
                    dataStorage(serviceContext, recordVO, bn.getType());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            dealWithError(url, serviceContext, e);
        }
    }




}
