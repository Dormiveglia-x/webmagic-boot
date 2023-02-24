package com.bidizhaobiao.data.Crawl.utils;

import java.io.*;
import java.util.Scanner;

public class WriteUtils {
    /*
     *  记录数据信息
     */
    public static void write(String text, String fileName) {
        FileWriter fw;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            fw = new FileWriter(fileName, true);
            BufferedWriter bw = new BufferedWriter(fw);
            try {
                bw.write(text);// 往已有的文件上添加字符串
                bw.write("\n ");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (bw != null) {
                    try {
                        bw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fw != null) {
                    try {
                        fw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     *  删除数据信息
     */
    public static void delete(String text, String fileName) {
        try {
            // TODO Auto-generated method stub
            File filesource = new File(fileName);
            Scanner input = new Scanner(filesource);
            StringBuffer temp = new StringBuffer();
            while (input.hasNext()) {
                String tem = input.nextLine();
                temp.append(tem.replace(text, "") + "\r\n");
            }
            PrintWriter output = new PrintWriter(filesource);
            output.print(temp);
            input.close();
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean findStringInFile(String path, String className) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        InputStreamReader read = new InputStreamReader(new FileInputStream(file), "UTF-8");//考虑到编码格式
        BufferedReader bufferedReader = new BufferedReader(read);
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("#")) {
                continue;
            }
            //指定字符串判断处
            if (line.contains(className)) {
                return true;
            }
        }
        return false;
    }

}
