package com.bidizhaobiao.data.Crawl.service.impl.SJ_13771;

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
 * 程序员：唐家逸 日期：2020-09-01 原网站：http://jalhf.com/zbgg 主页：http://jalhf.com/
 **/

@Service
public class   SJ_13771_GonggBgService extends SpiderService implements PageProcessor {

	public Spider spider = null;

	public String listUrl = "http://jalhf.com/zbby";
	public String baseUrl = "http://jalhf.com";
	// 网站编号
	public String sourceNum = "13771";
	// 网站名称
	public String sourceName = "吉安隆海锋建设咨询有限公司";
	// 信息源
	public String infoSource = "政府采购";
	// 设置地区
	public String area = "华东";
	// 设置省份
	public String province = "江西";
	// 设置城市
	public String city;
	// 设置县
	public String district;
	public String createBy = "唐家逸";
	public Pattern p = Pattern.compile("(\\d{4})(年|/|-|\\.)(\\d{1,2})(月|/|-|\\.)(\\d{1,2})");
	// 是否需要入广联达
	public boolean isNeedInsertGonggxinxi = false;
	// 站源类型
	public String taskType = "";
	// 站源名称
	public String taskName = "";
	// 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
	Site site = Site.me().setCycleRetryTimes(2).setTimeOut(30000).setSleepTime(20);

	// 信息源
	public Site getSite() {
		return this.site.setUserAgent(
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36");
	}

	public void startCrawl(int ThreadNum, int crawlType) {
		// 赋值
		serviceContextEvaluation();
		// 保存日志
		serviceContext.setCrawlType(crawlType);
		saveCrawlLog(serviceContext);
		// 启动爬虫
		spider = Spider.create(this).thread(ThreadNum).setDownloader(new MyDownloader(serviceContext, true, listUrl));
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
			if (url.equals(listUrl)) {
				Document doc = Jsoup.parse(page.getRawText());
				Element tab = doc.select("ul[class=w-list xn-resize]").first();
				if (tab != null) {
					Elements list = tab.select("li[class=w-list-item]:has(a)");
					if (list.size() > 0) {
						for (int i = 0; i < list.size(); i++) {
							Element li = list.get(i);
							Element a = li.select("a[href]").first();
							String id = a.attr("href").trim();
							if (id.startsWith("http") && !id.contains(baseUrl.replaceAll("https||http", ""))) {
								continue;
							}
							String link = baseUrl + id;
							// id = id.substring(id.lastIndexOf("/") + 1);
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
							BranchNew branch = new BranchNew();
							branch.setId(id);
                            serviceContext.setCurrentRecord(branch.getId());
							branch.setLink(link);
							branch.setTitle(title);
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
				} else {
					dealWithNullListPage(serviceContext);
				}
			} else {
				String detailHtml = page.getHtml().toString();
				String Content = "";
				 BranchNew bn = map.get(url);
				serviceContext.setCurrentRecord(bn.getId());
				if (bn != null) {
					String Title = bn.getTitle();
					String date = bn.getDate();
					Document doc = Jsoup.parse(page.getRawText());
					Elements aList = doc.select("a");
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
					Elements imgList = doc.select("IMG");
					for (Element img : imgList) {
						String href = img.attr("src");
						if (href.contains("/qrcode/")) {
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
					Element div = doc.select("div[class=w-detailcontent]").first();
					if (div != null) {
						div.select("script").remove();
						div.select("p:contains(来源：)").remove();
						Content += div.html().replace("? ?", "");
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
					dataStorage(serviceContext, recordVO, bn.getType());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			dealWithError(url, serviceContext, e);
		}
	}
}
