package com.bidizhaobiao.data.Crawl.service.impl.QX_03870;

import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
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
import us.codecraft.webmagic.model.HttpRequestBody;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.utils.HttpConstant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 程序员：杨业深 日期：2022-02-09
 * 原网站：http://ggzyjy.weihai.cn/rushan/jyxx/003001/003001002/transInfo.html
 * 主页：http://ggzyjy.weihai.cn
 **/

@Service
public class QX_03870_1_ZhaobGgService extends SpiderService implements PageProcessor {

    public Spider spider = null;

    public String listUrl = "http://ggzyjy.weihai.cn/EpointWebBuilder/rest/frontAppCustomAction/getPageInfoListNew";
    public String baseUrl = "http://ggzyjy.weihai.cn";
    public Pattern p = Pattern.compile("(\\d{4})(年|/|-|\\.)(\\d{1,2})(月|/|-|\\.)(\\d{1,2})");
    // 网站编号
    public String sourceNum = "03870-1";
    // 网站名称
    public String sourceName = "乳山公共资源交易网";
    // 信息源
    public String infoSource = "政府采购";
    // 设置地区
    public String area = "华东";
    // 设置省份
    public String province = "山东";
    // 设置城市
    public String city = "威海市";
    // 设置县
    public String district = "乳山市";
    public String createBy = "伍中林";
    // 站源类型
    public String taskType = "";
    // 站源名称
    public String taskName = "";
    public String access_token = "";
    // 是否需要入广联达
    public boolean isNeedInsertGonggxinxi = false;
    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数
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
        spider = Spider.create(this).thread(ThreadNum).setDownloader(new MyDownloader(serviceContext, false, listUrl));
        spider.addRequest(getToken());
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
            if (url.equals("http://ggzyjy.weihai.cn/EpointWebBuilder/rest/getOauthInfoAction/getNoUserAccessToken")) {
                JSONObject json = new JSONObject(page.getRawText());
                JSONObject custom = json.getJSONObject("custom");
                access_token = custom.get("access_token").toString();
                page.addTargetRequest(getListRequest(0));
            } else if (url.equals(listUrl)) {
                JSONObject json = new JSONObject(page.getRawText());
                //System.out.println(page.getRawText());
                JSONObject custom = json.getJSONObject("custom");

                if (custom.has("count")) {
                    int max = custom.getInt("count");
                    int maxPage = max % 12 == 0 ? max / 12 : max / 12 + 1;
                    serviceContext.setMaxPage(maxPage);


                    JSONArray arr = custom.getJSONArray("infodata");
                    if (arr.length() > 0) {
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            String id = obj.get("infourl").toString();
                            String title = obj.getString("title");
                            String date = obj.getString("infodate");
                            String link = "http://ggzyjy.weihai.cn" + id;
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
                            page.addTargetRequest(branch.getLink());
                        }
                    } else {
                        dealWithNullListPage(serviceContext);
                    }
                } else {
                    dealWithNullListPage(serviceContext);
                }
                if (serviceContext.getPageNum() < serviceContext.getMaxPage() && serviceContext.isNeedCrawl()) {
                    page.addTargetRequest(getListRequest(serviceContext.getPageNum()));
                    serviceContext.setPageNum(serviceContext.getPageNum() + 1);
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
                map.remove(url);//清除冗余
                String path = baseUrl;
                String path1 = listUrl;
                path1 = path1.substring(0, path1.lastIndexOf("/"));
                doc.select("input").remove();
                doc.select("meta").remove();
                doc.select("script").remove();
                doc.select("link").remove();
                doc.select("style").remove();


                Element conTag = doc.select("div.div-article2").first();
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

                }
                String content = conTag.outerHtml();
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

    public Request getListRequest(int pageNo) {
        Request request = new Request(listUrl);
        request.setMethod(HttpConstant.Method.POST);
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("params", "{\"siteGuid\":\"caf3f935-94d7-4d37-b361-16db510865c3\",\"categoryNum\":\"003001002\",\"kw\":\"\",\"startDate\":\"\",\"endDate\":\"\",\"pageIndex\":" + pageNo + ",\"pageSize\":12,\"area\":\"\"}");
            request.addHeader("Authorization", "Bearer " + access_token);
            request.setRequestBody(HttpRequestBody.form(params, "UTF-8"));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return request;
    }

    public Request getToken() {
        Request request = new Request("http://ggzyjy.weihai.cn/EpointWebBuilder/rest/getOauthInfoAction/getNoUserAccessToken");
        request.setMethod(HttpConstant.Method.POST);
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            request.setRequestBody(HttpRequestBody.form(params, "UTF-8"));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return request;
    }

}
