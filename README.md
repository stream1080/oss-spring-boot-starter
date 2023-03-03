## oss-spring-boot-starter

兼容 S3 协议的通用对象存储工具类 ，理论上支持所有兼容 S3 协议的对象存储

- MINIO
- 阿里云
- 华为云
- 腾讯云
- 七牛云

...

## 使用方法

未上传 maven 仓库，可 clone 后使用

### clone 项目

```shell
git clone git@github.com:stream1080/oss-spring-boot-starter.git
cd oss-spring-boot-starter
mvn clean install
```

### 引入依赖

```xml

<dependency>
  <groupId>com.stream1080.plugin</groupId>
  <artifactId>oss-spring-boot-starter</artifactId>
  <version>${version}</version>
</dependency>
```

### 配置文件

```yaml
oss:
  http:
    enable: true  # 开启 http 操作端点
    prefix: minio # url 前缀，默认为空
  endpoint: http:127.0.0.1:9000
  access-key: xxxxxx
  secret-key: xxxxxx
```

### 代码使用

```java

@RestController
public class OssTest {

  @Autowired
  private OssTemplate ossTemplate;

  /**
   * 上传文件
   *
   * @param file       文件对象
   * @param bucketName bucket 名称
   * @param filename   文件名
   * @return
   * @throws IOException IOException
   */
  @PostMapping("/upload")
  public R upload(@RequestParam("file") MultipartFile file, String bucketName, String filename) throws IOException {
    ossTemplate.putObject(bucketName, filename, file.getInputStream());
    return ResponseVo.ok();
  }
}
```
