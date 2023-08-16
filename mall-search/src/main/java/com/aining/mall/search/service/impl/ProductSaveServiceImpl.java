package com.aining.mall.search.service.impl;

import com.aining.common.to.es.SkuESModel;
import com.aining.mall.search.config.MallElasticSearchConfig;
import com.aining.mall.search.constant.ESConstant;
import com.aining.mall.search.service.ProductSaveService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.index.IndexReader;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/16 14:00
 */
@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {
    @Autowired
    private RestHighLevelClient esRestClient;
    /**
     * 上架，保存所有的sku数据
     * @param skuESModels
     */
    @Override
    public boolean productStatusUp(List<SkuESModel> skuESModels) throws IOException {
        // 1. 给es中建立索引：product，建立映射关系,在kibana完成
        // 2. 给es中保存数据
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuESModel skuESModel : skuESModels){
            // 构造保存请求
            IndexRequest indexRequest = new IndexRequest(ESConstant.PRODUCT_INDEX);
            indexRequest.id(skuESModel.getSkuId().toString());
            String jsonString = JSON.toJSONString(skuESModel);
            indexRequest.source(jsonString, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = esRestClient.bulk(bulkRequest, MallElasticSearchConfig.COMMON_OPTIONS);
        //TODO 如果批量错误

        // 有错误则hasFailures为true，否则为false
        boolean hasFailures = bulk.hasFailures();

        List<String> collect = Arrays.asList(bulk.getItems()).stream().map(item -> {
            return item.getId();
        }).collect(Collectors.toList());

        log.info("商品上架完成:{},返回数据:{}",collect,bulk.toString());

        return hasFailures;
    }
}
