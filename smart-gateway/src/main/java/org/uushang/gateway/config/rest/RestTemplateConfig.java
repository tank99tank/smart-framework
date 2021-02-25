package org.uushang.gateway.config.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 *
 * RestTemplate配置项
 *
 *
 * @author liyue
 * @version v1
 * @create 2019-10-10 11:05:14
 * @copyright www.liderong.cn
 */
@Configuration
public class RestTemplateConfig {

    @Autowired
    private OkHttpProperties httpProperties;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}