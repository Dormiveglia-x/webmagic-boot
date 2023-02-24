package com.bidizhaobiao.data.Crawl.dao.oracle;

import com.bidizhaobiao.data.Crawl.entity.oracle.DownloadInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 作者: 廉建林
 * @version 创建时间：2018年8月9日 上午11:07:54 类说明 附件下载信息操作类
 */
@Service
public interface DownloadInfoDao extends JpaRepository<DownloadInfo, String> {
    List<DownloadInfo> findByOldLinkAndRecordId(String oldLink, String recordId);
}




