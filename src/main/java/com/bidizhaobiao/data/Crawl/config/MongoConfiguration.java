package com.bidizhaobiao.data.Crawl.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.integration.transaction.PseudoTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Properties;

/**
 * @author 作者: 廉建林
 * @version 创建时间：2018年8月9日 上午11:07:54 类说明
 * 配置mongo数据源
 */
@Configuration
@EnableMongoRepositories(basePackages = "com.bidizhaobiao.data.Crawl.dao.mongo", mongoTemplateRef = "mongoTemplate")
public class MongoConfiguration {


    @Autowired
    @Qualifier("mongoConnProps")
    private Properties mongodbConnProps;

    @Primary
    @Bean(name = "mongoTemplate")
    public MongoTemplate mongoTemplate() throws Exception {
        MongoCredential mongoCredential = mongoCredential();
        MongoClient mongoClient = mongoClient();
        return new MongoTemplate(mongoClient, mongodbConnProps.getProperty("dbName"));
    }

    @Bean(name = "mongoCredential")
    public MongoCredential mongoCredential() {
        MongoCredential credential = MongoCredential.createScramSha1Credential(
                mongodbConnProps.getProperty("username"),
                mongodbConnProps.getProperty("dbName"),
                mongodbConnProps.getProperty("password").toCharArray()
        );
        return credential;
    }

    @Bean(name = "mongoClientOptions")
    public MongoClientOptions mongoClientOptions() {
        MongoClientOptions options = MongoClientOptions.builder()
                .minConnectionsPerHost(Integer.parseInt(mongodbConnProps.getProperty("minConnectionsPerHost")))
                .connectionsPerHost(Integer.parseInt(mongodbConnProps.getProperty("connectionsPerHost")))
                .threadsAllowedToBlockForConnectionMultiplier(
                        Integer.parseInt(mongodbConnProps.getProperty("threadsAllowedToBlockForConnectionMultiplier"))
                )
                .maxWaitTime(Integer.parseInt(mongodbConnProps.getProperty("maxWaitTime")))
                .connectTimeout(Integer.parseInt(mongodbConnProps.getProperty("connectTimeout")))
                .socketTimeout(Integer.parseInt(mongodbConnProps.getProperty("socketTimeout")))
                .maxConnectionIdleTime(Integer.parseInt(mongodbConnProps.getProperty("maxConnectionIdleTime")))
                .maxConnectionLifeTime(Integer.parseInt(mongodbConnProps.getProperty("maxConnectionLifeTime")))
                .build();
        return options;
    }

    @Bean(name = "mongoClient")
    public MongoClient mongoClient() {
        ServerAddress serverAddress = new ServerAddress(
                mongodbConnProps.getProperty("host"),
                Integer.parseInt(mongodbConnProps.getProperty("port"))
        );
        List<MongoCredential> credentialList = Lists.newArrayList(mongoCredential());
        MongoClient client = new MongoClient(serverAddress, credentialList, mongoClientOptions());
        return client;
    }

    @Bean
    public PlatformTransactionManager mongoTransactionManager() {
        return new PseudoTransactionManager();
    }
}
