package com.bidizhaobiao.data.Crawl.config;

import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.jcache.config.JCacheConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Properties;

@Configuration
@PropertySource("classpath:application.properties")
public class RedisConfig extends JCacheConfigurerSupport {

    @Autowired
    @Qualifier("redisConnProps")
    private Properties redisConnProps;

    @Bean
    public Redisson redisson(){
        Config config=new Config();
        String host=redisConnProps.getProperty("host");
        String port=redisConnProps.getProperty("port");
        String password=redisConnProps.getProperty("password");
        config.setCodec(new org.redisson.client.codec.StringCodec());
        //指定使用单节点部署方式
        //config.useSingleServer().setAddress("redis://192.168.2.65:6379");
        config.useSingleServer().setAddress("redis://"+host+":"+port);
        config.useSingleServer().setPassword(password);
        return (Redisson) Redisson.create(config);
    }


}
