package com.bidizhaobiao.data.Crawl.dao.oracle;

import com.bidizhaobiao.data.Crawl.entity.oracle.PosOfProclamation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author 作者: 廉建林
 * @version 创建时间：2018年8月9日 上午11:07:54 类说明 广联达公告数据库操作类
 */
@Service
public class AreaDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<PosOfProclamation> findByDistrict(String district) {
        String sql = "select REGION,PROVINCE,CITY,DISTRICT from BXKC_DATA_MONITOR.\"SYS_AREA_old\" where DISTRICT = ?";
        return jdbcTemplate.query(sql, new AreaRowMapper(), district);
    }

    public List<PosOfProclamation> findByDistrictAndCity(String district, String City) {
        String sql = "select REGION,PROVINCE,CITY,DISTRICT from BXKC_DATA_MONITOR.\"SYS_AREA_old\" where DISTRICT = ? and CITY = ?";
        return jdbcTemplate.query(sql, new AreaRowMapper(), district, City);
    }

    public List<PosOfProclamation> findByCity(String city) {
        String sql = "select REGION,PROVINCE,CITY,DISTRICT from BXKC_DATA_MONITOR.\"SYS_AREA_old\" where CITY = ? and DISTRICT = '无'";
        return jdbcTemplate.query(sql, new AreaRowMapper(), city);
    }

    public List<PosOfProclamation> findByProvince(String province) {
        String sql = "select REGION,PROVINCE,CITY,DISTRICT from BXKC_DATA_MONITOR.\"SYS_AREA_old\" where PROVINCE = ? and DISTRICT = '无' and CITY = '无'";
        return jdbcTemplate.query(sql, new AreaRowMapper(), province);
    }

    class AreaRowMapper implements RowMapper<PosOfProclamation> {
        // rs为返回结果集，以每行为单位封装着
        public PosOfProclamation mapRow(ResultSet rs, int rowNum) throws SQLException {
            // TODO Auto-generated method stub
            PosOfProclamation posOfProclamation = new PosOfProclamation();
            posOfProclamation.setArea(!rs.getString("REGION").equals("无") ? rs.getString("REGION") : null);
            posOfProclamation.setProvince(!rs.getString("PROVINCE").equals("无") ? rs.getString("PROVINCE") : null);
            posOfProclamation.setCity(!rs.getString("CITY").equals("无") ? rs.getString("CITY") : null);
            posOfProclamation.setDistrict(!rs.getString("DISTRICT").equals("无") ? rs.getString("DISTRICT") : null);
            return posOfProclamation;
        }
    }


}
