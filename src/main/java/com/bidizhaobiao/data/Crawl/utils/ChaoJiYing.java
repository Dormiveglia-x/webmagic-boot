package com.bidizhaobiao.data.Crawl.utils;

import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Date;

/**
 * @author 作者: 廉建林
 * @version 创建时间：2018年9月14日 上午10:02:54
 * 超级鹰Api工具类
 */
public class ChaoJiYing {


    public static final String codetype = "1902";
    public static final String codetype1 = "1005";
    public static final String chinesetype = "2004";//汉字
    public static final String suanshutype = "6001";// 算数
    public static final String codetype2 = "1008";

    public static final String codetype4 = "4005";

    public static final String len_min = "0";
    public static final String softid = "af82b7e4cc459db590162fe0da816733";
    public static String username = "bxkcljx";
    public static String password = "Biaoxun666@";

    /**
     * 字符串MD5加密
     *
     * @param s 原始字符串
     * @return 加密后字符串
     */
    public final static String MD5(String s) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            byte[] btInput = s.getBytes();
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 通用POST方法
     *
     * @param url   请求URL
     * @param param 请求参数，如：username=test&password=1
     * @return response
     * @throws IOException
     */
    public static String httpRequestData(String url, String param)
            throws IOException {
        URL u;
        HttpURLConnection con = null;
        OutputStreamWriter osw;
        StringBuffer buffer = new StringBuffer();

        u = new URL(url);
        con = (HttpURLConnection) u.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");

        osw = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
        osw.write(param);
        osw.flush();
        osw.close();

        BufferedReader br = new BufferedReader(new InputStreamReader(con
                .getInputStream(), "UTF-8"));
        String temp;
        while ((temp = br.readLine()) != null) {
            buffer.append(temp);
            buffer.append("\n");
        }

        return buffer.toString();
    }

    /**
     * 查询题分
     *
     * @param username 用户名
     * @param password 密码
     * @return response
     * @throws IOException
     */
    public static String GetScore(String username, String password) {
        String param = String.format("user=%s&pass=%s", username, password);
        String result;
        try {
            result = ChaoJiYing.httpRequestData(
                    "http://upload.chaojiying.net/Upload/GetScore.php", param);
        } catch (IOException e) {
            result = "未知问题";
        }
        return result;
    }

    /**
     * 注册账号
     *
     * @param username 用户名
     * @param password 密码
     * @return response
     * @throws IOException
     */
    public static String UserReg(String username, String password) {
        String param = String.format("user=%s&pass=%s", username, password);
        String result;
        try {
            result = ChaoJiYing.httpRequestData(
                    "http://upload.chaojiying.net/Upload/UserReg.php", param);
        } catch (IOException e) {
            result = "未知问题";
        }
        return result;
    }

    /**
     * 账号充值
     *
     * @param username 用户名
     * @param card     卡号
     * @return response
     * @throws IOException
     */
    public static String UserPay(String username, String card) {

        String param = String.format("user=%s&card=%s", username, card);
        String result;
        try {
            result = ChaoJiYing.httpRequestData(
                    "http://upload.chaojiying.net/Upload/UserPay.php", param);
        } catch (IOException e) {
            result = "未知问题";
        }
        return result;
    }

    /**
     * 报错返分
     *
     * @param username 用户名
     * @param password 用户密码
     * @param
     * @param id       图片ID
     * @return response
     * @throws IOException
     */
    public static String ReportError(String username, String password, String softid, String id) {

        String param = String
                .format(
                        "user=%s&pass=%s&softid=%s&id=%s",
                        username, password, softid, id);
        String result;
        try {
            result = ChaoJiYing.httpRequestData(
                    "http://upload.chaojiying.net/Upload/ReportError.php", param);
        } catch (IOException e) {
            result = "未知问题";
        }

        return result;
    }


    /**
     * 核心上传函数
     *
     * @param param 请求参数，如：username=test&password=1
     * @param data  图片二进制流
     * @return response
     * @throws IOException
     */
    public static String httpPostImage(String param, byte[] data) throws IOException {
        long time = (new Date()).getTime();
        URL u = null;
        HttpURLConnection con = null;
        String boundary = "----------" + MD5(String.valueOf(time));
        String boundarybytesString = "\r\n--" + boundary + "\r\n";
        OutputStream out = null;

        u = new URL("http://upload.chaojiying.net/Upload/Processing.php");

        con = (HttpURLConnection) u.openConnection();
        con.setRequestMethod("POST");
        //con.setReadTimeout(60000);
        con.setConnectTimeout(60000);
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setUseCaches(true);
        con.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);

        out = con.getOutputStream();

        for (String paramValue : param.split("[&]")) {
            out.write(boundarybytesString.getBytes("UTF-8"));
            String paramString = "Content-Disposition: form-data; name=\""
                    + paramValue.split("[=]")[0] + "\"\r\n\r\n" + paramValue.split("[=]")[1];
            out.write(paramString.getBytes("UTF-8"));
        }
        out.write(boundarybytesString.getBytes("UTF-8"));

        String paramString = "Content-Disposition: form-data; name=\"userfile\"; filename=\""
                + "chaojiying_java.gif" + "\"\r\nContent-Type: application/octet-stream\r\n\r\n";
        out.write(paramString.getBytes("UTF-8"));

        out.write(data);

        String tailer = "\r\n--" + boundary + "--\r\n";
        out.write(tailer.getBytes("UTF-8"));

        out.flush();
        out.close();

        StringBuffer buffer = new StringBuffer();
        BufferedReader br = new BufferedReader(new InputStreamReader(con
                .getInputStream(), "UTF-8"));
        String temp;
        while ((temp = br.readLine()) != null) {
            buffer.append(temp);
            buffer.append("\n");
        }

        return buffer.toString();
    }

    /**
     * 识别图片_按图片文件路径
     *
     * @param username 用户名
     * @param password 密码
     * @param softid   软件ID
     * @param codetype 图片类型
     * @param len_min  最小位数
     * @param filePath 图片文件路径
     * @return
     * @throws IOException
     */
    public static String PostPic(String username, String password, String softid, String codetype, String len_min, String filePath) {
        String result = "";
        String param = String
                .format(
                        "user=%s&pass=%s&softid=%s&codetype=%s&len_min=%s", username, password, softid, codetype, len_min);
        try {
            File f = new File(filePath);
            if (null != f) {
                int size = (int) f.length();
                byte[] data = new byte[size];
                FileInputStream fis = new FileInputStream(f);
                fis.read(data, 0, size);
                if (null != fis) fis.close();

                if (data.length > 0) result = ChaoJiYing.httpPostImage(param, data);
            }
        } catch (Exception e) {
            result = "未知问题";
        }


        return result;
    }

    /**
     * 识别图片_按图片二进制流
     *
     * @param username 用户名
     * @param password 密码
     * @param softid   软件ID
     * @param codetype 图片类型
     * @param len_min  最小位数
     * @param byteArr  图片二进制数据流
     * @return
     * @throws IOException
     */
    public static String PostPic(String username, String password, String softid, String codetype, String len_min, byte[] byteArr) {
        String result = "";
        String param = String
                .format(
                        "user=%s&pass=%s&softid=%s&codetype=%s&len_min=%s", username, password, softid, codetype, len_min);
        try {
            result = ChaoJiYing.httpPostImage(param, byteArr);
        } catch (Exception e) {
            result = "未知问题";
        }


        return result;
    }

    /**
     * 识别图片_按图片base64字符串 请提前参考base64注意事项 http://www.chaojiying.com/api-46.html
     *
     * @param username    用户名
     * @param password    密码
     * @param softid      软件ID
     * @param codetype    图片类型
     * @param len_min     最小位数
     * @param file_base64 图片base64字符串
     * @return
     * @throws IOException
     */
    public static String PostPic_base64(String username, String password, String softid, String codetype, String len_min, String file_base64) {

        // URL编码
        try {
            file_base64 = URLEncoder.encode(file_base64, "UTF-8");
        } catch (Exception e) {
            return "";
        }
        String param = String.format("user=%s&pass=%s&softid=%s&codetype=%s&len_min=%s&file_base64=%s", username, password, softid, codetype, len_min, file_base64);
        String result;
        try {
            result = ChaoJiYing.httpRequestData(
                    "http://upload.chaojiying.net/Upload/Processing.php", param);
        } catch (IOException e) {
            result = "未知问题";
        }
        return result;
    }

    //根据下载的图片来识别
    public static String CheckImg(String codeType, String imgname) {
        String pic_str = null;
        ChaoJiYing chaojiying = new ChaoJiYing();
        ChaoJiYing test = new ChaoJiYing();


        /*根据下载的图片，调取超级鹰，识别验证码
         *
         * 识别图片_按图片文件路径
         * @param username		用户名
         * @param password		密码
         * @param softid		软件ID
         * @param codetype		图片类型
         * @param len_min		最小位数默认为0
         * @param filePath		图片文件路径
         * @return
         * @throws IOException
         */

        //识别图片
        String result = chaojiying.PostPic(username, password, softid, codeType, len_min, imgname);
        //System.out.println("超级鹰返回的结果---------->" + result);
        //解析返回的json数据
        JSONObject dataJson = JSONObject.parseObject(result);
        String err_no = dataJson.get("err_no").toString();
        //		System.out.println("返回代码:"+err_no);

        //中文描述的返回信息
        String err_str = dataJson.get("err_str").toString();
        //		System.out.println("中文描述的返回信息:"+err_str);
        if (err_str.equals("OK")) {
            //			System.out.println("验证码识别成功");
        } else {
            pic_str = "验证码识别错误";
        }
        //图片标识号
        String pic_id = dataJson.get("pic_id").toString();
        //		System.out.println("图片标识号:"+pic_id);
        //识别出的结果
        pic_str = dataJson.get("pic_str").toString();
        //md5校验值
        String md5 = dataJson.get("md5").toString();
        //		System.err.println(pic_str+","+pic_id);
        return pic_str + "," + pic_id;
    }


    //识别错误，返回题分
    public static void errInfo(String pic_id) {
			/*
			 * user=用户账号
				pass=用户密码 //或 pass2=md5('用户密码')
				id=图片标识号  即识别接口返回来的pic_id字段值
				softid=软件ID
			 *
			 *
			 */
        ChaoJiYing chaojiying = new ChaoJiYing();
        String test = chaojiying.ReportError(username, password, softid, pic_id);
        //System.out.println(test);
    }
}
