package org.uushang.gateway.config.rest;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 *
 * OkHttpClient配置参数
 *
 *
 * @author liyue
 * @version v1
 * @create 2019-10-10 11:08:14
 * @copyright www.liderong.cn
 */
@Configuration
@ConfigurationProperties("http.config")
@Getter
@Setter
public class OkHttpProperties {

    private Long connectTimeout;

    private Long readTimeout;

    private Long writeTimeout;

    @Value("${http.config.connectionPool.maxIdleConnections}")
    private Integer maxIdleConnections;

    @Value("${http.config.connectionPool.keepAliveDuration}")
    private Long keepAliveDuration;
}