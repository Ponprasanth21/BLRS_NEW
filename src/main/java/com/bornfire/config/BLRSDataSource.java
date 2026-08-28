package com.bornfire.config;

import java.util.Properties;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@EnableTransactionManagement
@ConfigurationProperties("datasrc")
@EnableJpaRepositories(
        basePackages = "com.bornfire.entities",
        entityManagerFactoryRef = "datasrc",
        transactionManagerRef = "datasrcTransactionManager"
)
public class BLRSDataSource {

    @NotNull
    private String username;

    @NotNull
    private String password;

    @NotNull
    private String url;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Bean
    public LocalSessionFactoryBean datasrc() {

        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();

        sessionFactory.setDataSource(srcdataSource());

        sessionFactory.setPackagesToScan("com.bornfire.entities");

        sessionFactory.setHibernateProperties(hibernateProperties());

        return sessionFactory;
    }

    private Properties hibernateProperties() {

        Properties properties = new Properties();

        properties.setProperty(
                "hibernate.dialect",
                "org.hibernate.dialect.PostgreSQL95Dialect"
        );

        properties.setProperty(
                "hibernate.hbm2ddl.auto",
                "update"
        );

        properties.setProperty(
                "hibernate.show_sql",
                "false"
        );

        properties.setProperty(
                "hibernate.jdbc.lob.non_contextual_creation",
                "true"
        );

        return properties;
    }

    @Bean
    public DataSource srcdataSource() {

        DriverManagerDataSource dataSource =
                new DriverManagerDataSource();

        dataSource.setDriverClassName(
                "org.postgresql.Driver"
        );

        dataSource.setUrl(url);

        dataSource.setUsername(username);

        dataSource.setPassword(password);

        System.out.println("PostgreSQL Username: " + username);
        System.out.println("PostgreSQL URL: " + url);

        return dataSource;
    }

    @Bean
    public PlatformTransactionManager datasrcTransactionManager() {

        JpaTransactionManager transactionManager =
                new JpaTransactionManager();

        transactionManager.setEntityManagerFactory(
                datasrc().getObject()
        );

        return transactionManager;
    }
}