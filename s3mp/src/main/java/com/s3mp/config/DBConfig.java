package com.s3mp.config;

import java.util.Properties;
    
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
// import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

// This Config for SQLite was provided as boilerplate code in a tutorial on Baeldung.com found here: https://www.baeldung.com/spring-boot-sqlite

@Configuration
// @EnableJpaRepositories(basePackages = "com.")
@PropertySource("application.properties")
public class DBConfig {
    
        @Autowired
        private Environment env;
    
        @Bean
        public DataSource dataSource() {
            final DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(env.getProperty("driverClassName"));
            dataSource.setUrl(env.getProperty("url"));
            dataSource.setUsername(env.getProperty("user"));
            dataSource.setPassword(env.getProperty("password"));
            return dataSource;
        }
    
        @Bean
        public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
            final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
            em.setDataSource(dataSource());
            em.setPackagesToScan(new String[] { "com.s3mp" });
            em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            em.setJpaProperties(additionalProperties());
            return em;
        }
    
        final Properties additionalProperties() {
            final Properties hibernateProperties = new Properties();
            if (env.getProperty("hibernate.hbm2ddl.auto") != null) {
                hibernateProperties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("hibernate.hbm2ddl.auto"));
            }
            if (env.getProperty("hibernate.dialect") != null) {
                hibernateProperties.setProperty("hibernate.dialect", env.getProperty("hibernate.dialect"));
            }
            if (env.getProperty("hibernate.show_sql") != null) {
                hibernateProperties.setProperty("hibernate.show_sql", env.getProperty("hibernate.show_sql"));
            }
            return hibernateProperties;
        }
    
    @Configuration
    @Profile("sqlite")
    @PropertySource("classpath:application.properties")
    class SqliteConfig {
    }
}