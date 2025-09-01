package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.config.VodConstantProperties;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.util.UploadFileUtil;
import com.atguigu.tingshu.vo.album.TrackMediaInfoVo;
import com.qcloud.vod.VodUploadClient;
import com.qcloud.vod.model.VodUploadRequest;
import com.qcloud.vod.model.VodUploadResponse;
import com.tencentcloudapi.common.AbstractModel;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.vod.v20180717.VodClient;
import com.tencentcloudapi.vod.v20180717.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;


@Service
@Slf4j
public class VodServiceImpl implements VodService {

    @Autowired
    private VodConstantProperties vodConstantProperties;

    @Override
    public Map<String, Object> uploadTrack(MultipartFile file) {

        HashMap<String, Object> hashMap = new HashMap<>();
        VodUploadClient vodUploadClient = new VodUploadClient(vodConstantProperties.getSecretId(), vodConstantProperties.getSecretKey());
        VodUploadRequest vodUploadRequest = new VodUploadRequest();
        String tempPath = UploadFileUtil.uploadTempPath(vodConstantProperties.getTempPath(), file);
        vodUploadRequest.setMediaFilePath(tempPath);
        try {
            VodUploadResponse vodUploadResponse = vodUploadClient.upload(vodConstantProperties.getRegion(), vodUploadRequest);
            log.info("上传结果：{}", vodUploadResponse.getFileId());
            hashMap.put("mediaFileId", vodUploadResponse.getFileId());
            hashMap.put("mediaUrl", vodUploadResponse.getMediaUrl());
        } catch (Exception e) {
            log.error("上传失败", e);
        }
        return hashMap;
    }

    @Override
    public TrackMediaInfoVo getMediaInfo(String mediaFileId) {

        Credential credential = new Credential(vodConstantProperties.getSecretId(), vodConstantProperties.getSecretKey());
        VodClient vodClient = new VodClient(credential, vodConstantProperties.getRegion());
        DescribeMediaInfosRequest describeMediaInfosRequest = new DescribeMediaInfosRequest();
        String[] fileIds = {mediaFileId};
        describeMediaInfosRequest.setFileIds(fileIds);
        DescribeMediaInfosResponse describeMediaInfosResponse = null;
        try {
            describeMediaInfosResponse = vodClient.DescribeMediaInfos(describeMediaInfosRequest);
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException(e);
        }
        TrackMediaInfoVo trackMediaInfoVo = new TrackMediaInfoVo();
        MediaInfo mediaInfo = describeMediaInfosResponse.getMediaInfoSet()[0];
        trackMediaInfoVo.setDuration(mediaInfo.getMetaData().getDuration());
        trackMediaInfoVo.setSize(mediaInfo.getMetaData().getSize());
        trackMediaInfoVo.setMediaUrl(mediaInfo.getBasicInfo().getMediaUrl());
        trackMediaInfoVo.setType(mediaInfo.getBasicInfo().getType());
        return trackMediaInfoVo;
    }

    @Override
    public void deleteMediaFile(String mediaFileId) {
        try {
            Credential credential = new Credential(vodConstantProperties.getSecretId(), vodConstantProperties.getSecretKey());
            VodClient client = new VodClient(credential, "");
            DeleteMediaRequest request = new DeleteMediaRequest();
            request.setFileId(mediaFileId);
            DeleteMediaResponse deleteMedia = client.DeleteMedia(request);
            log.info(AbstractModel.toJsonString(deleteMedia));
        } catch (TencentCloudSDKException e) {
            log.info(e.toString());
        }

    }
}
