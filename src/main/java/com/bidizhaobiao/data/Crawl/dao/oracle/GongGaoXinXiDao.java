package com.bidizhaobiao.data.Crawl.dao.oracle;

import com.bidizhaobiao.data.Crawl.entity.oracle.GongGaoXinXi;
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
 * @version 创建时间：2018年8月9日 上午11:07:54 类说明 广联达公告数据库操作类
 */
@Service
public class GongGaoXinXiDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 判断公告是不是存在
    public List<GongGaoXinXi> isGongGaoExist(String id) {
        String sql = "select ID from BXKC.GONG_GAO_XIN_XI  where ID =?";
        return jdbcTemplate.query(sql, new GongGaoRowMapper(), id);
    }

    // 根据站源标识判断公告是不是存在
    public List<GongGaoXinXi> isGongGaoExistByTaskType(String taskType) {
        String sql = "select ID from BXKC.GONG_GAO_XIN_XI  where TASK_TYPE =?  and rownum<=2 ";
        return jdbcTemplate.query(sql, new GongGaoRowMapper(), taskType);
    }

    /*
     * 记录入库信息 向单次入库日志表存储日志信息
     */
    public String saveGongGao(final GongGaoXinXi gongGao) {
        // 插入数据
        final String Id = SpecialUtil.getNewUUID().toString();
        // TODO Auto-generated method stub
        String sql = "insert  into BXKC.GONG_GAO_XIN_XI"
                + " (ID,DETAIL_TITLE,DETAIL_HTML,DETAIL_DDID,DOCCHANNEL,TASK_TYPE,TASK_NAME,CREATETIME,DETAIL_LINK,LIST_TITLE,web_source_no,PAGE_TIME) values(?,?,?,?,?,?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql, new PreparedStatementSetter() {
            // 传参
            public void setValues(PreparedStatement pst) throws SQLException {
                Date startTime = new Date();
                pst.setString(1, gongGao.getId());
                pst.setString(2, gongGao.getDetailTitle());
                pst.setString(3, gongGao.getDetailHtml());
                pst.setString(4, gongGao.getDetailDdid());
                pst.setInt(5, gongGao.getDocChannel());
                pst.setString(6, gongGao.getTaskType());
                pst.setString(7, gongGao.getTaskName());
                pst.setTimestamp(8, new java.sql.Timestamp(startTime.getTime()));
                pst.setString(9, gongGao.getDetailLink());
                pst.setString(10, gongGao.getListTitle());
                pst.setString(11, gongGao.getWebResourceNum());
                pst.setString(12, gongGao.getPageTime());
            }
        });
        return Id;
    }

    class GongGaoRowMapper implements RowMapper<GongGaoXinXi> {
        // rs为返回结果集，以每行为单位封装着
        public GongGaoXinXi mapRow(ResultSet rs, int rowNum) throws SQLException {
            // TODO Auto-generated method stub
            GongGaoXinXi gongGao = new GongGaoXinXi();
            gongGao.setId(rs.getString("ID"));
            return gongGao;
        }
    }


}
