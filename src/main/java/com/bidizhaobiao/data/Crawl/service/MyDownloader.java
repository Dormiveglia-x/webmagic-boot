package com.bidizhaobiao.data.Crawl.service;

import com.bidizhaobiao.data.Crawl.utils.ProxyPoolUtil;
import com.bidizhaobiao.data.Crawl.utils.SpecialUtil;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.HttpClientRequestContext;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.ProxyProvider;
import us.codecraft.webmagic.selector.PlainText;

import java.io.IOException;
import java.util.Date;
import java.util.Random;
import java.util.Set;

/*
重写download记录异常信息
 */
public class MyDownloader extends HttpClientDownloader {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ServiceContext serviceContext;
    private String url;
    public Random random = new Random();

    public MyDownloader(ServiceContext serviceContext, boolean useProxy, String url) {
        this.serviceContext = serviceContext;
        this.url = url;
        if (useProxy) {
            serviceContext.setNeedProxy(true);
            setProxyProvider(new MyProxy());
        }
        // TODO Auto-generated constructor stub
    }

    protected void onSuccess(Request request) {

    }

    //新增一个ua池
    String[] userAgentPool = {"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/22.0.1207.1 Safari/537.1",

            "Mozilla/5.0 (X11; CrOS i686 2268.111.0) AppleWebKit/536.11 (KHTML, like Gecko) Chrome/20.0.1132.57 Safari/536.11",

            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.6 (KHTML, like Gecko) Chrome/20.0.1092.0 Safari/536.6",

            "Mozilla/5.0 (Windows NT 6.2) AppleWebKit/536.6 (KHTML, like Gecko) Chrome/20.0.1090.0 Safari/536.6",

            "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/19.77.34.5 Safari/537.1",

            "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.8) Gecko Fedora/1.9.0.8-1.fc10 Kazehakase/0.5.6",

            "Mozilla/5.0 (X11; Linux i686; U;) Gecko/20070322 Kazehakase/0.4.5",

            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.71 Safari/537.1 LBBROWSER",

            "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)",

            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11",

            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11",

            "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; QQDownload 732; .NET4.0C; .NET4.0E)",

            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11",

            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; SV1; QQDownload 732; .NET4.0C; .NET4.0E; 360SE)",

            "Mozilla/4.0 (compatible; MSIE 7.0b; Windows NT 5.2; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.2; .NET CLR 3.0.04506.30)",

            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/535.20 (KHTML, like Gecko) Chrome/19.0.1036.7 Safari/535.20",

            "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.8) Gecko Fedora/1.9.0.8-1.fc10 Kazehakase/0.5.6",

            "Mozilla/5.0 (X11; U; Linux x86_64; zh-CN; rv:1.9.2.10) Gecko/20100922 Ubuntu/10.10 (maverick) Firefox/3.6.10",

            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.71 Safari/537.1 LBBROWSER",

            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.89 Safari/537.1",

            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; Acoo Browser; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.0.04506)",

            "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.0.12) Gecko/20070731 Ubuntu/dapper-security Firefox/1.5.0.12",

            "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; QQDownload 732; .NET4.0C; .NET4.0E; LBBROWSER)",

            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.89 Safari/537.1",

            "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3 like Mac OS X) AppleWebKit/603.1.30 (KHTML, like Gecko) Version/10.3 Mobile/14E277 Safari/603.1.30",

            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36",

            "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; AcooBrowser; .NET CLR 1.1.4322; .NET CLR 2.0.50727)",

            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; Acoo Browser; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.0.04506)",

            "Mozilla/4.0 (compatible; MSIE 7.0; AOL 9.5; AOLBuild 4337.35; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)",

            "Mozilla/5.0 (Windows; U; MSIE 9.0; Windows NT 9.0; en-US)",

            "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET CLR 2.0.50727; Media Center PC 6.0)",

            "Mozilla/5.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET CLR 1.0.3705; .NET CLR 1.1.4322)",

            "Mozilla/4.0 (compatible; MSIE 7.0b; Windows NT 5.2; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.2; .NET CLR 3.0.04506.30)",

            "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN) AppleWebKit/523.15 (KHTML, like Gecko, Safari/419.3) Arora/0.3 (Change: 287 c9dfb30)",

            "Mozilla/5.0 (X11; U; Linux; en-US) AppleWebKit/527+ (KHTML, like Gecko, Safari/419.3) Arora/0.6",

            "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.2pre) Gecko/20070215 K-Ninja/2.1.1",

            "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9) Gecko/20080705 Firefox/3.0 Kapiko/3.0",

            "Mozilla/5.0 (X11; Linux i686; U;) Gecko/20070322 Kazehakase/0.4.5",

            "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.8) Gecko Fedora/1.9.0.8-1.fc10 Kazehakase/0.5.6",

            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11",

            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/535.20 (KHTML, like Gecko) Chrome/19.0.1036.7 Safari/535.20",

            "Opera/9.80 (Macintosh; Intel Mac OS X 10.6.8; U; fr) Presto/2.9.168 Version/11.52",

            "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.8) Gecko Fedora/1.9.0.8-1.fc10 Kazehakase/0.5.6",

            "Mozilla/5.0 (X11; Linux i686; U;) Gecko/20070322 Kazehakase/0.4.5",

            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.71 Safari/537.1 LBBROWSER",

            "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)",

            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11",

            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11",

            "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; QQDownload 732; .NET4.0C; .NET4.0E)",

            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11",

            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Trident/4.0; SV1; QQDownload 732; .NET4.0C; .NET4.0E; 360SE)",

            "Mozilla/4.0 (compatible; MSIE 7.0b; Windows NT 5.2; .NET CLR 1.1.4322; .NET CLR 2.0.50727; InfoPath.2; .NET CLR 3.0.04506.30)",

            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_3) AppleWebKit/535.20 (KHTML, like Gecko) Chrome/19.0.1036.7 Safari/535.20",

            "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.8) Gecko Fedora/1.9.0.8-1.fc10 Kazehakase/0.5.6",

            "Mozilla/5.0 (X11; U; Linux x86_64; zh-CN; rv:1.9.2.10) Gecko/20100922 Ubuntu/10.10 (maverick) Firefox/3.6.10",

            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.71 Safari/537.1 LBBROWSER",

            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.89 Safari/537.1",

            "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; Acoo Browser; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.0.04506)",

            "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.0.12) Gecko/20070731 Ubuntu/dapper-security Firefox/1.5.0.12",

            "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; QQDownload 732; .NET4.0C; .NET4.0E; LBBROWSER)",

            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.89 Safari/537.1",

            "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3 like Mac OS X) AppleWebKit/603.1.30 (KHTML, like Gecko) Version/10.3 Mobile/14E277 Safari/603.1.30",

            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36",

            "Mozilla/5.0 (Windows; U; MSIE 9.0; Windows NT 9.0; en-US"};

    //设置重试次数控制连接超时
    private static HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {
        @Override
        public boolean retryRequest(IOException exception,
                                    int executionCount, HttpContext context) {
            return false;
        }
    };

    @Override
    public Page download(Request request, Task task) {
        //先过滤掉请求为baidu的网站
        Page page = Page.fail();
        if (request.getUrl().contains("www.baidu.com")) {
            onSuccess(request);
            page.setRawText("这是一条百度的请求");
            page.setUrl(new PlainText(request.getUrl()));
            page.setRequest(request);
            page.setStatusCode(200);
            page.setDownloadSuccess(true);
            return page;
        }
        if (task == null || task.getSite() == null) {
            throw new NullPointerException("task or site can not be null");
        }
        CloseableHttpResponse httpResponse = null;
        CloseableHttpClient httpClient = getHttpClient(task.getSite());
        Proxy proxy = null;
        HttpHost hostInfo = serviceContext.getProxyInfo();
        if (serviceContext.isNeedProxy() && hostInfo == null) {
            logger.info("{}进入downloader时未获取到代理信息", serviceContext.getName());
            if (proxyProvider != null) {
                logger.info("{}请求代理开始", serviceContext.getName());
                proxy = proxyProvider.getProxy(task);
                if (proxy != null) {
                    hostInfo = new HttpHost(proxy.getHost(), proxy.getPort());
                    serviceContext.setProxyInfo(hostInfo);
                    logger.info("{}{}:{},代理的失效时间为：{}", serviceContext.getName(), proxy.getHost(), proxy.getPort(), serviceContext.getProxyExpireDate());
                }
            }
        } else if (hostInfo != null) {
            //判断代理是否过期
            Date proxyExpireDate = serviceContext.getProxyExpireDate();
            if (proxyExpireDate.after(new Date())) {
                logger.info("{}代理还未失效{}:{}，时间为：{}", serviceContext.getName(), hostInfo.getHostName(), hostInfo.getPort(), proxyExpireDate);
                proxy = new Proxy(hostInfo.getHostName(), hostInfo.getPort());
            } else {
                logger.info("{}代理已过期重新请求代理开始", serviceContext.getName());
                //代理池识别到代理失效
                proxy = proxyProvider.getProxy(task);
                if (proxy != null) {
                    logger.info("{}代理已过期重新请求代理结束{}:{},代理的失效时间为：{}", serviceContext.getName(), hostInfo.getHostName(), hostInfo.getPort(), serviceContext.getProxyExpireDate());
                }
            }

        }
        //切换代理池
        int userAgentRandom = random.nextInt(userAgentPool.length);
        task.getSite().setUserAgent(userAgentPool[userAgentRandom]);
        HttpClientRequestContext requestContext = httpUriRequestConverter.convert(request, task.getSite(), proxy);
        try {
            logger.info("开始请求{},请求的链接为{}", serviceContext.getName(), request.getUrl());
            requestContext.getHttpUriRequest().addHeader("Connection", "close");
            httpResponse = httpClient.execute(requestContext.getHttpUriRequest(),
                    requestContext.getHttpClientContext());
            page = handleResponse(request, task.getSite().getCharset(), httpResponse, task);
            httpResponse.addHeader("Connection", "close");
            int codeNum = httpResponse.getStatusLine().getStatusCode();
            logger.info("结束请求{},请求的链接为{},状态码为：{}", serviceContext.getName(), request.getUrl(), codeNum);
            if (codeNum != 200) {
                Set<Integer> codeList = task.getSite().getAcceptStatCode();
                boolean isValuble = false;
                for (Integer code : codeList) {
                    if (codeNum == code) {
                        isValuble = true;
                        break;
                    }
                }
                if (!isValuble) {
                    serviceContext.setProxyInfo(null);
                    //                    page.setDownloadSuccess(false);//如果因为状态码的异常导致的不重试
                    if (serviceContext.isNeedProxy()) {
                        onError(request, new Exception(serviceContext.getName() + "使用代理后网站响应异常状态码为：" + codeNum), page, codeNum);
                    } else {
                        onError(request, new Exception(serviceContext.getName() + "网站响应异常状态码为：" + codeNum), page, codeNum);
                    }
                    logger.debug("downloading page error{}", codeNum);
                } else {
                    onSuccess(request);
                }
            } else {
                onSuccess(request);
            }
            logger.debug("downloading page success {}", page);
            return page;
        } catch (Exception e) {
            int codeNum = 200;
            String errorInfo = SpecialUtil.getErrorInfoFromException(e);
            if (request.getUrl().contains("123.57.248.129:8999") && errorInfo.contains("Read timed out")) {
                onSuccess(request);
                page.setRawText("原网这条链接超时");
                page.setUrl(new PlainText(request.getUrl()));
                page.setRequest(request);
                page.setStatusCode(200);
                page.setDownloadSuccess(true);
                return page;
            } else {
                logger.info("{},异常的链接为{}，异常的信息为：{}", serviceContext.getName(), request.getUrl(), SpecialUtil.getErrorInfoFromException(e));
                serviceContext.setProxyInfo(null);
                onError(request, e, page, codeNum);
                return page;
            }
        } finally {
            if (httpResponse != null) {
                // ensure the connection is released back to pool
                EntityUtils.consumeQuietly(httpResponse.getEntity());
            }
            if (proxyProvider != null && proxy != null) {
                proxyProvider.returnProxy(proxy, page, task);
            }
        }
    }

    protected void onError(Request request, Exception e, Page page, int codeNum) {
        //请求失败时，将代理信息清空
        serviceContext.setProxyInfo(null);
        // 获取到爬虫已经请求的次数
        long pageCount = serviceContext.getSpider().getPageCount();
        String errorInfo = SpecialUtil.getErrorInfoFromException(e);
        Object cycleTriedTimesObject = request.getExtra(Request.CYCLE_TRIED_TIMES);
        // 网站有问题
        if (cycleTriedTimesObject != null && pageCount != 0) {
            int cycleTriedTimes = (Integer) cycleTriedTimesObject;
            if (cycleTriedTimes >= (serviceContext.getSpider().getSite().getCycleRetryTimes() - 1)) {
                serviceContext.setErrorNum(serviceContext.getErrorNum() + 1);
                String message = null;
                if (serviceContext.isNeedProxy()) {
                    message = "加了代理仍在报错，" + errorInfo;
                    logger.info("{}加了代理仍在报错", serviceContext.getName());
                } else if (errorInfo.contains("网站响应异常状态码")) {
                    message = errorInfo;
                } else {
                    message = "网站请求失败：" + errorInfo;
                }
                JSONObject errorJson = new JSONObject();
                errorJson.put("link", request.getUrl());
                errorJson.put("errorInfo", message);
                serviceContext.getErrorJsonArray().put(errorJson);
            }
        } else if (page.isDownloadSuccess() && pageCount == 0) {
            serviceContext.setErrorNum(serviceContext.getErrorNum() + 1);
            //处理首页打开404或者其他状态码但是被认定webmagic认定是正常下载的编号
            String message = "首页请求异常，异常的状态码为：" + codeNum + "请检查网站是否变更";
            JSONObject errorJson = new JSONObject();
            errorJson.put("link", request.getUrl());
            errorJson.put("errorInfo", message);
            serviceContext.getErrorJsonArray().put(errorJson);
        }else if (page.isDownloadSuccess()){
            serviceContext.setErrorNum(serviceContext.getErrorNum() + 1);
            //如果识别到DownloadSuccess 则不会重试，所以需要在这里记录异常信息
            JSONObject errorJson = new JSONObject();
            errorJson.put("link", request.getUrl());
            errorJson.put("errorInfo", errorInfo);
            serviceContext.getErrorJsonArray().put(errorJson);
        }

    }

    /*
     * 代理
     */
    class MyProxy implements ProxyProvider {
        @Override
        public void returnProxy(Proxy proxy, Page page, Task task) {

        }

        public Proxy getProxy(Task task) {
            JSONObject proxyJson = ProxyPoolUtil.invokeProxyPoolApi();
            Proxy proxy = (Proxy) proxyJson.get("Proxy");
            Date expireDate = (Date) proxyJson.get("expireDate");
            serviceContext.setProxyExpireDate(expireDate);
            return proxy;
        }
    }

}
