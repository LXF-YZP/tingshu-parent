package com.atguigu.tingshu.album.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.service.BaseCategoryService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.BaseAttribute;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Tag(name = "分类管理")
@RestController
@RequestMapping(value="/api/album/category")
@SuppressWarnings({"all"})
public class BaseCategoryApiController {
	
	@Autowired
	private BaseCategoryService baseCategoryService;

	//根据一级分类Id获取属性与属性值数据，即分类标签数据
	@Operation(summary = "根据一级分类ID获取属性与属性值")
	@GetMapping("/findAttribute/{category1Id}")
	public Result findAttribute(@PathVariable Long category1Id) {
		List<BaseAttribute> baseAttributeList = baseCategoryService.findAttribute(category1Id);
		return Result.ok(baseAttributeList);
	}


	//获取分类数据
	@Operation(summary = "获取分类数据")
	@GetMapping("/getBaseCategoryList")
	public Result getBaseCategoryList() {

		//调用服务层方法
		List<JSONObject> baseCategoryList =
				baseCategoryService.getBaseCategoryList();
		return Result.ok(baseCategoryList);

	}

}

