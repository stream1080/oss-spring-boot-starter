package com.stream1080.plugin;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * http 端点
 *
 * @author stream1080
 * @date 2023-03-02 18:14:53
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("${oss.http.prefix:}/oss")
@Api(value = "OssEndpoint", tags = "oss:http 接口")
public class OssEndpoint {

    /**
     * oss 操作模版
     */
    private final OssTemplate ossTemplate;


    @ApiOperation("创建 bucket")
    @PostMapping("/bucket/{bucketName}")
    public Bucket createBucket(@PathVariable @NotBlank String bucketName) {
        ossTemplate.createBucket(bucketName);
        return ossTemplate.getBucket(bucketName).get();
    }

    @ApiOperation("获取 bucket 列表")
    @GetMapping("/bucket")
    public List<Bucket> getBuckets() {
        return ossTemplate.getAllBuckets();
    }

    @ApiOperation("获取 bucket")
    @GetMapping("/bucket/{bucketName}")
    public Bucket getBucket(@PathVariable @NotBlank String bucketName) {
        return ossTemplate.getBucket(bucketName)
            .orElseThrow(() -> new IllegalArgumentException("Bucket Name not found!"));
    }

    @ApiOperation("删除 bucket ")
    @DeleteMapping("/bucket/{bucketName}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void deleteBucket(@PathVariable @NotBlank String bucketName) {
        ossTemplate.removeBucket(bucketName);
    }

    @ApiOperation("上传对象")
    @SneakyThrows
    @PostMapping("/object/{bucketName}")
    public S3ObjectSummary createObject(@RequestBody @NotNull MultipartFile object,
                                        @PathVariable @NotBlank String bucketName) {

        return buildS3ObjectSummary(object, bucketName, object.getOriginalFilename());
    }

    @ApiOperation("上传对象")
    @SneakyThrows
    @PostMapping("/object/{bucketName}/{objectName}")
    public S3ObjectSummary createObject(@RequestBody @NotNull MultipartFile object,
                                        @PathVariable @NotBlank String bucketName,
                                        @PathVariable @NotBlank String objectName) {

        return buildS3ObjectSummary(object, bucketName, objectName);
    }

    @ApiOperation("查询对象")
    @GetMapping("/object/{bucketName}/{objectName}")
    public List<S3ObjectSummary> filterObject(@PathVariable @NotBlank String bucketName,
                                              @PathVariable @NotBlank String objectName) {

        return ossTemplate.getAllObjectsByPrefix(bucketName, objectName);
    }

    @ApiOperation("获取对象外链-用于下载")
    @GetMapping("/object/{bucketName}/{objectName}/{expires}")
    public Map<String, Object> getObjectUrl(@PathVariable @NotBlank String bucketName,
                                            @PathVariable @NotBlank String objectName,
                                            @PathVariable @NotNull Integer expires) {

        String objectURL = ossTemplate.getObjectURL(bucketName, objectName, expires);
        return buildResponseBody(bucketName, objectName, objectURL, expires);
    }

    @ApiOperation("获取对象外链-用于上传")
    @GetMapping("/object/put/{bucketName}/{objectName}/{expires}")
    public Map<String, Object> getPutObjectUrl(@PathVariable @NotBlank String bucketName,
                                               @PathVariable @NotBlank String objectName,
                                               @PathVariable @NotNull Integer expires) {

        String objectURL = ossTemplate.getPutObjectURL(bucketName, objectName, expires);
        return buildResponseBody(bucketName, objectName, objectURL, expires);
    }

    @ApiOperation("删除对象")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @DeleteMapping("/object/{bucketName}/{objectName}/")
    public void deleteObject(@PathVariable @NotBlank String bucketName, @PathVariable @NotBlank String objectName) {
        ossTemplate.removeObject(bucketName, objectName);
    }

    /**
     * 构建外链返回 body
     *
     * @param bucketName bucket 名称
     * @param objectName 对象名称
     * @param objectURL  外链 url
     * @param expires    过期时间
     * @return responseBody
     */
    private Map<String, Object> buildResponseBody(String bucketName, String objectName,
                                                  String objectURL, Integer expires) {

        Map<String, Object> responseBody = new HashMap<>(8);
        responseBody.put("bucket", bucketName);
        responseBody.put("object", objectName);
        responseBody.put("url", objectURL);
        responseBody.put("expires", expires);

        return responseBody;
    }

    /**
     * 构建 S3ObjectSummary
     *
     * @param object     文件对象
     * @param bucketName bucket 名称
     * @param objectName 对象名称
     * @return S3ObjectSummary
     * @throws IOException IOException
     */
    private S3ObjectSummary buildS3ObjectSummary(MultipartFile object, String bucketName, String objectName)
        throws IOException {

        @Cleanup
        InputStream inputStream = object.getInputStream();
        ossTemplate.putObject(bucketName, objectName, inputStream, object.getSize(), object.getContentType());
        S3Object objectInfo = ossTemplate.getObjectInfo(bucketName, objectName);
        ObjectMetadata objectMetadata = objectInfo.getObjectMetadata();

        S3ObjectSummary objectSummary = new S3ObjectSummary();
        objectSummary.setKey(objectInfo.getKey());
        objectSummary.setBucketName(bucketName);
        objectSummary.setETag(objectMetadata.getETag());
        objectSummary.setLastModified(objectMetadata.getLastModified());
        objectSummary.setSize(objectMetadata.getContentLength());

        return objectSummary;
    }

}
