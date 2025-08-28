package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.config.MinioConstantProperties;
import com.atguigu.tingshu.album.utils.FileUtils;
import com.atguigu.tingshu.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "上传管理接口")
@RestController
@RequestMapping("api/album")
@Slf4j
public class FileUploadApiController {

    //配置文件类
    @Autowired
    private MinioConstantProperties minioConstantProperties;


    @Operation(summary = "文件上传")
    @PostMapping("/fileUpload")
    public Result fileUpload(MultipartFile file) {

        String url = FileUtils.fileUpload(minioConstantProperties, file);
        return Result.ok(url);
    }
}
