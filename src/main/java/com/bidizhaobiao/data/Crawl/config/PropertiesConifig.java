package com.bidizhaobiao.data.Crawl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @author 作者: 廉建林
 * @version 创建时间：2018年8月9日 上午11:07:54 类说明
 * 数据源配置
 */
@Configuration
public class PropertiesConifig {

    @Bean("druidPoolProps")
    /**
     * @author 作者: 廉建林
     * @version 创建时间：2018年8月9日 上午11:07:54 类说明
     * 数据源配置
     */
    @ConfigurationProperties(prefix = "spring.datasource.druid")
    public Properties druidPoolProps() {
        return new Properties();
    }

    @Bean("oracleConnProps")
    /**
     * @author 作者: 廉建林
     * @version 创建时间：2018年8月9日 上午11:07:54 类说明
     * 数据源配置
     */
    @ConfigurationProperties(prefix = "spring.datasource.oracle")
    public Properties oracleConnProps() {
        return new Properties();
    }

    @Bean("mongoConnProps")
    /**
     * @author 作者: 廉建林
     * @version 创建时间：2018年8月9日 上午11:07:54 类说明
     * 数据源配置
     */
    @ConfigurationProperties("spring.datasource.mongodb")
    public Properties mongoConnProps() {
        return new Properties();
    }

    /**
     * @author 作者: 杨维阵
     * @version 创建时间：2022年3月22日
     * Redis配置
     */
    @Bean("redisConnProps")
    @ConfigurationProperties("spring.redis")
    public Properties redisConnProps() {
        return new Properties();
    }
}
