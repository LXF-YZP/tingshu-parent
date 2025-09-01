package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "声音管理")
@RestController
@RequestMapping("api/album/trackInfo")
@SuppressWarnings({"all"})
public class TrackInfoApiController {

    @Autowired
    private TrackInfoService trackInfoService;

    @Autowired
    private VodService vodService;

    //修改声音
    @Operation(summary = "修改声音")
    @PutMapping("updateTrackInfo/{trackId}")
    public Result updateTrackInfo(@PathVariable("trackId") Long trackId,
                                  @RequestBody TrackInfoVo trackInfoVo) {
        trackInfoService.updateTrackInfo(trackId, trackInfoVo);
        return Result.ok();

    }

    //根据声音Id获取声音对象
    @Operation(summary = "根据声音Id获取声音对象")
    @GetMapping("/getTrackInfo/{trackId}")
    public Result getTrackInfo(@PathVariable("trackId") Long trackId) {
        TrackInfo trackInfo = trackInfoService.getById(trackId);
        return Result.ok(trackInfo);
    }

    //删除声音数据
    @Operation(summary = "根据声音ID删除声音数据")
    @DeleteMapping("/removeTrackInfo/{trackId}")
    public Result removeTrackInfo(@PathVariable("trackId") Long trackId) {

        trackInfoService.removeTrackInfo(trackId);
        return Result.ok();
    }

    @Operation(summary = "分页查询声音列表")
    @PostMapping("/findUserTrackPage/{pageNo}/{pageSize}")
    public Result findUserTrackPage(@PathVariable("pageNo") Integer pageNo,
                                    @PathVariable("pageSize") Integer pageSize,
                                    @RequestBody TrackInfoQuery trackInfoQuery) {

        //构建page对象
        Page<TrackListVo> page = new Page<>(pageNo, pageSize);
        //获取用户Id
        Long userId = null == AuthContextHolder.getUserId() ? 1L : AuthContextHolder.getUserId();
        trackInfoQuery.setUserId(userId);
        IPage<TrackListVo> trackListVoIPage = trackInfoService.findUserTrackPage(page, trackInfoQuery);
        return Result.ok(trackListVoIPage);
    }


    //保存声音，即持久化声音链接到数据库中
    @Operation(summary = "保存声音")
    @PostMapping("/saveTrackInfo")
    public Result saveTrackInfo(@RequestBody TrackInfoVo trackInfoVo) {
        Long userId = null == AuthContextHolder.getUserId() ? 1L : AuthContextHolder.getUserId();
        trackInfoService.saveTrackInfo(trackInfoVo, userId);
        return Result.ok();
    }

    @Operation(summary = "上传声音")
    @PostMapping("/uploadTrack")
    public Result uploadTrack(MultipartFile file) {
        Map<String, Object> map = vodService.uploadTrack(file);
        return Result.ok(map);
    }

}

