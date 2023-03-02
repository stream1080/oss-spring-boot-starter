package com.stream1080.oss;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 对象存储操作模版
 *
 * @author stream1080
 * @date 2023-03-02 16:08:02
 */
@RequiredArgsConstructor
public class OssTemplate implements InitializingBean {

    private final OssProperties ossProperties;

    private AmazonS3 amazonS3;

    /**
     * 创建 bucket
     *
     * @param bucketName bucket 名称
     */
    public void createBucket(String bucketName) {
        if (!amazonS3.doesBucketExistV2(bucketName)) {
            amazonS3.createBucket((bucketName));
        }
    }

    /**
     * 获取全部 bucket
     *
     * @return bucket 列表
     */
    public List<Bucket> getAllBuckets() {
        return amazonS3.listBuckets();
    }

    /**
     * 获取 bucket
     *
     * @param bucketName bucket名称
     * @return bucket
     */
    public Optional<Bucket> getBucket(String bucketName) {
        return amazonS3.listBuckets().stream().filter(b -> b.getName().equals(bucketName)).findFirst();
    }

    /**
     * 删除 bucket
     *
     * @param bucketName bucket名称
     */
    public void removeBucket(String bucketName) {
        amazonS3.deleteBucket(bucketName);
    }

    /**
     * 根据文件名前缀查询文件
     *
     * @param bucketName bucket名称
     * @param prefix     前缀
     * @return 文件对象列表
     */
    public List<S3ObjectSummary> getAllObjectsByPrefix(String bucketName, String prefix) {
        ObjectListing objectListing = amazonS3.listObjects(bucketName, prefix);
        return new ArrayList<>(objectListing.getObjectSummaries());
    }

    /**
     * 获取文件外链，只用于下载
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @param minutes    过期时间，单位分钟,请注意该值必须小于7天
     * @return url
     * @see AmazonS3#generatePresignedUrl(String bucketName, String key, Date expiration)
     */
    public String getObjectURL(String bucketName, String objectName, int minutes) {
        return getObjectURL(bucketName, objectName, Duration.ofMinutes(minutes));
    }

    /**
     * 获取文件外链，只用于下载
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @param expires    过期时间,请注意该值必须小于7天
     * @return url
     * @see AmazonS3#generatePresignedUrl(String bucketName, String key, Date expiration)
     */
    public String getObjectURL(String bucketName, String objectName, Duration expires) {
        return getObjectURL(bucketName, objectName, expires, HttpMethod.GET);
    }

    /**
     * 获取文件上传外链，只用于上传
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @param minutes    过期时间，单位分钟,请注意该值必须小于7天
     * @return url
     * @see AmazonS3#generatePresignedUrl(String bucketName, String key, Date expiration)
     */
    public String getPutObjectURL(String bucketName, String objectName, int minutes) {
        return getPutObjectURL(bucketName, objectName, Duration.ofMinutes(minutes));
    }

    /**
     * 获取文件上传外链，只用于上传
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @param expires    过期时间,请注意该值必须小于7天
     * @return url
     * @see AmazonS3#generatePresignedUrl(String bucketName, String key, Date expiration)
     */
    public String getPutObjectURL(String bucketName, String objectName, Duration expires) {
        return getObjectURL(bucketName, objectName, expires, HttpMethod.PUT);
    }

    /**
     * 获取文件外链
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @param minutes    过期时间，单位分钟,请注意该值必须小于7天
     * @param method     文件操作方法：GET（下载）、PUT（上传）
     * @return url
     * @see AmazonS3#generatePresignedUrl(String bucketName, String key, Date expiration, HttpMethod method)
     */
    public String getObjectURL(String bucketName, String objectName, int minutes, HttpMethod method) {
        return getObjectURL(bucketName, objectName, Duration.ofMinutes(minutes), method);
    }

    /**
     * 获取文件外链
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @param expires    过期时间，请注意该值必须小于7天
     * @param method     文件操作方法：GET（下载）、PUT（上传）
     * @return url
     * @see AmazonS3#generatePresignedUrl(String bucketName, String key, Date expiration, HttpMethod method)
     */
    public String getObjectURL(String bucketName, String objectName, Duration expires, HttpMethod method) {
        // 设置预签名 URL 在 expires 之后过期。
        Date expiration = Date.from(Instant.now().plus(expires));

        // 生成预签名 URL
        URL url = amazonS3.generatePresignedUrl(
            new GeneratePresignedUrlRequest(bucketName, objectName).withMethod(method).withExpiration(expiration));

        return url.toString();
    }

    /**
     * 获取文件 URL
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @return url
     */
    public String getObjectURL(String bucketName, String objectName) {
        URL url = amazonS3.getUrl(bucketName, objectName);
        return url.toString();
    }

    /**
     * 获取文件
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @return 文件对象
     */
    public S3Object getObject(String bucketName, String objectName) {
        return amazonS3.getObject(bucketName, objectName);
    }

    /**
     * 上传文件
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @param stream     文件流
     * @throws IOException IOException
     */
    public void putObject(String bucketName, String objectName, InputStream stream) throws IOException {
        putObject(bucketName, objectName, stream, stream.available(), "application/octet-stream");
    }

    /**
     * 上传文件 指定 contextType
     *
     * @param bucketName  bucket名称
     * @param objectName  文件名称
     * @param stream      文件流
     * @param contextType 文件类型
     * @throws IOException IOException
     */
    public void putObject(String bucketName, String objectName, String contextType, InputStream stream)
        throws IOException {
        putObject(bucketName, objectName, stream, stream.available(), contextType);
    }

    /**
     * 上传文件
     *
     * @param bucketName  bucket名称
     * @param objectName  文件名称
     * @param stream      文件流
     * @param size        大小
     * @param contextType 类型
     * @return 上传结果
     */
    public PutObjectResult putObject(String bucketName, String objectName, InputStream stream, long size,
                                     String contextType) {

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(size);
        objectMetadata.setContentType(contextType);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, stream, objectMetadata);

        // 将读限制值设置为比流的大小大一个字节，避免 ResetException
        putObjectRequest.getRequestClientOptions().setReadLimit(Long.valueOf(size).intValue() + 1);

        return amazonS3.putObject(putObjectRequest);
    }

    /**
     * 获取文件信息
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     */
    public S3Object getObjectInfo(String bucketName, String objectName) {
        return amazonS3.getObject(bucketName, objectName);
    }

    /**
     * 删除文件
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     */
    public void removeObject(String bucketName, String objectName) {
        amazonS3.deleteObject(bucketName, objectName);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ClientConfiguration clientConfiguration = new ClientConfiguration();

        AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(
            ossProperties.getEndpoint(), ossProperties.getRegion());

        AWSCredentials awsCredentials = new BasicAWSCredentials(ossProperties.getAccessKey(), ossProperties.getSecretKey());

        AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);

        this.amazonS3 = AmazonS3Client.builder()
            .withEndpointConfiguration(endpointConfiguration)
            .withClientConfiguration(clientConfiguration)
            .withCredentials(awsCredentialsProvider)
            .disableChunkedEncoding()
            .withPathStyleAccessEnabled(ossProperties.getPathStyleAccess())
            .build();
    }
}
