package com.bidizhaobiao.data.Crawl.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.MessageDigest;

public class StringCoverMd5 {

    private static final Logger logger = LogManager.getLogger(StringCoverMd5.class);

    /**
     * @author jianlin
     * @date 2017年12月19日 下午1:47:36
     * @verison 1.0
     */
    public static String StringToMd5(String psw) {
        {
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(psw.getBytes("UTF-8"));
                byte[] encryption = md5.digest();

                StringBuffer strBuf = new StringBuffer();
                for (int i = 0; i < encryption.length; i++) {
                    if (Integer.toHexString(0xff & encryption[i]).length() == 1) {
                        strBuf.append("0").append(Integer.toHexString(0xff & encryption[i]));
                    } else {
                        strBuf.append(Integer.toHexString(0xff & encryption[i]));
                    }
                }

                return strBuf.toString();
            } catch (Exception e) {
                return "";
            }
        }
    }

    public static void main(String[] args) {
        String test = "123456";
        // 直接加密
        String hash = StringToMd5(test).toUpperCase();
        logger.info(hash);
        // 加盐
        //var hash = md5(md5(password)+salt);
    }
}


