package com.bidizhaobiao.data.Crawl.service.impl.DX008116;

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
import org.json.JSONArray;
import org.json.JSONObject;
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
 * 程序员：郭建婷 日期：2022-03-11
 * 原网站：https://www.ciitp.com.cn/newsList?type=4
 * 主页：https://www.ciitp.com.cn
 **/

@Service("DX008116_ZhaobGgService")
public class DX008116_ZhaobGgService extends SpiderService implements PageProcessor {

    public Spider spider = null;
    public String listUrl = "https://www.ciitp.com.cn/newsInfo/getNewsInfoList?page=1&rows=100&classify=4";
    public String baseUrl = "https://www.ciitp.com.cn";
    public String baidu = "https://www.baidu.com/?wd=";
    // 网站编号
    public String sourceNum = "DX008116";
    // 网站名称
    public String sourceName = "中国工信出版传媒集团";
    // 信息源
    public String infoSource = "企业采购";
    // 设置地区
    public String area = "华北";
    // 设置省份
    public String province = "北京";
    // 设置城市
    public String city;
    // 设置县
    public String district;
    // 设置CreateBy
    public String createBy = "郭建婷";
    //附件
    public Pattern p = Pattern.compile("(\\d{4})(年|/|-)(\\d{1,2})(月|/|-)(\\d{1,2})");
    public Pattern p_p = Pattern.compile("view\\('(.*?)','(.*?)','(.*?)'\\)");
    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(3).setTimeOut(30000).setSleepTime(20);

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
        spider = Spider.create(this).thread(ThreadNum)
                .setDownloader(new MyDownloader(serviceContext, false, listUrl));
        spider.addRequest(new Request(baidu + listUrl));
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
            url = url.substring(url.lastIndexOf("wd=") + 3);
            String contentHtml = getContent(url);
            int contentCount = 5;
            while ("".equals(contentHtml) && contentCount > 0) {
                contentHtml = getContent(url);
                contentCount--;
            }
            page.setRawText(contentHtml);
            if (url.contains("page=")) {
                //System.out.println(page.getRawText());
                JSONObject json = new JSONObject(page.getRawText());
                JSONObject data = json.getJSONObject("data");
                if (data.has("total")) {
                    int maxPage = data.getInt("total");
                    if (maxPage % 10 == 0) {
                        maxPage = maxPage / 10;
                    } else {
                        maxPage = (maxPage / 10) + 1;
                    }
                    serviceContext.setMaxPage(maxPage);
                    JSONArray arr = data.getJSONArray("rows");
                    if (arr.length() > 0) {
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            String id = obj.get("staticInfoId").toString();
                            String title = obj.getString("mainTitle");
                            String date = obj.get("newsDate").toString();
                            date = date.replaceAll("[./年月]", "-");
                            Matcher m = p.matcher(date);
                            if (m.find()) {
                                date = sdf.get().format(sdf.get().parse(m.group()));
                            }
                            String link = obj.get("staticInfoUrl").toString();
                            link = baseUrl + link;
                            //分解
                            String key = "询标、交易、机构、需求、废旧、废置、处置、报废、供应商、承销商、服务商、调研、择选、择优、选取、优选、公选、选定、摇选、摇号、摇珠、抽选、定选、定点、招标、采购、询价、询比、竞标、竞价、竞谈、竞拍、竞卖、竞买、竞投、竞租、比选、比价、竞争性、谈判、磋商、投标、邀标、议标、议价、单一来源、标段、明标、明投、出让、转让、拍卖、招租、出租、预审、发包、承包、分包、外包、开标、遴选、答疑、补遗、澄清、延期、挂牌、变更、预公告、监理、改造工程、报价、小额、零星、自采、商谈";
                            String[] keys = key.split("、");
                            if (!CheckProclamationUtil.isProclamationValuable(title, keys)) {
                                continue;
                            }
                            BranchNew branch = new BranchNew();
                            branch.setId(id);
                            serviceContext.setCurrentRecord(branch.getId());
                            branch.setLink(link);
                            branch.setDetailLink(link);
                            branch.setTitle(title);
                            branch.setDate(date);
                            detailList.add(branch);
                        }
                        // 校验数据List<BranchNew> detailList,int pageNum,String
                        List<BranchNew> needCrawlList = checkData(detailList, serviceContext);
                        for (BranchNew branch : needCrawlList) {
                            map.put(branch.getLink(), branch);
                            page.addTargetRequest(baidu + branch.getLink());
                        }
                    } else {
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
                String path1 = listUrl;
                path1 = path1.substring(0, path1.lastIndexOf("/"));
                doc.select("input").remove();
                doc.select("meta").remove();
                doc.select("script").remove();
                doc.select("link").remove();
                doc.select("style").remove();
                doc.outputSettings().prettyPrint(true);


                Element conTag = doc.select("div.article").first();
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
            e.printStackTrace();
            dealWithError(url, serviceContext, e);
        }
    }


    public String getContent(String path) {
        String result = "";
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        try {
            client = getHttpClient(true, false);
            HttpGet httpGet = new HttpGet(path);
            httpGet.addHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
            httpGet.addHeader("Connection", "close");
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(20 * 1000)
                    .setSocketTimeout(30 * 1000).build();
            httpGet.setConfig(requestConfig);
            response = client.execute(httpGet);
            response.addHeader("Connection", "close");
            if (response.getStatusLine().getStatusCode() == 200) {
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

}
