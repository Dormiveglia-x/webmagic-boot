package com.bidizhaobiao.data.Crawl.service.impl.DX007528;

import com.bidizhaobiao.data.Crawl.entity.oracle.BranchNew;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.MyDownloader;
import com.bidizhaobiao.data.Crawl.service.SpiderService;
import com.bidizhaobiao.data.Crawl.utils.SpecialUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.model.HttpRequestBody;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.utils.HttpConstant;

import java.util.*;

/**
 * @Author: 史炜立
 * @DateTime: 2020/11/17
 * @Description: 原网站：https://bidding.e-cbest.com:8000/index.do
 * 主页：https://bidding.e-cbest.com:8000/
 */
@Service
public class DX007528_2_ZhongbXxService extends SpiderService implements PageProcessor {


    public Spider spider = null;

    public String listUrl = "https://bidding.e-cbest.com:8000/LoginAction!noticeList.do";
    public String baseUrl = "https://bidding.e-cbest.com:8000/";

    // 网站编号
    public String sourceNum = "DX007528-2";
    // 网站名称
    public String sourceName = "重庆百货招采平台";
    // 信息源
    public String infoSource = "企业采购";
    // 设置地区
    public String area = "西南";
    // 设置省份
    public String province = "重庆";
    //创建人
    public String createBy = "郭建婷";

    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    Site site = Site.me().setCycleRetryTimes(2).setTimeOut(30000).setSleepTime(20);
    // 是否需要入广联达
    public boolean isNeedInsertGonggxinxi = false;

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
        spider.addRequest(getListRequest(1));
        serviceContext.setSpider(spider);
        spider.run();
        // 爬虫状态监控部分
        saveCrawlResult(serviceContext);
    }

    public void process(Page page) {
        String url = page.getUrl().toString();
        String detailHtml = page.getRawText();
        try {
            List<BranchNew> detailList = new ArrayList<BranchNew>();
            Thread.sleep(500);
            if (url.contains("List")) {
                JSONObject dataJsonObject = new JSONObject(detailHtml);
                if (dataJsonObject.length() > 0) {

                    JSONArray dataJsonArray = dataJsonObject.getJSONArray("dataList");
                    for (int i = 0; i < dataJsonArray.length(); i++) {
                        JSONObject datailContent = dataJsonArray.getJSONObject(i);
                        String title = datailContent.getString("resulttitle");//详情title
                        Long sendTime = datailContent.getLong("publicizeStartTime");//详情时间
                        String content = datailContent.getString("suppliercompname");
                        String link = baseUrl;//详情链接
                        String date = SpecialUtil.date2Str(new Date(sendTime));
                        String id = SpecialUtil.stringMd5(title + date);//id
                        content = "<div>\n" +
                                "<h4>" + title + "</h4>" +
                                "    <table>" +
                                "        <tr>" +
                                "            <td>中标公告</td>" +
                                "            <td>公示时间</td>" +
                                "            <td>供应商名称</td>" +
                                "        </tr>" +
                                "        <tr>" +
                                "            <td>" + title + "</td>" +
                                "            <td>" + date + "</td>" +
                                "            <td>" + content + "</td>" +
                                "        </tr>" +
                                "    </table>" +
                                "</div>";
                        BranchNew branch = new BranchNew();
                        branch.setId(id);
                        branch.setLink(link);
                        branch.setTitle(title);
                        branch.setDate(date);
                        branch.setContent(content);
                        detailList.add(branch);
                    }
                    List<BranchNew> branchNewList = checkData(detailList, serviceContext);
                    for (BranchNew branch : branchNewList) {
                        String content = branch.getContent();
                        detailHtml = Jsoup.parse(content).outerHtml();

                        RecordVO recordVO = new RecordVO();
                        recordVO.setId(branch.getId());
                        recordVO.setListTitle(branch.getTitle());
                        recordVO.setTitle(branch.getTitle());
                        recordVO.setDetailLink("https://bidding.e-cbest.com:8000/");
                        recordVO.setDetailHtml(detailHtml);
                        recordVO.setDdid(SpecialUtil.stringMd5(detailHtml));
                        recordVO.setDate(branch.getDate());
                        recordVO.setContent(branch.getContent());
                        dataStorage(serviceContext, recordVO, branch.getType());
                    }
                } else {
                    dealWithNullListPage(serviceContext);
                }
                if (dataJsonObject.has("total") && serviceContext.getPageNum() == 1) {
                    int totalPage = dataJsonObject.getInt("total");//total pages
                    int maxPage = totalPage / 10 == 0 ? totalPage / 10 : totalPage / 10 + 1;
                    serviceContext.setMaxPage(maxPage);
                }
                if (serviceContext.getPageNum() < serviceContext.getMaxPage() && serviceContext.isNeedCrawl()) {
                    //翻页规则
                    serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                    page.addTargetRequest(getListRequest(serviceContext.getPageNum()));
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            dealWithError(url, serviceContext, e);
        }
    }

    public static Request getListRequest(int page) {
        String url = "https://bidding.e-cbest.com:8000/LoginAction!noticeList.do";
        Request request = new Request(url);
        request.setMethod(HttpConstant.Method.POST);
        //request.setRequestBody(HttpRequestBody.xml(params, "UTF-8"))
        Map parmMap = new HashMap();
        String parmJson = "{\"page\":" + page + ",\"size\":10,\"type\":\"WinBid\"}";
        parmMap.put("paramJson", parmJson);

        request.setRequestBody(HttpRequestBody.form(parmMap, "UTF-8"));

        return request;
    }
}



