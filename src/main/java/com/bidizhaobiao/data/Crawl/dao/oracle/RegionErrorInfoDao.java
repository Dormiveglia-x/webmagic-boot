package com.bidizhaobiao.data.Crawl.dao.oracle;

import com.bidizhaobiao.data.Crawl.entity.oracle.RegionErrorInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

/**
 * @author 作者: 陆浩锐
 * @version 创建时间：2022年11月29日 类说明 地区信息操作类
 */
@Service
public interface RegionErrorInfoDao extends JpaRepository<RegionErrorInfo, String> {

}




