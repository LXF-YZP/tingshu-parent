package com.atguigu.tingshu.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.atguigu.tingshu.album.client.AlbumInfoFeignClient;
import com.atguigu.tingshu.album.client.CategoryFeignClient;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import com.atguigu.tingshu.model.search.AttributeValueIndex;
import com.atguigu.tingshu.query.search.AlbumIndexQuery;
import com.atguigu.tingshu.search.repository.AlbumIndexRepository;
import com.atguigu.tingshu.search.service.SearchService;
import com.atguigu.tingshu.user.client.UserInfoFeignClient;
import com.atguigu.tingshu.vo.search.AlbumSearchResponseVo;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


@Slf4j
@Service
@SuppressWarnings({"all"})
public class SearchServiceImpl implements SearchService {


    @Autowired
    private AlbumInfoFeignClient albumInfoFeignClient;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private CategoryFeignClient categoryFeignClient;

    @Autowired
    private UserInfoFeignClient userInfoFeignClient;


    @Autowired
    private AlbumIndexRepository albumIndexRepository;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    /**
     * 上架专辑
     * @param albumId
     */
    @Override
    public void upperAlbum(Long albumId) {

        //需要使用albuminfoindex
        AlbumInfoIndex albumInfoIndex = new AlbumInfoIndex();
        //使用多线程查询数据
        CompletableFuture<AlbumInfo> albumInfoCompletableFuture = CompletableFuture.supplyAsync(
                () -> {
                    //赋值 album_info表数据
                    Result<AlbumInfo> albumInfoResult = albumInfoFeignClient.getAlbumInfo(albumId);
                    Assert.notNull(albumInfoResult, "查询专辑信息失败");
                    AlbumInfo albumInfo = albumInfoResult.getData();
                    Assert.notNull(albumInfo, "查询专辑信息失败");
                    BeanUtils.copyProperties(albumInfo, albumInfoIndex);
                    //获取属性与属性值信息
                    List<AlbumAttributeValue> albumAttributeValueVoList = albumInfo.getAlbumAttributeValueVoList();
                    //判断albumattributevaluevolist是否为空
                    if (!CollectionUtils.isEmpty(albumAttributeValueVoList)) {
                        List<AttributeValueIndex> attributeValueIndexList = albumAttributeValueVoList.stream().map(albumAttributeValue -> {
                            //创建一个attributevalueindex对象
                            AttributeValueIndex attributeValueIndex = new AttributeValueIndex();
                            attributeValueIndex.setAttributeId(albumAttributeValue.getAttributeId());
                            attributeValueIndex.setValueId(albumAttributeValue.getValueId());
                            return attributeValueIndex;
                        }).collect(Collectors.toList());
                        //将属性与属性值添加到索引中
                        albumInfoIndex.setAttributeValueIndexList(attributeValueIndexList);
                    }
                    //返回数据
                    return albumInfo;

                }, threadPoolExecutor).exceptionally(throwable -> {
            log.error("查询专辑信息失败{}", throwable.getCause());
            return null;
        });


        //根据三级分类Id查询视图就可以了，albuminfo.getcategory3Id;
        //获取到上一个返回值的结果并使用select * form base_category_view  where id =10001
        CompletableFuture<Integer> cateCompletableFuture = albumInfoCompletableFuture.thenApplyAsync(albumInfo -> {
            //远程调用根据三级分类Id获取分类数据；
            Result<BaseCategoryView> baseCategoryView = categoryFeignClient.getBaseCategoryView(albumInfo.getCategory3Id());
            Assert.notNull(baseCategoryView, "查询分类数据失败");
            BaseCategoryView baseCategoryViewData = baseCategoryView.getData();
            Assert.notNull(baseCategoryViewData, "查询分类数据失败");
            albumInfoIndex.setCategory1Id(baseCategoryViewData.getCategory1Id());
            albumInfoIndex.setCategory2Id(baseCategoryViewData.getCategory2Id());
            return 1;
        }, threadPoolExecutor).exceptionally(throwable -> {
            log.error("查询分类数据失败{}", throwable.getMessage());
            return 0;
        });

        //查询作者信息
        CompletableFuture<Integer> userCompletableFuture = albumInfoCompletableFuture.thenApplyAsync(albumInfo -> {

            Result<UserInfoVo> userInfoVoResult = userInfoFeignClient.getUserInfoVo(albumInfo.getUserId());
            Assert.notNull(userInfoVoResult, "查询作者信息失败");
            UserInfoVo userInfoVo = userInfoVoResult.getData();
            Assert.notNull(userInfoVo, "查询作者信息失败");
            albumInfoIndex.setAnnouncerName(userInfoVo.getNickname());
            return 1;
        }, threadPoolExecutor).exceptionally(throwable -> {
            log.error("查询作者信息失败{}", throwable.getMessage());
            return 0;
        });
        //统计信息，根据专辑id查询专辑的统计数据；
        albumInfoIndex.setPlayStatNum(new Random().nextInt(1000));
        albumInfoIndex.setSubscribeStatNum(new Random().nextInt(1000));
        albumInfoIndex.setBuyStatNum(new Random().nextInt(1000));
        albumInfoIndex.setCommentStatNum(new Random().nextInt(1000));
        albumInfoIndex.setHotScore(Double.valueOf(new Random().nextInt(1000)));
        //利用es的api进行数据保存
        try {
            if (null == albumInfoCompletableFuture.get() || 0 == cateCompletableFuture.get() || 0 == userCompletableFuture.get()) {
                throw new GuiguException(20001, "远程调用获取数据失败");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        //数据汇总
        albumIndexRepository.save(albumInfoIndex);
    }

    @Override
    public AlbumSearchResponseVo search(AlbumIndexQuery albumIndexQuery) {
        //  1. 先获取到用户检索的DSL语句;
        SearchRequest searchRequest = this.queryBuildDsl(albumIndexQuery);
        SearchResponse<AlbumInfoIndex> searchResponse = null;
        try {
            searchResponse = elasticsearchClient.search(searchRequest, AlbumInfoIndex.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //  赋值--private List<AlbumInfoIndexVo> list = new ArrayList<>();
        AlbumSearchResponseVo albumSearchResponseVo = this.parseResultData(searchResponse);
        //  其他属性赋值；
        albumSearchResponseVo.setTotal(searchResponse.hits().total().value());
        albumSearchResponseVo.setPageNo(albumIndexQuery.getPageNo());
        albumSearchResponseVo.setPageSize(albumIndexQuery.getPageSize());
        //  计算总页数; 10 3 4 | 9 3 3
        //  Long totalPages = albumSearchResponseVo.getTotal()%albumIndexQuery.getPageSize()==0?albumSearchResponseVo.getTotal()/albumIndexQuery.getPageSize():albumSearchResponseVo.getTotal()/albumIndexQuery.getPageSize()+1;
        Long totalPages = (albumSearchResponseVo.getTotal() + albumIndexQuery.getPageSize() - 1) / albumIndexQuery.getPageSize();
        albumSearchResponseVo.setTotalPages(totalPages);
        //  返回数据;
        return albumSearchResponseVo;
    }

    private AlbumSearchResponseVo parseResultData(SearchResponse<AlbumInfoIndex> searchResponse) {
        return null;
    }

    private SearchRequest queryBuildDsl(AlbumIndexQuery albumIndexQuery) {
        return null;
    }

    @Override
    public void lowerAlbum(Long albumId) {
        //利用es的api进行删除数据
        albumIndexRepository.deleteById(albumId);
    }
}
