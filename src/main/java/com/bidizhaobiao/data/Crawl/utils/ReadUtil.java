package com.bidizhaobiao.data.Crawl.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.util.*;

/**
 * @author 作者: 廉建林
 * @version 创建时间：2018年8月10日 上午9:13:00
 * 类说明  读取工具类
 */
public class ReadUtil {
    private static final Logger logger = LogManager.getLogger(ReadUtil.class);

    public static List readCvs(String fileName) {
        List enterPriseNameList = new ArrayList();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));//换成你的文件名
            reader.readLine();//第一行信息，为标题信息，不用,如果需要，注释掉
            String line = null;
            while ((line = reader.readLine()) != null) {
                String item[] = line.split(",");//CSV格式文件为逗号分隔符文件，这里根据逗号切分
                String last = item[item.length - 1];//这就是你要的数据了
                last = last.replaceAll("\"", "");
                logger.info(last);
                enterPriseNameList.add(last);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return enterPriseNameList;

    }

    /*
     * 读取properties
     *
     */
    public static List readPro(String fileName) {
        List valueList = new ArrayList();
        try {
            Properties properties = new Properties();
            // 使用InPutStream流读取properties文件
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
            properties.load(bufferedReader);
            Enumeration en = properties.propertyNames();
            while (en.hasMoreElements()) {
                String key = (String) en.nextElement();
                String value = properties.getProperty(key);
                //logger.info(key + " : " + value);
                valueList.add(value);
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return valueList;
    }
    /*
     * 讀取xml文件
     */

    public static List readXml(String filename) {
        List list = new ArrayList();
        long lasting = System.currentTimeMillis();
        try {
            File f = new File(filename);
            SAXReader reader = new SAXReader();
            Document doc = reader.read(f);
            Element root = doc.getRootElement();
            Element foo;
            for (Iterator i = root.elementIterator("service"); i.hasNext(); ) {
                foo = (Element) i.next();
                String value = foo.attributeValue("enable");
                if (value.equals("true")) {
                    logger.info(foo.getText());
                    list.add(foo.getText());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void main(String[] args) {

        //readCvs("AccountInfo.csv");
        //readPro("src/main/resources/task.properties");
        readXml("src/main/resources/services.xml");
    }

}





