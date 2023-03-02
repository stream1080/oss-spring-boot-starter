package com.stream1080.oss;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配置类
 *
 * @author stream1080
 * @date 2023-03-02 17:25:19
 */
@EnableAutoConfiguration
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({OssProperties.class})
public class OssAutoConfiguration {

    /**
     * OSS 操作模板
     *
     * @param properties
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(OssTemplate.class)
    @ConditionalOnProperty(prefix = OssProperties.PREFIX, name = "enable", havingValue = "true", matchIfMissing = true)
    public OssTemplate ossTemplate(OssProperties properties) {
        return new OssTemplate(properties);
    }

}
