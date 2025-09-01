package com.atguigu.tingshu.album.service;

import com.atguigu.tingshu.validation.NotEmptyPaid;
import com.atguigu.tingshu.vo.album.TrackMediaInfoVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface VodService {

    //上传声音
    Map<String, Object> uploadTrack(MultipartFile file);

    TrackMediaInfoVo getMediaInfo(@NotEmptyPaid(message = "mediaFileId不能为空") String mediaFileId);

    //删除云点播声音
    void deleteMediaFile(String mediaFileId);
}
