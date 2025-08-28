package com.atguigu.tingshu.album.utils;/**
 * @Auther: yzp
 * @Date: 2025/8/29 - 08 - 29 - 7:02
 * @Description: com.atguigu.tingshu.album.utils
 * @version: 1.0
 */

import com.atguigu.tingshu.album.config.MinioConstantProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.UUID;

/**
 * @Auther: yzp
 * @Date: 2025/8/29 - 08 - 29 - 7:02
 * @Description: com.atguigu.tingshu.album.utils
 * @version: 1.0
 */

@Slf4j
public class FileUtils {

    public static String fileUpload(MinioConstantProperties minioConstantProperties, MultipartFile file) {

        String url = "";
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioConstantProperties.getEndpointUrl())
                .credentials(minioConstantProperties.getAccessKey(), minioConstantProperties.getSecreKey())
                .build();
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioConstantProperties.getBucketName()).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioConstantProperties.getBucketName()).build());
            } else {
                log.info("Bucket {} already exists.", minioConstantProperties.getBucketName());
            }
            String myFilename = UUID.randomUUID().toString().replace("-", "") + Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));
            log.info("fileName:{}", myFilename);

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConstantProperties.getBucketName())
                    .object(myFilename)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            url = minioConstantProperties.getEndpointUrl() + "/" + minioConstantProperties.getBucketName() + "/" + myFilename;
            log.info("url:{}", url);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return url;
    }
}
