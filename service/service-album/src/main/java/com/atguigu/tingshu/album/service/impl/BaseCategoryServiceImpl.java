package com.atguigu.tingshu.album.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.mapper.*;
import com.atguigu.tingshu.album.service.BaseCategoryService;
import com.atguigu.tingshu.model.album.BaseCategory1;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@SuppressWarnings({"all"})
public class BaseCategoryServiceImpl extends ServiceImpl<BaseCategory1Mapper, BaseCategory1> implements BaseCategoryService {

	@Autowired
	private BaseCategory1Mapper baseCategory1Mapper;

	@Autowired
	private BaseCategory2Mapper baseCategory2Mapper;

	@Autowired
	private BaseCategory3Mapper baseCategory3Mapper;


	@Autowired
	private BaseCategoryViewMapper baseCategoryViewMapper;

	@Autowired
	private BaseAttributeMapper baseAttributeMapper;


	@Override
	public List<JSONObject> getBaseCategoryList() {

		List<JSONObject> list = new ArrayList<>();
		//	使用视图获取数据：
		List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);
		//	Json 数据是通过map map.put(key,value)或 class  setName("zs") 形式！{“name”:"zs"}
		Map<Long, List<BaseCategoryView>> basecategory1Map = baseCategoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
		Iterator<Map.Entry<Long, List<BaseCategoryView>>> iterator = basecategory1Map.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Long, List<BaseCategoryView>> entry = iterator.next();
			Long category1Id = entry.getKey();
			List<BaseCategoryView> categoryViewList = entry.getValue();
			JSONObject category1 = new JSONObject();
			category1.put("categoryName", categoryViewList.get(0).getCategory1Name());
			category1.put("categoryId", category1Id);
			//	获取一级分类下的二级分类数据;
			Map<Long, List<BaseCategoryView>> category2Map = categoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
			Iterator<Map.Entry<Long, List<BaseCategoryView>>> iterator1 = category2Map.entrySet().iterator();
			//	存储到二级分类数据：
			List<JSONObject> categoryChild2List = new ArrayList<>();
			while (iterator1.hasNext()) {
				//	获取二级分类对象数据 音乐层级下应该有3次循环;
				Map.Entry<Long, List<BaseCategoryView>> entry1 = iterator1.next();
				Long category2Id = entry1.getKey();
				//	获取到的数据是二级分类Id 对应的集合数据
				List<BaseCategoryView> categoryViewList1 = entry1.getValue();
				JSONObject category2 = new JSONObject();
				category2.put("categoryName", categoryViewList1.get(0).getCategory2Name());
				category2.put("categoryId", category2Id);
				//	获取三级分类数据：直接循环遍历获取三级分类数据即可
				List<JSONObject> categoryChild3List = categoryViewList1.stream().map(baseCategoryView -> {
					//	创建三级分类对象
					JSONObject category3 = new JSONObject();
					category3.put("categoryId", baseCategoryView.getCategory3Id());
					category3.put("categoryName", baseCategoryView.getCategory3Name());
					return category3;
				}).collect(Collectors.toList());
				//	获取三级分类数据添加到二级分类中;
				category2.put("categoryChild", categoryChild3List);
				categoryChild2List.add(category2);
			}
			//	将二级分类数据添加到一级分类中;
			category1.put("categoryChild", categoryChild2List);
			//	将所有一级分类对象添加到集合中
			list.add(category1);
		}
		return list;
	}
}
