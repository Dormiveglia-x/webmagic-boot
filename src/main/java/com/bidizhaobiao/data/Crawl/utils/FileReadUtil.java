package com.bidizhaobiao.data.Crawl.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: 廉建林
 * @Date: 2019/9/5 11:10
 * @Version 1.0
 * 读取impl文件夹下的文件，准备入库
 */
public class FileReadUtil {
    private static final Logger logger = LogManager.getLogger(FileReadUtil.class);
    public static List list = new ArrayList();

    public List selectConfig() {
        try {
            list.clear();
            String base = Thread.currentThread().getContextClassLoader().getResource("").getPath();
            base = base + "com/bidizhaobiao/data/Crawl/service/impl";
            base = base.replaceAll("%20", " ");
            String packageName = "";
            File root = new File(base);
            list = loopSelect(root, packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List loopSelect(File folder, String packageName) {
        File[] files = folder.listFiles();
        for (int fileIndex = 0; fileIndex < files.length; fileIndex++) {
            try {
                File file = files[fileIndex];
                String fileName = file.getName();
                if (file.isDirectory()) {
                    loopSelect(file, packageName + file.getName() + ".");
                } else {
                    fileName = fileName.substring(0, fileName.lastIndexOf("."));
                    if (fileName.contains("$")) {
                        continue;
                    }
                    list.add(fileName);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (e.getMessage().contains("No bean named")) {
                    continue;
                }
            }
        }
        return list;
    }


}
