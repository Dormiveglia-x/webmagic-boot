package com.bidizhaobiao.data.Crawl.config;


import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author 作者: 廉建林
 * @version 创建时间：2018年8月9日 上午11:07:54 类说明
 * 配置orcale数据源
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.bidizhaobiao.data.Crawl.dao.oracle", entityManagerFactoryRef = "oracleManagerFactory", transactionManagerRef = "oracleTransactionManager")
public class OracleConfiguration {

    @Autowired
    @Qualifier("druidPoolProps")
    private Properties druidPoolProps;
    @Autowired
    @Qualifier("oracleConnProps")
    private Properties oracleConnProps;

    public static Map propertiesToMap(Properties properties) {
        return new HashMap<String, String>((Map) properties);
    }

    @Bean("oracleDataSource")
    @DependsOn({"oracleConnProps", "druidPoolProps"})
    public DataSource oracleDataSource() throws SQLException {
        DruidDataSource oracleDataSource = new DruidDataSource();

        oracleDataSource.setDriverClassName(oracleConnProps.getProperty("driver-class-name"));
        oracleDataSource.setUrl(oracleConnProps.getProperty("url"));
        oracleDataSource.setUsername(oracleConnProps.getProperty("username"));
        oracleDataSource.setPassword(oracleConnProps.getProperty("password"));

        oracleDataSource.setInitialSize(Integer.parseInt(druidPoolProps.getProperty("initial-size")));
        oracleDataSource.setMaxActive(Integer.parseInt(druidPoolProps.getProperty("max-active")));
        oracleDataSource.setMinIdle(Integer.parseInt(druidPoolProps.getProperty("min-idle")));
        oracleDataSource.setMaxWait(Long.parseLong(druidPoolProps.getProperty("max-wait")));
        oracleDataSource.setPoolPreparedStatements(
                Boolean.parseBoolean(druidPoolProps.getProperty("pool-prepared-statements"))
        );
        oracleDataSource.setValidationQuery(druidPoolProps.getProperty("validation-query"));
        oracleDataSource.setValidationQueryTimeout(
                Integer.parseInt(druidPoolProps.getProperty("validation-query-timeout"))
        );
        oracleDataSource.setTestOnBorrow(Boolean.parseBoolean(druidPoolProps.getProperty("test-on-borrow")));
        oracleDataSource.setTestOnReturn(Boolean.parseBoolean(druidPoolProps.getProperty("test-on-return")));
        oracleDataSource.setTestWhileIdle(Boolean.parseBoolean(druidPoolProps.getProperty("test-while-idle")));
        oracleDataSource.setTimeBetweenEvictionRunsMillis(
                Long.parseLong(druidPoolProps.getProperty("time-between-eviction-runs-millis"))
        );
        oracleDataSource.setMinEvictableIdleTimeMillis(
                Long.parseLong(druidPoolProps.getProperty("min-evictable-idle-time-millis"))
        );
        oracleDataSource.setMaxEvictableIdleTimeMillis(
                Long.parseLong(druidPoolProps.getProperty("max-evictable-idle-time-millis"))
        );
        oracleDataSource.setFilters(druidPoolProps.getProperty("filters"));

        return oracleDataSource;
    }

    @Bean
    /**
 * @author 作者: 廉建林
 * @version 创建时间：2018年8月9日 上午11:07:54 类说明
 * 配置orcale数据源
 */
@ConfigurationProperties("spring.datasource.jpa.oracle")
    public Properties oracleJpaProperties() {
        return new Properties();
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean oracleManagerFactory() throws SQLException {
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean.setDataSource(oracleDataSource());
        localContainerEntityManagerFactoryBean.setPackagesToScan("com.bidizhaobiao.data.Crawl.entity.oracle");
        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        localContainerEntityManagerFactoryBean.setJpaPropertyMap(OracleConfiguration.propertiesToMap(oracleJpaProperties()));
        return localContainerEntityManagerFactoryBean;
    }

    @Bean
    public PlatformTransactionManager oracleTransactionManager() throws SQLException {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(oracleManagerFactory().getObject());
        return transactionManager;
    }

}
