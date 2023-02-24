package com.bidizhaobiao.data.Crawl.dao.mongo;

import com.bidizhaobiao.data.Crawl.entity.mongo.CrawlerTimerLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author 作者: 廉建林
 * @version 创建时间：2018年8月9日 上午11:07:54
 * 类说明 mongo CrawlerTimerLog数据库操作类
 */
@Repository
public interface CrawlerTimerLogDao extends MongoRepository<CrawlerTimerLog, String> {


}
