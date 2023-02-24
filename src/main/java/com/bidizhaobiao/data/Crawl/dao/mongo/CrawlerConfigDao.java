package com.bidizhaobiao.data.Crawl.dao.mongo;

import com.bidizhaobiao.data.Crawl.entity.mongo.CrawlerConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author 作者: 廉建林
 * @version 创建时间：2018年8月9日 上午11:07:54
 * 类说明 mongo config数据库操作类
 */
@Repository
public interface CrawlerConfigDao extends MongoRepository<CrawlerConfig, String> {
    //根据类名返回一个集合
    List<CrawlerConfig> findByCrawlClassIn(List<String> list);

    CrawlerConfig findByCrawlClass(String className);


}
