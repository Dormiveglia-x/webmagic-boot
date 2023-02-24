package com.bidizhaobiao.data.Crawl.utils;

import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.service.ServiceContext;
import com.bidizhaobiao.data.Crawl.utils.CheckProclamationUtil.RecordStatus;
import com.bidizhaobiao.data.Crawl.utils.SpecialUtil.UnreadableTextValidationMode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.regex.Matcher;

/**
 * @author 作者: 廉建林
 * @version 创建时间：2018年8月9日 上午11:07:54 类说明 :爬虫是否有必要爬取的多种校验过程
 */
@Service
public class CrawlCheckUtils {
    protected static final Logger logger = LoggerFactory.getLogger(CrawlCheckUtils.class);

    /**
     * 校验详情页 校验记录各项信息是否合法
     *
     * @param recordVO 记录值对象 待入库的详情页页
     * @return RecordVO 校验后的结果
     * @throws Exception
     */
    public static RecordVO validateDetail(ServiceContext serviceContext, RecordVO recordVO)
            throws Exception {
        // 请求未入库记录的内容页
        try {
            RecordStatus recordStatus = validateRecord(recordVO);
            if (recordStatus == RecordStatus.HasNullRecordIdException) {
                recordVO.setId("null");
                serviceContext.setHasNullRecordId(true);
                throw new Exception("异常原因说明：公告ID为空！");
            } else if (recordStatus == RecordStatus.HasNullTitleException) {
                recordVO.setTitle("null");
                serviceContext.setHasNullTitle(true);
                throw new Exception("异常原因说明：公告标题为空！");
            } else if (recordStatus == RecordStatus.HasIncompleteTitleException) {
                throw new Exception("异常原因说明：公告标题不完整！");
            } else if (recordStatus == RecordStatus.HasUnreableTitleException) {
                throw new Exception("异常原因说明：公告标题存在乱码字符！");
            } else if (recordStatus == RecordStatus.HasIllegalTitleException) {
                throw new Exception("异常原因说明：公告标题存在Html标签等非法字符！");
            } else if (recordStatus == RecordStatus.HasNullContentException) {
                throw new Exception("异常原因说明：公告内容为空！");
            } else if (recordStatus == RecordStatus.HasNoChineseOrNoElementWithSrcAttrException) {
                throw new Exception("异常原因说明：公告内容不含有任何中文且不包含任何具有合法src属性的iframe元素和img元素！");
            } else if (recordStatus == RecordStatus.HasUnreableContentException) {
                throw new Exception("异常原因说明：公告内容存在乱码字符！");
            } else if (recordStatus == RecordStatus.HasIllegalContentException) {
                throw new Exception("异常原因说明：公告内容含有“页面出错、找不到文件或 404 Not Found”等敏感字符！");
            } else if (recordStatus == RecordStatus.HasDateFormatException) {
                throw new Exception("异常原因说明：公告发布日期格式错误或取值不合理！");
            } else if (recordStatus == RecordStatus.HasIllegalAreaException) {
                throw new Exception("异常原因说明：公告所属地区取值不合理！");
            } else if (recordStatus == RecordStatus.HasOverLengthContentException) {
                logger.warn("公告详情长度超过200KB，请检查！");
            }

        } catch (Exception e) {
            // 登记异常对象中的Message信息
            String message = (e.getMessage() == null ? "" : e.getMessage());
            throw new Exception(message, e);
        }
        // 过滤太旧的数据
        Date publishDate = SpecialUtil.str2Date(recordVO.getDate());
        if (publishDate.before(serviceContext.getTimeSplitPoint())) {
            serviceContext.setHasReachedTooOldData(true);
        }
        // 将发布日期大于当前日期的公告发布日期修改为当前日期
        Date today = new Date();
        if (today.before(publishDate)) {
            recordVO.setDate(SpecialUtil.date2Str(today));
        } else {
            recordVO.setDate(SpecialUtil.date2Str(publishDate));
        }
        return recordVO;
    }


    /**
     * 校验待入库公告记录各项信息的合法性
     *
     * @param recordVO 待入库的实体
     * @return 校验状态
     */
    protected static RecordStatus validateRecord(RecordVO recordVO) {
        // 若在当前列表页中，有一条记录标题为空，则判定本接口存在异常
        RecordStatus recordStatus = RecordStatus.Normal;
        String title = recordVO.getTitle();
        String recordId = recordVO.getId();
        String date = recordVO.getDate();
        String content = recordVO.getContent();
        Document contentDocument = Jsoup.parseBodyFragment(content != null ? content : "");
        String textContent = contentDocument.text()
                .replace("[\u00a0\u1680\u180e\u2000-\u200a\u2028\u2029\u202f\u205f\u3000\ufeff\\s]+", "");
        String area = recordVO.getArea();
        String province = recordVO.getProvince();
        String city = recordVO.getCity();
        String district = recordVO.getDistrict();

        if (recordId == null || (recordId != null && recordId.trim().isEmpty())) {
            recordStatus = RecordStatus.HasNullRecordIdException;
        } else {
            Matcher legalTitleMatcher = SpecialUtil.legalTitlePattern.matcher(title != null ? title : "");
            Matcher illegalTitleMatcher = SpecialUtil.illegalTitlePattern.matcher(title != null ? title : "");
            Matcher legalContentMatcher = SpecialUtil.legalContentPattern
                    .matcher(textContent != null ? textContent : "");
            Matcher illegalContentMatcher = SpecialUtil.illegalContentPattern
                    .matcher(textContent != null ? textContent : "");
            Matcher legalDateMatcher = SpecialUtil.legalDatePattern.matcher(date != null ? date : "");

            // 若公告标题为null、空字符串或不含有任意有意义字符（字母、数字或汉字），则判定存在“公告标题为空”异常
            if (title == null || (title != null && (!legalTitleMatcher.find() || "null".equals(title)))) {
                recordStatus = RecordStatus.HasNullTitleException;
            }
            // 若公告标题以“..”结尾，则判定存在“公告标题不完整”异常
            else if (title.trim().endsWith("..")) {
                recordStatus = RecordStatus.HasIncompleteTitleException;
            }
            // 若公告标题含有� （\ufffd）或过多的?、？或生僻字，则判定存在“公告标题存在乱码”异常
            else if (SpecialUtil.isTextUnreadable(title, UnreadableTextValidationMode.Harsh)) {
                recordStatus = RecordStatus.HasUnreableTitleException;
            }
            // 若公告标题含有Html标签等非法字符，则判定存在“公告标题存在Html标签等非法字符”异常
            else if (illegalTitleMatcher.find()) {
                recordStatus = RecordStatus.HasIllegalTitleException;
            }
            // 若公告Html为null、空字符串或公告Html长度小于10，则判定存在“公告内容为空”异常
            else if (content == null || (content != null && content.length() < 10)) {
                recordStatus = RecordStatus.HasNullContentException;
            }
            // 若公告文本节点内容不含有中文，且公告Html中不含有iframe、img等可加载外置资源的标签，则判定存在“公告内容可能不含有任何有意义内容”异常
            else if (!legalContentMatcher.find() && contentDocument.select("iframe[src^=http]").size() <= 0
                    && contentDocument.select("img[src^=http]").size() <= 0) {
                recordStatus = RecordStatus.HasNoChineseOrNoElementWithSrcAttrException;
            }
            // 若公告文本节点内容含有� （\ufffd）或过多的?、？或生僻字，则判定存在“公告内容存在乱码”异常
            else if (SpecialUtil.isTextUnreadable(textContent, UnreadableTextValidationMode.Loose)) {
                recordStatus = RecordStatus.HasUnreableContentException;
            }
            // 若公告文本节点内容含有“出错”、“找不到文件”或“404 not found”字符，则判定存在“公告内容存在敏感字符”异常
            else if (illegalContentMatcher.find()) {
                recordStatus = RecordStatus.HasIllegalContentException;
            }
            // 若公告发布日期没有采用标准ISO-8601日期表示法，则判定存在”公告发布日期格式错误”异常
            else if (!legalDateMatcher.find()) {
                recordStatus = RecordStatus.HasDateFormatException;
            }
            // 若公告发布地区、省份和城市信息之间不能互相对应，则判定存在“公告所属地区取值不合理”异常
            else if (!CheckProclamationUtil.isPosOfProclamationValidate(area, province, city, district)) {
                recordStatus = RecordStatus.HasIllegalAreaException;
            }
            // 若公告长度过长（在UTF-8编码下超过200KB），则判定存在“公告详情超长”异常
            else {
                try {
                    if (content.getBytes("UTF-8").length / 1000.0 > 200) {
                        recordStatus = RecordStatus.HasOverLengthContentException;
                    }
                } catch (UnsupportedEncodingException e) {
                    // This should never happen!
                    logger.error(SpecialUtil.getInfoOfExceptionStackTrace(e));
                }
            }
        }

        return recordStatus;
    }
	/*public static void main(String[] args) {
		boolean rs=CheckProclamationUtil.isPosOfProclamationValidate("全国", "全国", "全国", "全国");
		logger.info(rs);
	}*/

    /**
     * 入库前对RecordVO的增强处理方法
     *
     * @param recordVO 待增强处理的RecordVO实例
     */
    public static RecordVO refreshRecordVO(RecordVO recordVO) {
        String title = recordVO.getTitle();
        String content = recordVO.getContent();
        String area = recordVO.getArea();
        String province = recordVO.getProvince();
        String city = recordVO.getCity();
        String district = recordVO.getDistrict();
        String infoType = recordVO.getInfoType();
        String industry = recordVO.getIndustry();
        // 删除标题中包含的所有空格字符
        if (title != null) {
            title = Jsoup.parse(title).text();
            recordVO.setTitle(title.replaceAll("[\u00a0\u1680\u180e\u2000-\u200a\u2028\u2029\u202f\u205f\u3000\ufeff\\s]+", ""));
        }
        // 清洗公告文档中的样式以及评论节点
        if (!StringUtils.isEmptyIfRgdlssSpace(content)) {
            content = SpecialUtil.cleanHtml(content);
            recordVO.setContent(content);
        }

        // 修正可能从详情抓取到的地区信息
        if (area != null && !area.isEmpty()) {
            recordVO.setArea(area);
        }
        if (province != null && !province.isEmpty()) {
            recordVO.setProvince(province);
        }
        if (city != null && !city.isEmpty()) {
            recordVO.setCity(city);
        }
        if (district != null && !district.isEmpty()) {
            recordVO.setDistrict(district);
        }

        // 强制删除省份字段的“省”字以及城市字段的“市”字
        if (province != null && !province.isEmpty()) {
            recordVO.setProvince(province.replaceFirst("(.*)省$", "$1"));
        }
        if (city != null && !city.isEmpty()) {
            recordVO.setCity(city.replaceFirst("(.*)市$", "$1"));
        }

        // 修正可能从详情抓取到的信息类型和行业分类信息
        if (infoType != null && !infoType.isEmpty()) {
            recordVO.setInfoType(infoType);
        } else {
            infoType = CheckProclamationUtil.judgeInfoType(content);
            recordVO.setInfoType(infoType);
        }
        if (industry != null && !industry.isEmpty()) {
            recordVO.setIndustry(industry);
        } else {
            industry = CheckProclamationUtil.judgeIndustry(content);
            recordVO.setIndustry(industry);
        }

        return recordVO;
    }


}

