package com.bidizhaobiao.data.Crawl.utils;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 作者: 廉建林
 * @version 创建时间：2018年8月10日 下午3:52:51
 * 类说明
 */
public class SpecialUtil {
    private static final Logger logger = LogManager.getLogger(SpecialUtil.class);
    /**
     * 清洗HTML格式
     * <p>
     * <ol>
     * <li>将隐藏（display:none）的元素删除</div>
     * <li>清洗各标签的id、style、class、border、align属性</li>
     * <li>清洗HTML中的style标签</li>
     * <li>清洗HTML中的评论节点</li>
     * <li>将HTML中的h1到h6标签替换为div标签</li>
     * </ol>
     *
     * @param html
     * 待清洗的HTML文本
     * @return 清洗后的HTML文本
     */
    private static final List<String> NON_UNWRAPABLE_ElEMENT_TAG_NAMES = ImmutableList.of("a", "address", "article", "aside", "audio", "blockquote", "br", "canvas", "dd", "div", "dl", "dt",
            "fieldset", "figcaption", "figure", "footer", "form", "h1", "h2", "h3", "h4", "h5", "h6", "header", "hgroup", "hr", "iframe", "img", "input", "li", "noscript", "ol", "output", "p", "pre",
            "script", "section", "style", "table", "tbody", "td", "tfoot", "th", "tr", "ul", "video");
    // 以下对象用于识别接口URL的IP地址
    public static Pattern ipPattern = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
    // 以下对象用于匹配非空标题
    public static Pattern legalTitlePattern = Pattern.compile("[\u4e00-\u9fa5A-Za-z0-9]+");
    // 以下对象用于检测标题是否含有Html标签等非法字符
    public static Pattern illegalTitlePattern = Pattern.compile("<\\s*?[a-zA-Z]+[^>]*>", Pattern.CASE_INSENSITIVE);
    // 以下对象用于检测公告内容是否含有汉字
    public static Pattern legalContentPattern = Pattern.compile("[\u4e00-\u9fa5]+");
    // 以下对象用于检测公告内容是否含有敏感字符
    public static Pattern illegalContentPattern = Pattern.compile("(页面|网络|请求|返回|链接|下载)(出错|错误)|找不到文件|404NotFound", Pattern.CASE_INSENSITIVE);
    // 以下对象用于检测公告发布日期是否采用“标准ISO-8601”日期表示法
    public static Pattern legalDatePattern = Pattern.compile("20\\d{2}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])");
    // 以下对象用于匹配文本中的生僻字符
    public static Pattern infrequentTextPattern = Pattern.compile("[\u2e80-\u2fff\u3040-\u4dff\u9fa6-\u9fff]+");
    // 以下对象用于匹配文本中的不可读乱码字符
    public static Pattern unreadableTextPattern = Pattern.compile("[\ufffd?？]+");
    public static TimeBasedGenerator uuidGenerator = Generators.timeBasedGenerator(EthernetAddress.fromInterface());
    // 以下对象用于匹配文本中的所有双字节字符
    public static Pattern nonASCIIPattern = Pattern.compile("[^\\x00-\\xff]+");
    // 以下对象用于处理日期与字符串的转换关系
    private static ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };
    //以下对象用于处理日期与字符串之间的关系
    private static ThreadLocal<SimpleDateFormat> sdf1 = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            // TODO Auto-generated method stub
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    /**
     * Date to String 转换函数
     *
     * @param date 待转换的日期对象
     * @return date的ISO 8601字符串表示
     */
    public static String date2Str(Date date) {
        return sdf.get().format(date);
    }

    /**
     * Date to String 转换函数
     *
     * @param date 待转换的日期对象
     * @return date的ISO 8601字符串表示
     */
    public static String date2StrContainHour(Date date) {
        return sdf1.get().format(date);
    }

    /**
     * String to Date 转换函数
     *
     * @param str 待转换的日期字符串对象
     * @return str所示时间对应的Date对象
     */
    public static Date str2Date(String str) throws ParseException {
        return sdf.get().parse(str);
    }

    /**
     * 計算時間差（月份）
     *
     * @param
     * @return str所示时间对应的Date对象
     */
    public static String str2(int monthNum) throws ParseException {
        Date dNow = new Date();   //当前时间
        Date dBefore = new Date();
        Calendar calendar = Calendar.getInstance(); //得到日历
        calendar.setTime(dNow);//把当前时间赋给日历
        calendar.add(Calendar.MONTH, monthNum);  //设置为前3月
        dBefore = calendar.getTime();   //得到前3月的时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); //设置时间格式
        String defaultStartDate = sdf.format(dBefore);    //格式化前3月的时间
        String defaultEndDate = sdf.format(dNow); //格式化当前时间

        return defaultStartDate;
    }

    /*
     * 根据指定时间计算时间差(小时)
     */
    public static String addTime(String oldTime, int hour) {
        String afterTime = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date oldDate = sdf.parse(oldTime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(oldDate);
            calendar.add(Calendar.HOUR_OF_DAY, hour);
            afterTime = sdf.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return afterTime;
    }

    /*
     * 根据指定时间计算时间差(小时)
     */
    public static String UpdateTime(String oldTime, double time) {
        String dateString = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date oldDate = sdf.parse(oldTime);
            long newTime = (long) (oldDate.getTime() + time);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            dateString = formatter.format(newTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateString;
    }

    //将yyyy-MM-dd HH:mm:ss转换成GMT格式的时间格式
    public static String changeGmtTimeToDateTime(Date date) {
        SimpleDateFormat sf = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss z", Locale.ENGLISH);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateTime = "";
        try {
            if (!StringUtils.isEmptyIfRgdlssSpace(date.toString())) {
                String time = date2StrContainHour(date);
                Date date1 = sdf.parse(time);
                dateTime = sf.format(date1);
                String text = URLEncoder.encode("中国标准时间", "utf8");
                dateTime = dateTime.replaceAll("CST", "GMT+0800 (" + text + ")");
                dateTime = dateTime.replaceAll(" ", "%20");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateTime;
    }

    public static Date getBeforeOrAfterDate(Date date, int num) {
        Calendar calendar = Calendar.getInstance();//获取日历
        calendar.setTime(date);//当date的值是当前时间，则可以不用写这段代码。
        calendar.add(Calendar.DATE, num);
        Date d = calendar.getTime();//把日历转换为Date
        return d;
    }


    public static void main(String[] args) {
        String st = changeGmtTimeToDateTime(new Date());
        logger.info(st);
    }

    /**
     * 生成一个新的版本号为1的UUID
     *
     * @return
     */
    public static UUID getNewUUID() {
        UUID uuid = uuidGenerator.generate();
        return uuid;
    }

    /**
     * 获取java.lang.Exception对象的异常信息跟踪栈中记录的内容
     *
     * @param exception 异常对象
     * @return 异常对象异常信息跟踪栈中的内容
     */
    public static String getInfoOfExceptionStackTrace(Exception exception) {
        String info = "";
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            info = "\n" + sw.toString() + "\n";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    public static String cleanHtml(String html) {
        html = html.replace("&amp;", "&").replace("&ensp;", " ");
        Document document = Jsoup.parseBodyFragment(html);
        document.outputSettings().prettyPrint(true);

        // region 用div包裹整段HTML文本
        Element bodyEle = document.body();
        bodyEle.html("<div>" + bodyEle.html() + "</div>");
        // endregion

        Elements eles = bodyEle.select("*");
        // region 删除指定的属性
        eles.removeAttr("id");
        eles.removeAttr("class");
        eles.removeAttr("border");
        eles.removeAttr("align");
        eles.removeAttr("valign");
        eles.removeAttr("cellpadding");
        eles.removeAttr("cellspacing");
        eles.removeAttr("bgcolor");
        eles.removeAttr("bordercolor");
        // endregion
        //处理display
        Elements removeElemets = bodyEle.select("*[style~=^.*display\\s*:\\s*none\\s*(;\\s*[0-9A-Za-z]+|;\\s*)?$]");
        for (Element element : removeElemets) {
            if (!element.outerHtml().contains("bidizhaobiao")) {
                element.remove();
            }
        }
        // 非display
        Elements removeElemets1 = bodyEle.getElementsByAttributeValueNot("style", "display:none");
        for (Element element : removeElemets1) {
            if ((!element.attr("href").contains("bidizhaobiao")) || (!element.attr("src").contains("bidizhaobiao"))) {
                element.removeAttr("style");
            }
        }

        // region 删除script标签元素
        bodyEle.select("script").remove();
        // endregion
        Elements inputs = bodyEle.select("input");
        for (Element input : inputs) {
            String value = input.attr("value");
            if (value != null) {
                if (value.length() > 100) {
                    input.remove();
                }
            }
        }

        // region 删除style标签元素
        bodyEle.select("style").remove();
        // endregion

        // region 将h1~h6节点替换为div
        bodyEle.select("h1").tagName("div");
        bodyEle.select("h2").tagName("div");
        bodyEle.select("h3").tagName("div");
        bodyEle.select("h4").tagName("div");
        bodyEle.select("h5").tagName("div");
        bodyEle.select("h6").tagName("div");
        // endregion

        // region 将textarea标签替换为table
        bodyEle.select("textarea").tagName("td").wrap("<table><tbody><tr></tr></tbody></table>");
        // endregion

        // region 删除注释节点
        removeHtmlComments(bodyEle);
        // endregion

        //region 删掉多余的链接

        eles.removeAttr("background");
        eles.removeAttr("action");
        // endregion

        // region 删除link标签元素
        bodyEle.select("link").remove();

        // region 删除多重嵌套的标签
        boolean hasChanged;
        do {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            hasChanged = false;
            Elements allEles = bodyEle.children().select("*");
            for (Element ele : allEles) {
                Element parent = ele.parent();
                if (parent != null && !ele.hasAttr("src") && !ele.hasAttr("href") && (!NON_UNWRAPABLE_ElEMENT_TAG_NAMES.contains(ele.tagName()))
                        && StringUtils.isEmpty(ele.ownText())) {
                    ele.unwrap();
                    hasChanged = true;
                }
            }
        } while (hasChanged);
        // endregion

        //去掉td中的不换行标签 nowrap
        Elements elements = bodyEle.getElementsByTag("td");
        for (Element td : elements) {
            if (td.hasAttr("nowrap")) {
                td.removeAttr("nowrap");
            }
        }
        //替换不换行标签 nobr
        bodyEle.select("nobr").tagName("p");
        bodyEle.select("a").attr("rel", "noreferrer");
        bodyEle.select("img").attr("rel", "noreferrer");
        //替换不标准的标签
        String resultHtml = bodyEle.html();
        resultHtml = resultHtml.replace("&gt;", ">").replace("&lt;", "<");
        resultHtml = resultHtml.replaceAll("\ufeff|\u2002|\u200b|\u2003", " ");
        resultHtml = resultHtml.replace("？", "").replace("??", "").replace("? ?", "");
        return resultHtml;
    }

    /**
     * 实现递归删除指定节点及其后代节点中的所有评论节点
     *
     * @param parentNode
     */
    public static void removeHtmlComments(Node parentNode) {
        for (int i = 0; i < parentNode.childNodeSize(); i++) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            Node childNode = parentNode.childNode(i);
            if (childNode instanceof Comment) {
                childNode.remove();
            } else {
                removeHtmlComments(childNode);
            }
        }
    }


    /**
     * 根据传入的表名父类类名得到DOCCHANNEL值
     *
     * @param serviceName
     *            service父类类名
     * @return docChannel值
     */
    /**
     * 根据传入的service父类类名得到DOCCHANNEL值
     *
     * @param serviceName service父类类名
     * @return docChannel值
     */
    public static int calcDocChannel(String serviceName) {
        int docChannel = -1;
        switch (serviceName) {
            case "ZhaobGgService":
                docChannel = 52;
                break;
            case "ZhongbXxService":
                docChannel = 101;
                break;
            case "GonggBgService":
                docChannel = 51;
                break;
            case "ZhaobYgService":
                docChannel = 102;
                break;
            case "ZhaobDyService":
                docChannel = 103;
                break;
            case "ZhaobWjService":
                docChannel = 104;
                break;
            case "ZisJgService":
                docChannel = 105;
                break;
            case "KongZjService":
                docChannel = 106;
                break;
            case "CaigYxService":
                docChannel = 114;
                break;
            case "PaimCrService":
                docChannel = 115;
                break;
            case "TudKcService":
                docChannel = 116;
                break;
            case "ChanqJyService":
                docChannel = 117;
                break;
            default:
                break;
        }
        return docChannel;
    }

    public static String getTableName(String serviceName) {
        String tableName = "t_zhao_biao_gong_gao";
        String shenPiTableName = "T_SHEN_PI_XIANG_MU_NEW";
        switch (serviceName) {
            case "ZhaobGgService":
                tableName = "t_zhao_biao_gong_gao";
                break;
            case "ZhongbXxService":
                tableName = "t_zhong_biao_xin_xi";
                break;
            case "GonggBgService":
                tableName = "t_gong_gao_bian_geng";
                break;
            case "ZhaobYgService":
                tableName = "t_zhao_biao_yu_gao";
                break;
            case "ZhaobDyService":
                tableName = "t_zhao_biao_da_yi";
                break;
            case "ZhaobWjService":
                tableName = "t_zhao_biao_wen_jian";
                break;
            case "ZisJgService":
                tableName = "t_zi_shen_jie_guo";
                break;
            case "KongZjService":
                tableName = "t_kong_zhi_jia";
                break;
            //采购意向
            case "CaigYxService":
                tableName = "t_cai_gou_yi_xiang";
                break;
            //拍卖出让
            case "PaimCrService":
                tableName = "t_pai_mai_chu_rang";
                break;
            //土地矿产
            case "TudKcService":
                tableName = "t_tu_di_kuang_chan";
                break;
            //产权交易
            case "ChanqJyService":
                tableName = "t_chan_quan_jiao_yi";
                break;

            //审批
            //审批公示
            case "ShenpGsService":
                tableName=shenPiTableName;
                break;
            //审批结果
            case "ShenpJgService":
                tableName=shenPiTableName;
                break;
            //核准公示
            case "HezGsService":
                tableName=shenPiTableName;
                break;
            //核准结果
            case "HezJgService":
                tableName=shenPiTableName;
                break;
            //备案公示
            case "BeiaGsService":
                tableName=shenPiTableName;
                break;
            //备案结果
            case "BeiaJgService":
                tableName=shenPiTableName;
                break;
            //推介项目
            case "TuijXmService":
                tableName=shenPiTableName;
                break;
            //推介结果
            case "TuijJgService":
                tableName=shenPiTableName;
                break;
            //项目撤销
            case "XiangmCxService":
                tableName=shenPiTableName;
                break;
            //筹备阶段
            case "ChoubJdService":
                tableName=shenPiTableName;
                break;

            //失信
            case "WeifJlService":
                tableName = "t_wei_fa_ji_lu";
                break;
            case "TousClService":
                tableName = "t_tou_su_chu_li";
                break;
            case "QitSxService":
                tableName = "t_qi_ta_shi_xin";
                break;
            default:
                break;
        }
        return tableName;
    }

    public static int getSyTypeByServiceName(String serviceName) {
        int sptype = 2;
        switch (serviceName) {
            case "ShenpGsService":
                sptype = 2;
                break;
            case "ShenpJgService":
                sptype = 4;
                break;
            case "HezGsService":
                sptype = 8;
                break;
            case "HezJgService":
                sptype = 16;
                break;
            case "BeiaGsService":
                sptype = 32;
                break;
            case "BeiaJgService":
                sptype = 64;
                break;
            case "TuijXmService":
                sptype = 128;
                break;
            case "TuijJgService":
                sptype = 256;
                break;
            case "XiangmCxService":
                sptype = 512;
                break;
            case "ChoubJdService":
                sptype = 1024;
                break;
            default:
                break;
        }
        return sptype;
    }

    public static String getType(String serviceName) {
        String type = "t_zhao_biao_gong_gao";
        switch (serviceName) {
            case "ZhaobGgService":
                type = "招标公告";
                break;
            case "ZhongbXxService":
                type = "中标信息";
                break;
            case "GonggBgService":
                type = "公告变更";
                break;
            case "ZhaobYgService":
                type = "招标预告";
                break;
            case "ZhaobDyService":
                type = "招标答疑";
                break;
            case "ZhaobWjService":
                type = "招标文件";
                break;
            case "ZisJgService":
                type = "资审结果";
                break;
            case "KongZjService":
                type = "控制价";
                break;
            case "CaigYxService":
                type = "采购意向";
                break;
            //拍卖出让
            case "PaimCrService":
                type = "拍卖出让";
                break;
            //土地矿产
            case "TudKcService":
                type = "土地矿产";
                break;
            //产权交易
            case "ChanqJyService":
                type = "产权交易";
                break;
            default:
                break;
        }
        return type;
    }

    public static boolean isTextUnreadable(String text, UnreadableTextValidationMode validationMode) {
        boolean isUnreadable = false;

        if (text != null && !text.isEmpty()) {
            if (validationMode == null) {
                validationMode = UnreadableTextValidationMode.Loose;
            }

            text = text.replaceAll("[\u00a0\u1680\u180e\u2000-\u200a\u2028\u2029\u202f\u205f\u3000\ufeff\\s]+", "");
            StringBuilder nonASCIIBuilder = new StringBuilder();
            Matcher nonASCIIMatcher = nonASCIIPattern.matcher(text);
            while (nonASCIIMatcher.find()) {
                nonASCIIBuilder.append(nonASCIIMatcher.group());
            }
            String nonASCIIText = nonASCIIBuilder.toString();

            if (validationMode == UnreadableTextValidationMode.Harsh) {
                if (text.matches("^.*?[?？]{2,}.*$")) {
                    isUnreadable = true;
                } else if (nonASCIIText.contains("\ufffd")) {
                    isUnreadable = true;
                }
            }
            if (!isUnreadable) {
                StringBuilder unreadableTextBuilder = new StringBuilder();
                Matcher unreadableTextMatcher = unreadableTextPattern.matcher(text);
                while (unreadableTextMatcher.find()) {
                    unreadableTextBuilder.append(unreadableTextMatcher.group());
                }
                String unreadableText = unreadableTextBuilder.toString();

                if (unreadableText.length() - 0.5 * nonASCIIText.length() >= 0.001) {
                    isUnreadable = true;
                }
            }
            if (!isUnreadable) {
                StringBuilder infrequentTextBuilder = new StringBuilder();
                Matcher infrequentTextMatcher = infrequentTextPattern.matcher(nonASCIIText);
                while (infrequentTextMatcher.find()) {
                    infrequentTextBuilder.append(infrequentTextMatcher.group());
                }
                String infrequentText = infrequentTextBuilder.toString();

                if (infrequentText.length() - 0.5 * nonASCIIText.length() >= 0.001) {
                    isUnreadable = true;
                }
            }
        }
        return isUnreadable;
    }

    /*
     * 通过正则获取一条连接上的域名信息
     */
    public static String getHostByUrl(String url) {
        Pattern pattern = Pattern.compile("^(http://|https://)(.*?)([0-9])*/");
        Matcher matcher = pattern.matcher(url);
        String host = "";
        if (matcher.find()) {
            host = matcher.group(0);
        }
        host = host.substring(0, host.length() - 1);
        return host;
    }

    /**
     * @author jianlin
     * @date 2018年3月5日 下午2:02:40
     * @verison 获取详细的异常信息
     */
    public static String getErrorInfoFromException(Exception e) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return "\r\n" + sw.toString() + "\r\n";
        } catch (Exception e2) {
            return "bad getErrorInfoFromException";
        }
    }

    public static String stringMd5(String input) {
        try {
            //拿到一个MD5转换器（如果想要SHA1加密参数换成"SHA1"）
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            //输入的字符串转换成字节数组
            byte[] inputByteArray = input.getBytes();
            //inputByteArray是输入字符串转换得到的字节数组
            messageDigest.update(inputByteArray);
            //转换并返回结果，也是字节数组，包含16个元素
            byte[] resultByteArray = messageDigest.digest();
            //字符数组转换成字符串返回
            return byteArrayToHex(resultByteArray);


        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static String stringSHA1(String input) {
        try {
            //拿到一个MD5转换器（如果想要SHA1加密参数换成"SHA1"）
            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            //输入的字符串转换成字节数组
            byte[] inputByteArray = input.getBytes();
            //inputByteArray是输入字符串转换得到的字节数组
            messageDigest.update(inputByteArray);
            //转换并返回结果，也是字节数组，包含16个元素
            byte[] resultByteArray = messageDigest.digest();
            //字符数组转换成字符串返回
            return byteArrayToHex(resultByteArray);


        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }


    public static String stringSHA256(String input) {
        MessageDigest messageDigest;
        String encodeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(input.getBytes(StandardCharsets.UTF_8));
            encodeStr = byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return encodeStr;
    }

    private static String byte2Hex(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        String temp;
        for (byte aByte : bytes) {
            temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1) {
                //1得到一位的进行补0操作
                stringBuilder.append("0");
            }
            stringBuilder.append(temp);
        }
        return stringBuilder.toString();
    }

    public static String stringMd5(byte[] inputByteArray) {
        try {
            //拿到一个MD5转换器（如果想要SHA1加密参数换成"SHA1"）
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            //输入的字符串转换成字节数组
            //byte[] inputByteArray = input.getBytes();
            //inputByteArray是输入字符串转换得到的字节数组
            messageDigest.update(inputByteArray);
            //转换并返回结果，也是字节数组，包含16个元素
            byte[] resultByteArray = messageDigest.digest();
            //字符数组转换成字符串返回
            return byteArrayToHex(resultByteArray);


        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static String byteArrayToHex(byte[] byteArray) {
        //首先初始化一个字符数组，用来存放每个16进制字符
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        //new一个字符数组，这个就是用来组成结果字符串的（解释一下：一个byte是八位二进制，也就是2位十六进制字符）
        char[] resultCharArray = new char[byteArray.length * 2];
        //遍历字节数组，通过位运算（位运算效率高），转换成字符放到字符数组中去
        int index = 0;
        for (byte b : byteArray) {
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }

        //字符数组组合成字符串返回
        return new String(resultCharArray);
    }

    /**
     * 判定指定文本中是否含有乱码
     * <p>
     * <p>
     * 若文本中是否含有连续两个或以上的问号，或者文本中生僻字的长度满足“占其所有双字节符长度的二分之一或以上”，则判定文本中存在乱码字符.
     * </p>
     * <p>
     * 判定依据：
     * </p>
     * <ol>
     * <li>当从Unicode编码向某个字符集转换时，如果在该字符集中没有对应的编码，则得到0x3f（即英文问号字符“?”）</li>
     * <li>从其他字符集向Unicode编码转换时，如果这个二进制数在该字符集中没有标识任何的字符，则得到的结果是0xfffd</li>
     * <li>汉字生僻字的Unicode值范围为[\u2e80-\u4dff\u9fa6-\u9fff]</li>
     * </ol>
     * <p>
     * <p>
     * 参考资料：
     * </p>
     * <ol>
     * <li>https://gist.github.com/shingchi/64c04e0dd2cbbfbc1350</li>
     * <li>http://www.unicode.org/charts/PDF/</li>
     * </ol>
     *
     * @return 若指定文本含有乱码字符，则返回true，否则返回false
     */
    public static enum UnreadableTextValidationMode {
        Harsh,
        Loose
    }
}





