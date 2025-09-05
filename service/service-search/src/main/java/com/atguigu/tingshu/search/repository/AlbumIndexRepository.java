package com.atguigu.tingshu.search.repository;

import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author 陆小凤
 * @version 1.0
 * @description: TODO
 * @date 2025/9/4 17:13
 */
public interface AlbumIndexRepository extends ElasticsearchRepository<AlbumInfoIndex,Long> {

}
