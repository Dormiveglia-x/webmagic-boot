package com.bidizhaobiao.data.Crawl.utils;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: 廉建林
 * @Date: 2019/12/6 14:06
 * @Version 1.0
 * 上传图片或者图片流到验证码识别接口
 * 校验成功则结束
 * 校验不成功，则调用日志接口记录异常信息
 * codeType  suansu  4-6位纯数字
 * shuzi,1-2位算术加减乘求模
 */
@Service
public class GetImgDataUtil {
    //获取验证码提交链接
    public static String uploadUrl;
    public static String errorLogUrl;

    @Value("${spring.getdata.uploadUrl}")
    public void setUpUrl(String upUrl) {
        uploadUrl = upUrl;
    }

    @Value("${spring.getdata.errorUrl}")
    public void setErrorUrl(String errorUrl) {
        errorLogUrl = errorUrl;
    }

    public static void main(String[] args) {
        try {
            uploadUrl = "http://121.46.18.113:17052/upload";
            String txtx = "";
            String codeType = "shuzi";
            String ttt = getImgDataByBase64(codeType, txtx);
            System.out.println("最终响应的结果为：" + ttt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //上传图片到指定接口并返回识别结果---上传文件
    public static String getImgData(String codeType, String imgName) throws Exception {
        String str = "";
        CloseableHttpResponse resp = null;
        CloseableHttpClient client = null;
        try {
            HttpPost httpPost = new HttpPost(uploadUrl);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("code", codeType);
            builder.addBinaryBody("pic", new File(imgName + ".jpg"));
            HttpEntity multipart = builder.build();
            client = HttpClients.createDefault();
            httpPost.setEntity(multipart);
            resp = client.execute(httpPost);
            if (resp.getStatusLine().getStatusCode() == 200) {
                str = EntityUtils.toString(resp.getEntity(), "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(SpecialUtil.getInfoOfExceptionStackTrace(e));
        } finally {
            try {
                if (resp != null) {
                    resp.close();
                }
                if (client != null) {
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return str;
    }

    //上传图片到指定接口并返回识别结果---上传base64
    public static String getImgDataByBase64(String codeType, String base64Info) throws Exception {
        String str = "";
        CloseableHttpResponse resp = null;
        CloseableHttpClient client = null;
        try {
            HttpPost httpPost = new HttpPost(uploadUrl);
            List<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("base64pic", base64Info));
            pairs.add(new BasicNameValuePair("code", codeType));
            httpPost.setEntity(new UrlEncodedFormEntity(pairs, "UTF-8"));
            client = HttpClients.createDefault();
            resp = client.execute(httpPost);
            if (resp.getStatusLine().getStatusCode() == 200) {
                str = EntityUtils.toString(resp.getEntity(), "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(SpecialUtil.getInfoOfExceptionStackTrace(e));
        } finally {
            try {
                if (resp != null) {
                    resp.close();
                }
                if (client != null) {
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return str;
    }

    //识别异常的时候，提交图片到日志记录接口
    public static String saveErrorImg(String codeType, String imgName) throws Exception {
        String str = "";
        CloseableHttpResponse resp = null;
        CloseableHttpClient client = null;
        try {
            HttpPost httpPost = new HttpPost(errorLogUrl);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("code", codeType);
            builder.addBinaryBody("pic", new File(imgName + ".jpg"));
            HttpEntity multipart = builder.build();
            client = HttpClients.createDefault();
            httpPost.setEntity(multipart);
            resp = client.execute(httpPost);
            if (resp.getStatusLine().getStatusCode() == 200) {
                str = EntityUtils.toString(resp.getEntity(), "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(SpecialUtil.getInfoOfExceptionStackTrace(e));
        } finally {
            try {
                if (resp != null) {
                    resp.close();
                }
                if (client != null) {
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return str;
    }

    //识别异常的时候，提交图片到日志记录接口
    public static String saveErrorImgByBase64(String codeType, String base64Info) throws Exception {
        String str = "";
        CloseableHttpResponse resp = null;
        CloseableHttpClient client = null;
        try {
            HttpPost httpPost = new HttpPost(errorLogUrl);
            List<NameValuePair> pairs = new ArrayList<>();
            pairs.add(new BasicNameValuePair("base64pic", base64Info));
            pairs.add(new BasicNameValuePair("code", codeType));
            httpPost.setEntity(new UrlEncodedFormEntity(pairs, "UTF-8"));
            client = HttpClients.createDefault();
            resp = client.execute(httpPost);
            if (resp.getStatusLine().getStatusCode() == 200) {
                str = EntityUtils.toString(resp.getEntity(), "UTF-8");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(SpecialUtil.getInfoOfExceptionStackTrace(e));
        } finally {
            try {
                if (resp != null) {
                    resp.close();
                }
                if (client != null) {
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return str;
    }


}
