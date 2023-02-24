package com.bidizhaobiao.data.Crawl.dao.oracle;

import com.bidizhaobiao.data.Crawl.entity.oracle.PosOfProclamation;
import com.bidizhaobiao.data.Crawl.entity.oracle.Proclamation;
import com.bidizhaobiao.data.Crawl.entity.oracle.RecordVO;
import com.bidizhaobiao.data.Crawl.utils.CheckProclamationUtil;
import com.bidizhaobiao.data.Crawl.utils.SpecialUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * @author 作者: 廉建林
 * @version 创建时间：2018年8月9日 上午11:07:54 类说明 公告数据库操作类
 */
@Service
public class ProclamationDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 判断公告是不是存在
    public List<Proclamation> isProclamationExist(String tablename, String webSourceNo, String pageTime,
                                                  String recordid) {
        // TODO Auto-generated method stub and PHONE_NO!=null or MOBILE_NO=!null
        String sql = "select ID from BXKC." + tablename + "  where WEB_SOURCE_NO =? and PAGE_TIME >=? and RECORD_ID =?";
        return jdbcTemplate.query(sql, new ProclamationRowMapper(), webSourceNo, pageTime, recordid);
    }

    public final Proclamation createEntity(String sourceNum, String sourceName, String infoSource, RecordVO recordVO) {
        Proclamation zbdy = new Proclamation();

        String area = recordVO.getArea();
        String province = recordVO.getProvince();
        String city = recordVO.getCity();
        String district = recordVO.getDistrict();
        String infoType = recordVO.getInfoType();
        String industry = recordVO.getIndustry();
        if (district != null || (area != null && province != null && city != null && !area.isEmpty()
                && !province.isEmpty() && !city.isEmpty() && !city.equals(province))) {
            zbdy.setArea(area);
            zbdy.setProvince(province);
            zbdy.setCity(city);
            zbdy.setDistrict(district);
        } else {
            /*
             * 设置进行地区信息匹配的初始条件 若区域非空，则设置在指定区域下展开匹配 若省份非空，则设置在指定省份下展开匹配
             */
            PosOfProclamation condition = new PosOfProclamation();
            if (area != null && !area.isEmpty()) {
                condition.setArea(area);
            }
            if (province != null && !province.isEmpty()) {
                condition.setProvince(province);
            }
            if (city != null && !city.isEmpty()) {
                condition.setCity(city);
            }
            /*
             * 根据初始条件，先从标题匹配区域、省份和城市信息 若区域、省份和城市信息均匹配不到，则从内容继续尝试匹配区域、省份和城市信息
             * 若匹配到区域和省份信息分别为area和province，但匹配不到城市信息，则在区域为area且省份为province的条件下，
             * 从内容继续尝试匹配城市信息
             */
            PosOfProclamation p = CheckProclamationUtil.judgePosOfProclamation(condition, recordVO.getTitle());
            if (p == null) {
                p = CheckProclamationUtil.judgePosOfProclamation(condition, recordVO.getContent());
            } else if (p != null && p.getProvince() != null && p.getCity() == null) {
                condition.setArea(p.getArea());
                condition.setProvince(p.getProvince());
                p = CheckProclamationUtil.judgePosOfProclamation(condition, recordVO.getContent());
            }
            p = (p == null ? CheckProclamationUtil.getDefaultPosOfProclamation() : p);
            CheckProclamationUtil.revisePosOfProclamation(p);

            zbdy.setArea(p.getArea());
            zbdy.setProvince(p.getProvince());
            zbdy.setCity(p.getCity());
            zbdy.setDistrict(p.getDistrict());
        }
        zbdy.setInfoType(infoType == null ? CheckProclamationUtil.judgeInfoType(recordVO.getContent()) : infoType);
        zbdy.setIndustry(industry == null ? CheckProclamationUtil.judgeIndustry(recordVO.getContent()) : industry);

        zbdy.setWebSourceNo(sourceNum);
        zbdy.setWebSourceName(sourceName);
        zbdy.setInfoSource(infoSource);
        zbdy.setRecordId(recordVO.getId());
        zbdy.setPageTitle(recordVO.getTitle());
        zbdy.setPageTime(recordVO.getDate());
        zbdy.setPageContent(recordVO.getContent());
        if (recordVO.getAttachment() != null) {
            zbdy.setAttachmentPath(recordVO.getAttachment());
        }
        zbdy.setId(SpecialUtil.getNewUUID().toString());
        zbdy.setCreateTime(new Date());

        return zbdy;
    }
    /*
     * 根据指定字段创建实体
     *
     *
     * protected final ZhaoBiaoDaYi createEntity(String recordId, String title,
     * String date, String content) { ZhaoBiaoDaYi zbdy = new ZhaoBiaoDaYi();
     *
     * if (area == null || province == null || city == null || "".equals(area)
     * || "".equals(province) || "".equals(city)) { PosOfProclamation p =
     * CheckProclamationUtil.judgePosOfProclamation(null, title); if (p == null)
     * { p = getDefaultPosOfProclamation(); } revisePosOfProclamation(p);
     *
     * zbdy.setArea(p.getArea()); zbdy.setProvince(p.getProvince());
     * zbdy.setCity(p.getCity()); zbdy.setDistrict(p.getDistrict()); } else {
     * zbdy.setArea(area); zbdy.setProvince(province); zbdy.setCity(city);
     * zbdy.setDistrict(district); } zbdy.setInfoType(infoType == null ?
     * CheckProclamationUtil.judgeInfoType(content) : infoType);
     * zbdy.setIndustry(industry == null ?
     * CheckProclamationUtil.judgeIndustry(content) : industry);
     *
     * zbdy.setWebSourceNo(sourceNum); zbdy.setWebSourceName(sourceName);
     * zbdy.setInfoSource(infoSource); zbdy.setRecordId(recordId);
     * zbdy.setPageTitle(title); zbdy.setPageTime(date);
     * zbdy.setPageContent(content); zbdy.setPageAttachments(null);
     * zbdy.setId(CheckProclamationUtil.getNewUUID().toString());
     * zbdy.setCreateTime(new Date());
     *
     * return zbdy; }
     */

    /*
     * 根据结果创建一个招标答疑的实体
     */

    /*
     * 记录入库信息 向单次入库日志表存储日志信息
     */
    public String saveProclamation(String tablename, final Proclamation entity) {
        // 插入数据
        String sql = "insert  into BXKC." + tablename
                + " (ID,WEB_SOURCE_NO,AREA,PROVINCE,CITY,WEB_SOURCE_NAME,INFO_SOURCE,INFO_TYPE,INDUSTRY,"
                + "RECORD_ID,PAGE_TITLE,PAGE_TIME,PAGE_ATTACHMENTS,CREATE_TIME,DISTRICT,DETAIL_LINK,Attachment_Path,PAGE_CONTENT) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
//                + "RECORD_ID,PAGE_TITLE,PAGE_TIME,PAGE_ATTACHMENTS,CREATE_TIME,DISTRICT,PAGE_CONTENT,DETAIL_LINK) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql, new PreparedStatementSetter() {
            // 传参
            public void setValues(PreparedStatement pst) throws SQLException {
                Date startTime = new Date();
                pst.setString(1, entity.getId());
                pst.setString(2, entity.getWebSourceNo());
                pst.setString(3, entity.getArea());
                pst.setString(4, entity.getProvince());
                pst.setString(5, entity.getCity());
                pst.setString(6, entity.getWebSourceName());
                pst.setString(7, entity.getInfoSource());
                pst.setString(8, entity.getInfoType());
                pst.setString(9, entity.getIndustry());
                pst.setString(10, entity.getRecordId());
                pst.setString(11, entity.getPageTitle());
                pst.setString(12, entity.getPageTime());
                pst.setString(13, entity.getPageAttachments());
                pst.setTimestamp(14, new java.sql.Timestamp(startTime.getTime()));
                pst.setString(15, entity.getDistrict());
                pst.setString(16, entity.getDetailLink());
                pst.setString(17, entity.getAttachmentPath());
                pst.setString(18, entity.getPageContent());
            }
        });
        return entity.getId();
    }

    class ProclamationRowMapper implements RowMapper<Proclamation> {
        // rs为返回结果集，以每行为单位封装着
        public Proclamation mapRow(ResultSet rs, int rowNum) throws SQLException {
            // TODO Auto-generated method stub
            Proclamation ZhaoBiaoDaYi = new Proclamation();
            ZhaoBiaoDaYi.setId(rs.getString("ID"));
            return ZhaoBiaoDaYi;
        }
    }
}
