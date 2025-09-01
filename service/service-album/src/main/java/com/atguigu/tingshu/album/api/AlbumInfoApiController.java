package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "专辑管理")
@RestController
@RequestMapping("api/album/albumInfo")
@SuppressWarnings({"all"})
public class AlbumInfoApiController {

	@Autowired
	private AlbumInfoService albumInfoService;

	//获取用户专辑列表
	@Operation(summary = "获取专辑列表" )
	@GetMapping("/findUserAllAlbumList")
	public Result findUserAllAlbumList(){
		Long userId = null == AuthContextHolder.getUserId() ? 1L :AuthContextHolder.getUserId();
		List<AlbumInfo> albumInfoList = albumInfoService.findUserAllAlbumList(userId);
		return Result.ok(albumInfoList);
	}

	//删除专辑
	@Operation(summary = "删除专辑")
	@DeleteMapping("/removeAlbumInfo/{albumId}")
	public Result removeAlbumInfo(@PathVariable("albumId") Long albumId) {

		albumInfoService.removeAlbumInfo(albumId);
		return Result.ok();
	}

	//更新专辑
	@Operation(summary = "根据专辑id更新专辑信息")
	@PostMapping("/updateAlbumInfo/{albumId}")
	public Result updateAlbumInfo(@PathVariable("albumId") Long albumId,
								 @RequestBody AlbumInfoVo albumInfoVo) {

		albumInfoService.updateAlbumInfo(albumId, albumInfoVo);
		return Result.ok();
	}

	@Operation(summary = "根据专辑id查看专辑信息")
	@GetMapping("/getALbumInfo/{albumId}")
	public Result getAlblumInfo(@PathVariable("albumId") Long albumId) {
		AlbumInfo albumInfoVo = albumInfoService.getAlbumInfo(albumId);
		return Result.ok(albumInfoVo);
	}

	//分页列表查询
	@Operation(summary = "分页查询专辑列表")
	@PostMapping("/findUserAlbumPage/{pageNo}/{pageSize}")
	public Result findUserAlbumPage(@PathVariable("pageNo") Integer pageNo,
									@PathVariable("pageSize") Integer pageSize,
									@RequestBody AlbumInfoQuery albumInfoQuery){

		Long userId = AuthContextHolder.getUserId() == null ? 1L : AuthContextHolder.getUserId();
		albumInfoQuery.setUserId(userId);
		Page<AlbumListVo> page = new Page<>(pageNo, pageSize);
		IPage<AlbumListVo> albumListVoIPage = albumInfoService.findUserAlbumPage(page, albumInfoQuery);
		return Result.ok(albumListVoIPage);
	}

	//保存专辑
	//必须要登录，如果不登录，则不能访问！如果完成登录模块，那么我们会将用户ID 存储到本地线程中，threadlocal；通过
	//authcontextholder获取用户ID
	@Operation(summary = "保存专辑")
	@PostMapping("saveAlbumInfo")
	public Result saveAlbumInfo(@RequestBody AlbumInfoVo albumInfoVo) {
		Long userId = AuthContextHolder.getUserId() == null ? 1L : AuthContextHolder.getUserId();
		Boolean flag = albumInfoService.saveAlbumInfo(albumInfoVo, userId);
		return Result.ok(flag);
	}


}

