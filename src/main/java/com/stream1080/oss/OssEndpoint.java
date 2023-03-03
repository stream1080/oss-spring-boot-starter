package com.stream1080.oss;

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
        @Cleanup
        InputStream inputStream = object.getInputStream();
        String name = object.getOriginalFilename();

        ossTemplate.putObject(bucketName, name, inputStream, object.getSize(), object.getContentType());
        S3Object objectInfo = ossTemplate.getObjectInfo(bucketName, name);
        ObjectMetadata objectMetadata = objectInfo.getObjectMetadata();
        S3ObjectSummary objectSummary = new S3ObjectSummary();
        objectSummary.setKey(objectInfo.getKey());
        objectSummary.setBucketName(bucketName);
        objectSummary.setETag(objectMetadata.getETag());
        objectSummary.setLastModified(objectMetadata.getLastModified());
        objectSummary.setSize(objectMetadata.getContentLength());
        return objectSummary;
    }


    @ApiOperation("获取 bucket 列表")
    @SneakyThrows
    @PostMapping("/object/{bucketName}/{objectName}")
    public S3ObjectSummary createObject(@RequestBody @NotNull MultipartFile object,
                                        @PathVariable @NotBlank String bucketName,
                                        @PathVariable @NotBlank String objectName) {
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
        Map<String, Object> map = new HashMap<>(8);
        map.put("bucket", bucketName);
        map.put("object", objectName);
        map.put("url", ossTemplate.getObjectURL(bucketName, objectName, expires));
        map.put("expires", expires);
        return map;
    }

    @ApiOperation("获取对象外链-用于上传")
    @GetMapping("/object/put/{bucketName}/{objectName}/{expires}")
    public Map<String, Object> getPutObjectUrl(@PathVariable @NotBlank String bucketName,
                                               @PathVariable @NotBlank String objectName,
                                               @PathVariable @NotNull Integer expires) {
        Map<String, Object> responseBody = new HashMap<>(8);
        responseBody.put("bucket", bucketName);
        responseBody.put("object", objectName);
        responseBody.put("url", ossTemplate.getPutObjectURL(bucketName, objectName, expires));
        responseBody.put("expires", expires);
        return responseBody;
    }

    @ApiOperation("删除对象")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @DeleteMapping("/object/{bucketName}/{objectName}/")
    public void deleteObject(@PathVariable @NotBlank String bucketName, @PathVariable @NotBlank String objectName) {
        ossTemplate.removeObject(bucketName, objectName);
    }

}
