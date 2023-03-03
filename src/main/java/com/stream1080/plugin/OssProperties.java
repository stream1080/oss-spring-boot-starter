package com.stream1080.plugin;

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
     * 自定义域名
     */
    private String customDomain;

    /**
     * 资源访问方式
     * <p>
     * true path-style nginx 反向代理和 S3 默认支持 pathStyle {http://endpoint/bucketname}
     * false supports virtual-hosted-style 阿里云等需要配置为 virtual-hosted-style 模式 {http://bucketname.endpoint}
     */
    private Boolean pathStyleAccess = true;

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
