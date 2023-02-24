package com.bidizhaobiao.data.Crawl.dao.mongo;

import com.bidizhaobiao.data.Crawl.entity.mongo.CrawlerLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author 作者: 廉建林
 * @version 创建时间：2018年8月9日 上午11:07:54
 * 类说明 mongo CrawlerLog数据库操作类
 */
@Repository
public interface CrawlerLogDao extends MongoRepository<CrawlerLog, String> {

    //根据公告类名查找
    List<CrawlerLog> findByCrawlClassOrderByCrawlStartTime(String className, Pageable pageable);

    //获取时间段的监控结果
    List<CrawlerLog> findByCrawlClassAndCrawlStartTimeBetween(String crawlClass, String start, String end);
}
