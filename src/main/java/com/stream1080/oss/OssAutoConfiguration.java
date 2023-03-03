package com.stream1080.oss;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自动配置类
 *
 * @author stream1080
 * @date 2023-03-02 17:25:19
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({OssProperties.class})
public class OssAutoConfiguration {

    /**
     * OSS 操作模板
     *
     * @param properties oss 配置
     * @return oss 操作模版
     */
    @Bean
    @ConditionalOnMissingBean(OssTemplate.class)
    @ConditionalOnProperty(prefix = OssProperties.PREFIX, name = "enable", havingValue = "true", matchIfMissing = true)
    public OssTemplate ossTemplate(OssProperties properties) {
        return new OssTemplate(properties);
    }

    /**
     * OSS 端点信息
     *
     * @param template oss操作模版
     * @return oss远程服务端点
     */
    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnProperty(prefix = OssProperties.PREFIX, name = "http.enable", havingValue = "true")
    public OssEndpoint ossEndpoint(OssTemplate template) {
        return new OssEndpoint(template);
    }

}
