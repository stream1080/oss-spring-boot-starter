package com.stream1080.oss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 对象存储配置
 *
 * @author stream1080
 * @date 2023-03-02 15:57:31
 */
@Data
@ConfigurationProperties(prefix = OssProperties.PREFIX)
public class OssProperties {

    /**
     * 配置前缀
     */
    public static final String PREFIX = "oss";

    /**
     * 是否启用 oss，默认为：true
     */
    private boolean enable = true;

    /**
     * 对象存储服务的 URL
     */
    private String endpoint;

    /**
     * 区域
     */
    private String region;

    /**
     * 账户的 key
     */
    private String accessKey;

    /**
     * 账户的密码
     */
    private String secretKey;

    /**
     * 默认的存储桶名称
     */
    private String bucketName;
}
