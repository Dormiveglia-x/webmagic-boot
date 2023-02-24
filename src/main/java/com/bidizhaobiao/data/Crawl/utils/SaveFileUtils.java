package com.bidizhaobiao.data.Crawl.utils;

import com.bidizhaobiao.data.Crawl.service.ServiceContext;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @Author: 廉建林
 * @从制定URL下载文件并保存到指定目录
 * @return
 */

public class SaveFileUtils {
    public static final Logger logger = LoggerFactory.getLogger(SaveFileUtils.class);

    public static void main(String[] args) {
        try {
            String url = "http://61.186.175.243/filed/3986/20201216125035.rar";
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            String infoLink = "";
            // 遍历字符串
            for (int i = 0; i < url.length(); i++) {
                char charAt = url.charAt(i);
                // 只对汉字处理
                if (isChineseChar(charAt)) {
                    String encode = URLEncoder.encode(charAt + "", "UTF-8");
                    infoLink += encode;
                } else {
                    infoLink += charAt;
                }
            }
            logger.info(fileName);
            String path = "FileInfo";
            // boolean result = saveUrlAs(infoLink, path, fileName);
            // System.out.println("下载的结果为：" + result);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 判断汉字的方法,只要编码在\u4e00到\u9fa5之间的都是汉字，以及中文标点
    private static boolean isChineseChar(char c) {
        boolean result = false;
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            result = true;

        }
        return result;
    }

    /**
     * 根据url下载文件，保存到filepath中,默认未处理ssl的附件下载问题
     *
     * @param url
     * @return
     */
    public JSONObject saveUrlAs(ServiceContext serviceContext, String url, String filePath, String fileName,
                                String detailLink) {
        boolean result = false;
        boolean needRest = false;
        JSONObject jsonObject = new JSONObject();
        if (url.contains("bidizhaobiao") || url.contains("bxkc.oss-cn-shanghai.aliyuncs.com")) {
            jsonObject.put("result", result);
            return jsonObject;
        }
        //修改url\异常方向
        if (url.contains("\\")) {
            url = url.replace("\\", "/");
        }
        int tryNum = 0;
        while ((tryNum < 3)) {
            CloseableHttpClient client = null;
            CloseableHttpResponse response = null;
            try {
                client = getHttpClient(serviceContext, serviceContext.isSaveFileAddSSL());
                logger.info("{}第{}次开始下载附件：{}", serviceContext.getName(), tryNum, url);
                HttpGet httpget = new HttpGet(url);
                httpget.addHeader("user-agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36");
                if (detailLink != null && (serviceContext.isSaveFileAddRef() || detailLink.contains("/info/"))) {
                    httpget.addHeader("Referer", detailLink);
                    String host = getHost(detailLink);
                    if (!"".equals(host)) {
                        httpget.addHeader("Host", host);
                    }
                }
                response = client.execute(httpget);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    Header[] heards = response.getAllHeaders();
                    int length = 0;
                    boolean findLength = false;
                    for (Header h : heards) {
                        if ("Content-Length".equals(h.getName())) {
                            findLength = true;
                            String value = h.getValue();
                            length = Integer.valueOf(value);
                            break;
                        }
                    }
                    if ((length >= (1024 * 1024 * 100)) && findLength) {
                        logger.info("{}附件{}下载文件过大，不下载", serviceContext.getName(), url);
                        break;
                    } else if (((100 + 1024 * 2) >= length) && findLength) {
                        logger.info("{}附件{}下载文件过小不下载", serviceContext.getName(), url);
                        break;
                    } else {
                        MessageDigest md = MessageDigest.getInstance("MD5");
                        InputStream is = entity.getContent();
                        // 文件保存位置
                        File saveDir = new File(filePath);
                        if (!saveDir.exists()) {
                            saveDir.mkdirs();
                        }
                        File file = new File(saveDir + "/" + fileName);
                        FileOutputStream fileout = new FileOutputStream(file);
                        /**
                         * 根据实际运行效果 设置缓冲区大小
                         */
                        byte[] buffer = new byte[10 * 1024];
                        int ch = 0;
                        while ((ch = is.read(buffer)) != -1) {
                            fileout.write(buffer, 0, ch);
                        }
                        is.close();
                        fileout.flush();
                        fileout.close();
                        result = true;

                        //将文件流转换成md5
                        FileInputStream fis = new FileInputStream(file);
                        int len = 0;
                        while ((len = fis.read(buffer)) != -1) {
                            md.update(buffer, 0, len);
                        }
                        byte[] b = md.digest();
                        String md5 = new HexBinaryAdapter().marshal(b).toLowerCase();
                        jsonObject.put("FileIno", md5);
                        logger.info("{}附件{}下载完毕，附件的大小为：{}", serviceContext.getName(), url, length);
                        break;
                    }
                } else if (statusCode == 403) {
                    if (!serviceContext.isNeedProxy()) {
                        logger.info("{}附件下载文件响应异常为403，自动添加代理后下载附件", url);
                        serviceContext.setNeedProxy(true);
                        needRest = true;
                    }
                } else {
                    logger.info("{}附件下载{}响应异常,状态码为{}，附件不下载", serviceContext.getName(), url, statusCode);
                    break;
                }
            } catch (Exception e) {
                if (e.getMessage().contains("PKIX path building failed")) {
                    logger.info("{}附件下载文件响应异常为ssl，自动添加ssl后下载附件", url);
                    serviceContext.setSaveFileAddSSL(true);
                }
                e.printStackTrace();
            } finally {
                tryNum = tryNum + 1;
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
        }
        logger.info("{}结束附件{}下载,附件下载的结果为：{}", serviceContext.getName(), url, result);
        jsonObject.put("result", result);
        if (needRest) {
            serviceContext.setNeedProxy(false);
        }

        return jsonObject;
    }

    // 设置重试次数控制连接超时
    private static HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {
        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            return false;
        }
    };

    // 从定时器外部获取httpclent的方法
    public CloseableHttpClient getHttpClient(ServiceContext serviceContext, boolean isNeedSsl) {
        SocketConfig.Builder socketConfigBuilder = SocketConfig.custom();
        socketConfigBuilder.setSoKeepAlive(true).setTcpNoDelay(true).setSoTimeout(30 * 1000);

        SocketConfig socketConfig = socketConfigBuilder.build();
        CloseableHttpClient client = null;
        HttpHost httpHost = null;
        String className = serviceContext.getName();
        try {
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30 * 1000)
                    .setConnectionRequestTimeout(30 * 1000).setSocketTimeout(30 * 1000).build();
            if (serviceContext.isNeedProxy()) {
                // 验证IP
                logger.info("{}附件下载代理开始获取代理", className);
                httpHost = ProxyPoolUtil.getProxyPoolApi();
                logger.info("{}附件下载代理结束获取代理", className);
            }
            if (isNeedSsl) {
                /* HttpClientBuilder b = HttpClientBuilder.create(); */
                HttpClientBuilder b = HttpClients.custom();
                // 禁用SSL验证
                SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                    public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                        return true;
                    }
                }).build();
                b.setSslcontext(sslContext);
                SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext,
                        NoopHostnameVerifier.INSTANCE);
                Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                        .<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", sslSocketFactory).build();
                BasicHttpClientConnectionManager connMgr = new BasicHttpClientConnectionManager(socketFactoryRegistry);
                connMgr.setSocketConfig(socketConfig);
                // HttpClients.custom().setConnectionManager(connMgr);
                // 需要代理，则在原有基础上添加代理
                if (httpHost != null) {
                    DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(httpHost);
                    client = HttpClients.custom().setConnectionManager(connMgr).setRoutePlanner(routePlanner)
                            .setDefaultRequestConfig(requestConfig).setDefaultSocketConfig(socketConfig)
                            .setRetryHandler(myRetryHandler).build();
                } else {
                    client = HttpClients.custom().setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig)
                            .setDefaultSocketConfig(socketConfig).setRetryHandler(myRetryHandler).build();
                }
            } else if (serviceContext.isNeedProxy()) {
                // 普通自发请求----需要代理
                if (httpHost != null) {
                    client = HttpClients.custom().setDefaultRequestConfig(requestConfig)
                            .setDefaultSocketConfig(socketConfig).setProxy(httpHost).setRetryHandler(myRetryHandler)
                            .build();
                } else {
                    client = HttpClients.custom().setDefaultRequestConfig(requestConfig)
                            .setDefaultSocketConfig(socketConfig).setRetryHandler(myRetryHandler).build();
                }
            } else {
                // 普通自发请求--不需要代理
                client = HttpClients.custom().setDefaultRequestConfig(requestConfig)
                        .setDefaultSocketConfig(socketConfig).setRetryHandler(myRetryHandler).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return client;
    }

    //根据正常url获取host
    private String getHost(String url) {
        String returnVal = "";
        try {
            URI uri = new URI(url);
            returnVal = uri.getHost();
        } catch (Exception e) {
        }
        return returnVal;
    }
}
