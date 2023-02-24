package com.bidizhaobiao.data.Crawl.service.impl.DX009394;

import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
import com.bidizhaobiao.data.Crawl.utils.SpecialUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 程序员：陈省龙 日期：2022-02-15
 * 原网站：https://www.donghaiair.com/html/passengers-notice/notice-index.html?type=SYS0206&id=36?type=SYS0206&id=36
 * 主页：https://www.donghaiair.com
 **/

@Service("DX009394_ZhaobGgService")
public class DX009394_ZhaobGgService extends SpiderService implements PageProcessor {

    public Spider spider = null;

    public String listUrl = "https://www.baidu.com/?wd=https://b2capi.donghaiair.cn/guide/getWebsiteGuideContent";

    public String baseUrl = "https://www.donghaiair.com";

    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(3).setTimeOut(30000).setSleepTime(20);
    // 网站编号
    public String sourceNum = "DX009394";
    // 网站名称
    public String sourceName = "东海航空有限公司";
    // 信息源
    public String infoSource = "企业采购";
    // 设置地区
    public String area = "华南";
    // 设置省份
    public String province = "广东";
    // 设置城市
    public String city = "深圳市";
    // 设置县
    public String district = "宝安区";
    // 设置CreateBy
    public String createBy = "陈省龙";
    //附件
    public Pattern p = Pattern.compile("(\\d{4})(年|/|-)(\\d{1,2})(月|/|-)(\\d{1,2})");

    public Pattern p_p = Pattern.compile("view\\('(.*?)','(.*?)','(.*?)'\\)");

    public Site getSite() {
        return this.site.setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36");
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

    public void process(Page page) {

        String url = page.getUrl().toString();
        try {
            if (url.contains("wd=")) {
                url = url.substring(url.indexOf("=") + 1);
            }
            String html = getContent(url);
            int times = 2;
            while ("".equals(html) && times > 0) {
                html = getContent(url);
                times--;
            }
            List<BranchNew> detailList = new ArrayList<BranchNew>();
            Thread.sleep(500);
            if (url.contains("getWebsiteGuideContent")) {
                JSONObject object = new JSONObject(html);

                if (object.has("data")) {
                    JSONArray arr = object.getJSONArray("data");

                    if (arr.length() > 0) {
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            String id = obj.get("id").toString();
                            String title = obj.getString("title");
                            long time = obj.getLong("createTime");
                            String date = SpecialUtil.date2Str(new Date(time));
                            date = date.replaceAll("[./年月]", "-");
                            Matcher m = p.matcher(date);
                            if (m.find()) {
                                date = sdf.get().format(sdf.get().parse(m.group()));
                            }
                            String link = "https://www.donghaiair.com/html/passengers-notice/notice-index.html?type=SYS0206&id=36?type=SYS0206&id=36";
                            String detail = obj.getString("content");
                            String content = "<div>" + detail + "</div>";

                            JSONArray guideFileList = obj.getJSONArray("guideFileList");
                            for (int j = 0; j < guideFileList.length(); j++) {
                                JSONObject jsonObject = guideFileList.getJSONObject(j);
                                String name = jsonObject.getString("name");
                                String hrefUrl = jsonObject.getString("url");
                                content += "<div><a href=" + hrefUrl + ">" + name + "</a><div>";
                            }

                            BranchNew bn = new BranchNew();
                            bn.setTitle(title);
                            bn.setId(id);
                            bn.setDate(date);
                            bn.setDetailLink(link);
                            bn.setLink(link);
                            bn.setContent(content);
                            detailList.add(bn);
                        }
                        // 校验数据List<BranchNew> detailList,int pageNum,String
                        List<BranchNew> needCrawlList = checkData(detailList, serviceContext);
                        for (BranchNew branch : needCrawlList) {
                            String content = branch.getContent();
                            if (!content.equals("")) {
                                RecordVO recordVO = new RecordVO();
                                recordVO.setId(branch.getId());
                                recordVO.setListTitle(branch.getTitle());
                                recordVO.setDate(branch.getDate());
                                recordVO.setContent(branch.getContent().replaceAll("\\u2002", ""));
                                recordVO.setTitle(branch.getTitle());//详情页标题
                                recordVO.setDetailLink(branch.getDetailLink());//详情页链接
                                dataStorage(serviceContext, recordVO, branch.getType());
                            } else {
                                map.put(branch.getLink(), branch);
                                page.addTargetRequest(branch.getLink());
                            }
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


                Element conTag = doc.select("div.nei_left_about").first();
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

    public String getContent(String url) {
        String result = "";
        CloseableHttpClient httpClient = getHttpClient(false, false);

        HttpResponse httpResponse;
        HttpPost httpPost = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10 * 1000).setSocketTimeout(60 * 1000)
                .build();
        httpPost.setConfig(requestConfig);
        httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
        try {
            HttpEntity entity = new StringEntity(
                    "{\"id\":36,\"language\":\"zh-CN\"}", "utf-8");
            httpPost.setEntity(entity);
            httpResponse = httpClient.execute(httpPost);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                HttpEntity httpEntity = httpResponse.getEntity();
                InputStream ins = httpEntity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(ins, StandardCharsets.UTF_8));
                StringBuilder stringBuilder = new StringBuilder();
                while ((result = reader.readLine()) != null) {
                    stringBuilder.append(result);
                }
                result = stringBuilder.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
                e.printStackTrace();
            }
        }
        return result;
    }
}
