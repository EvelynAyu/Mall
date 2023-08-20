package com.aining.mall.search;

import com.aining.mall.search.config.MallElasticSearchConfig;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MallSearchApplicationTests {
    @Autowired
    private RestHighLevelClient client;

    @Test
    public void contextLoads() {
        System.out.println(client);
    }

    @Test
    public void searchData() throws IOException {
        // 1. 创建检索请求
        SearchRequest searchRequest = new SearchRequest();
        // 指定索引
        searchRequest.indices("bank");
        // 指定DSL 检索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 构造检索条件
        sourceBuilder.query(QueryBuilders.matchQuery("address","mill"));

        System.out.println(sourceBuilder.toString());
        searchRequest.source(sourceBuilder);

        // 2. 执行检索,返回响应
        SearchResponse searchResponse = client.search(searchRequest, MallElasticSearchConfig.COMMON_OPTIONS);
        // 3. 分析结果
        System.out.println(searchResponse.toString());
    }

    @Test
    public void splitTest(){
        String s = "_600";
        String[] s1 = s.split("_");
        System.out.println("长度"+s1.length);
        System.out.println(s1[0] + "+" + s1[s1.length - 1]);

        String s2 = "300_500";
        s1 = s2.split("_");
        System.out.println("长度"+s1.length);
        System.out.println(s1[0] + "+" + s1[s1.length - 1]);


        String s3 = "500_";
        s1 = s3.split("_");
        System.out.println("长度"+s1.length);
        System.out.println(s1[0] + "+" + s1[s1.length - 1]);
    }

}
