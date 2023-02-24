package com.bidizhaobiao.data.Crawl.utils;

import com.bidizhaobiao.data.Crawl.service.ServiceContext;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 作者: 廉建林
 * @version 创建时间：2018年8月10日 上午9:36:22 类说明
 */
@Service
public class ProxyPoolUtil {
    public static String PROXY_POOL_API;
    public static final Logger logger = LoggerFactory.getLogger(ProxyPoolUtil.class);

    public static void main(String[] args) {
        getProxyPoolApi();
    }


    /**
     * 从IP代理池中获取一个IP
     */
    public static HttpHost getProxyPoolApi() {
        String respStr = "";
        HttpHost proxy = null;
        CloseableHttpClient client = HttpClients.createDefault();
        HttpResponse res;
        HttpGet httpGet = new HttpGet(PROXY_POOL_API);
        httpGet.addHeader("Content-type", "application/json;charset=UTF-8");
        httpGet.setHeader("Accept", "*/*");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            res = client.execute(httpGet);
            int statusCode = res.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                // 建议的写法1（编码要去Http头信息中查看）
                respStr = EntityUtils.toString(res.getEntity(), "UTF-8");
                JSONObject json = new JSONObject(respStr);
                if (json.has("result")) {
                    JSONArray arr = json.getJSONArray("result");
                    logger.info("成功代理池获取到ip代理信息是：" + arr);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject ipJson = arr.getJSONObject(i);
                        logger.info("获取到ip代理信息是：" + ipJson + "已经校验了" + i + "次");
                        String expireTime = ipJson.getString("expireTime");
                        Date expireDate = formatter.parse(expireTime);
                        if (expireDate.after(new Date())) {
                            String ip = ipJson.getString("ip");
                            Integer port = ipJson.getInt("port");
                            proxy = new HttpHost(ip, port);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info(e.getMessage());
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return proxy;
    }

    /**
     * 从IP代理池中获取一个IP
     */
    public static JSONObject invokeProxyPoolApi() {
        String respStr = "";
        us.codecraft.webmagic.proxy.Proxy proxy = null;
        CloseableHttpClient client = HttpClients.createDefault();
        HttpResponse res;
        HttpGet httpGet = new HttpGet(PROXY_POOL_API);
        httpGet.addHeader("Content-type", "application/json;charset=UTF-8");
        httpGet.setHeader("Accept", "*/*");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONObject resultJson = new JSONObject();
        try {
            res = client.execute(httpGet);
            int statusCode = res.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                // 建议的写法1（编码要去Http头信息中查看）
                respStr = EntityUtils.toString(res.getEntity(), "UTF-8");
                JSONObject json = new JSONObject(respStr);
                if (json.has("result")) {
                    JSONArray arr = json.getJSONArray("result");
                    logger.info("成功代理池获取到ip代理信息是：" + arr);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject ipJson = arr.getJSONObject(i);
                        logger.info("获取到ip代理信息是：" + ipJson + "已经校验了" + i + "次");
                        String expireTime = ipJson.getString("expireTime");
                        Date expireDate = formatter.parse(expireTime);
                        if (expireDate.after(new Date())) {
                            String ip = ipJson.getString("ip");
                            Integer port = ipJson.getInt("port");
                            proxy = new us.codecraft.webmagic.proxy.Proxy(ip, port);
                            resultJson.put("Proxy", proxy);
                            resultJson.put("expireDate", expireDate);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info(e.getMessage());
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultJson;
    }


    @Value("${spring.proxypool.api}")
    public void setApi(String db) {
        PROXY_POOL_API = db;
    }
}
