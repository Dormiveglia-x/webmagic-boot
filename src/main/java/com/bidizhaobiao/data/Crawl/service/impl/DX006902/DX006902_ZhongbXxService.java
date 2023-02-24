package com.bidizhaobiao.data.Crawl.service.impl.DX006902;

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

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 程序员：许广衡
 * 日期：2021-03-18
 * 原网站：http://www.changchunwater.com/#/news/building?id=b96efe79-07c5-493f-8047-04c7be222c2e
 * 主页：http://www.changchunwater.com
 **/
@Service("DX006902_ZhongbXxService")
public class DX006902_ZhongbXxService extends SpiderService implements PageProcessor {
    public Spider spider = null;

    public String listUrl = "http://219.149.221.119:7234/api/news/typeid/b96efe79-07c5-493f-8047-04c7be222c2e/1/5";
    public String baseUrl = "http://www.changchunwater.com";
    // 抓取网站的相关配置，包括：编码、抓取间隔、重试次数等
    public Site site = Site.me().setCycleRetryTimes(2).setTimeOut(30000).setSleepTime(20);

    // 网站编号
    public String sourceNum = "DX006902";
    // 网站名称
    public String sourceName = "长春水务集团有限公司";
    // 信息源
    public String infoSource = "企业采购";
    // 设置地区
    public String area = "东北";
    // 设置省份
    public String province = "吉林";
    // 设置城市
    public String city = "长春市";
    // 设置县
    public String district;
    // 设置CreateBy
    public String createBy = "许广衡";

    //private Pattern pattern_page = Pattern.compile("1/(\\d+)");
    //private Pattern pattern_page = Pattern.compile("countPage =(\\d+)");
    private Pattern pattern = Pattern.compile("totalPage = parseInt\\('(.*?)'");
    private Pattern p = Pattern.compile("20\\d{2}-\\d{1,2}-\\d{1,2}");
    //是否需要入广联达
    private boolean isNeedInsertGonggxinxi = false;
    //站源类型
    private String taskType;
    //站源名称
    private String taskName;


    public Site getSite() {
        return this.site.setCharset("UTF-8");
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


    @Override
    public void process(Page page) {
        JSONObject obj = new JSONObject(page.getRawText());
        obj = obj.getJSONObject("value");
        String url = page.getUrl().toString();
        try {
            Thread.sleep(1000);
            if (url.contains("/typeid/b96efe79-07c5-493f-8047-04c7be222c2e/")) {

                if (page.getRawText().contains("totalcount")) {
                    if (serviceContext.getPageNum() == 1) {
                        String max = obj.get("totalcount").toString();
                        int maxPage = Integer.parseInt(max);
                        serviceContext.setMaxPage(maxPage);
                    }
                    JSONArray list = obj.getJSONArray("list");
                    List<BranchNew> detailList = new ArrayList<>();
                    if (list.length() > 0) {
                        for (int i = 0; i < list.length(); i++) {
                            JSONObject oo = list.getJSONObject(i);
                            String title = oo.get("tittle").toString();
                            String date = oo.get("createtime").toString();
                            date = date.replaceAll("[./年月]", "-");
                            Matcher m = p.matcher(date);
                            if (m.find()) {
                                date = sdf.get().format(sdf.get().parse(m.group()));
                            }
                            if (!CheckProclamationUtil.isProclamationValuable(title)){
                                continue;
                            }
                            String id = oo.get("id").toString();
                            String link = "http://219.149.221.119:7234/api/news/" + id;
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
                    }
                } else {
                    dealWithNullListPage(serviceContext);
                }
                if (serviceContext.getPageNum() < serviceContext.getMaxPage() && serviceContext.isNeedCrawl()) {
                    serviceContext.setPageNum(serviceContext.getPageNum() + 1);
                    int index = serviceContext.getPageNum();
                    page.addTargetRequest(listUrl.replace("/1/5", "/" + index + "/5"));
                }
            } else {
                if (page.getStatusCode() == 404) return;
                BranchNew bn = map.get(url);
                if (bn == null) {
                    return;
                }
                String content = obj.get("contents").toString();
                String title = obj.get("tittle").toString();
                content = URLDecoder.decode(content, "UTF-8");
                Document doc = Jsoup.parse(content);
                String Title = bn.getTitle();
                String recordId = bn.getId();
                serviceContext.setCurrentRecord(recordId);
                String Time = bn.getDate();
                map.remove(url);//清除冗余
                String path = "http://www.changchunwater.com";
                String path1 = bn.getLink();
                path1 = path1.substring(0, path1.lastIndexOf("/"));
                doc.select("input").remove();
                doc.select("meta").remove();
                doc.select("script").remove();
                doc.select("link").remove();
                doc.select("style").remove();
                doc.outputSettings().prettyPrint(true);//允许格式化文档格式
                Element conTag = doc;
                if (conTag != null) {
                    conTag.select("*[style~=^.*display\\s*:\\s*none\\s*(;\\s*[0-9A-Za-z]+|;\\s*)?$]").remove();//删除隐藏格式
                    conTag.select("iframe").remove();
                    Elements as = conTag.select("a");
                    for (Element ae : as) {
                        String href = ae.attr("href");
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

                        if (href.contains("mailto:") || href.contains("#")) {
                            ae.remove();
                        }
                    }
                    Elements imgs = conTag.select("img");
                    for (Element imge : imgs) {
                        String src = imge.attr("src");
                        if (src.contains("data:image")) {
                            try {
                                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                                String dateString = formatter.format(new Date());
                                String path2 = imgPath + "/" + dateString + "/" + bn.getDate() + "/" + sourceNum;
                                String uuid = UUID.randomUUID().toString();
                                String fileName = uuid + ".jpg";
                                String newLink = "http://www.bidizhaobiao.com/file/" + dateString + "/" + bn.getDate()
                                        + "/" + sourceNum + "/" + fileName;
                                // 文件保存位置
                                File saveDir = new File(path2);
                                if (!saveDir.exists()) {
                                    saveDir.mkdirs();
                                }
                                byte[] imagedata = DatatypeConverter
                                        .parseBase64Binary(src.substring(src.indexOf(",") + 1));
                                BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imagedata));
                                ImageIO.write(bufferedImage, "png", new File(path2 + "/" + fileName));
                                imge.attr("src", newLink);
                            } catch (Exception e) {
                                imge.remove();
                            }
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
                    content = "<div>" + title + "</div>" + conTag.outerHtml();
                } else if (url.contains(".doc") || url.contains(".rar") || url.contains(".pdf") || url.contains(".zip") || url.contains(".xls")) {
                    content = "<div>附件下载：<a href='" + url + "'>" + Title + "</a></div>";
                    title = Title;
                }
                RecordVO recordVo = new RecordVO();
                recordVo.setTitle(title);
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
