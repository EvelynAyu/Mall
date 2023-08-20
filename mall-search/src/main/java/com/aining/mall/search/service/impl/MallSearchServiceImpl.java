package com.aining.mall.search.service.impl;

import com.aining.common.to.es.SkuESModel;
import com.aining.common.utils.R;
import com.aining.mall.search.config.MallElasticSearchConfig;
import com.aining.mall.search.constant.ESConstant;
import com.aining.mall.search.feign.ProductFeignService;
import com.aining.mall.search.service.MallSearchService;
import com.aining.mall.search.vo.AttrResponseVo;
import com.aining.mall.search.vo.BrandVo;
import com.aining.mall.search.vo.SearchParam;
import com.aining.mall.search.vo.SearchResult;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/19 15:35
 */
@Slf4j
@Service
public class MallSearchServiceImpl implements MallSearchService {
    @Autowired
    private RestHighLevelClient esRestClient;

    @Resource
    private ProductFeignService productFeignService;

    /**
     * 根据检索条件到es中查询结果并返回给前端
     * 动态构建出查询需要的DSL语句
     * @param param
     * @return
     */
    @Override
    public SearchResult search(SearchParam param) {
        // 1、动态构建出查询需要的DSL语句
        SearchResult result = null;

        // 2. 准备检索请求
        SearchRequest searchRequest = buildSearchRequest(param);

        try {
            // 3.执行检索请求
            SearchResponse response = esRestClient.search(searchRequest, MallElasticSearchConfig.COMMON_OPTIONS);

            // 4.分析响应数据，封装成我们需要的格式
            result = buildSearchResult(response, param);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 准备检索请求
     * 模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存），排序，分页，高亮，聚合分析
     * 根据DSL.json中的查询语句一步步写
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        // searchSourceBuilder用于构建检索请求
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        /**
         * 模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存）
         * Bool查询中有 "must"和"filter"两个query
         */
        // 1. 构建bool query
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        // 1.1 构建bool-nust:通过搜索栏检索
        if(!StringUtils.isEmpty(param.getKeyword())){
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }

        // 1.2 bool-fiter
        // 1.2.1 通过分类栏查询：catelogId
        if(param.getCatalog3Id() != null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }

        // 1.2.2 brandId
        if(param.getBrandId() != null && param.getBrandId().size() > 0){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId",param.getBrandId()));
        }

        // 1.2.3 hasStock：0-无库存，1-有库存
        if(param.getHasStock() != null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock",param.getHasStock() == 1));
        }

        // 1.2.4 按照价格区间检索：skuPrice
        if(param.getSkuPrice() != null){
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("skuPrice");
            /*
            skuPrice形式为：1_500(大于1小于500)，或_500（小于500）或500_（大于500）
            需要对传来的price参数进行符号分割处理
             */
            String[] prices = param.getSkuPrice().split("_");

            if(prices.length == 2){
                // 1_500 split的结果为[1,500]，长度为2
                // _500 split的结果为["",500]，长度为2
                if(!prices[0].isEmpty()){
                    rangeQueryBuilder.gte(prices[0]);
                }
                rangeQueryBuilder.lte(prices[1]);
            } else if (prices.length == 1) {
                // 说明传来的价格参数是：_500或500_
                if(param.getSkuPrice().startsWith("_")){
                    // _500 split的结果为["",500]，长度为2
                    rangeQueryBuilder.lte(prices[1]);
                }
                if(param.getSkuPrice().endsWith("_")){
                    // 500_ split的结果为[500],长度为1
                    rangeQueryBuilder.gte(prices[0]);
                }
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }

        // 1.2.5 按照属性进行查询，返回属性名和对应的属性值（在ES中Attr为nested）
        List<String> attrs = param.getAttrs();
        if(attrs != null && attrs.size() > 0){
            /*
             attrs的格式：attrs=1_5寸:8寸&2_16G:8G
             意思是：attrId=1的属性对应的属性值有：5寸/8寸， attrId = 2的属性对应的属性值有：16G/8G
             */
            attrs.forEach(attr->{
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

                // 分割属性id和属性值：attrs=1_5寸:8寸 -> [1,5寸:8寸]
                String[] attrSplit = attr.split("_");
                String attrId = attrSplit[0];

                // 分割属性值5寸:8寸 -> [5寸:8寸]
                String[] attrValues = attrSplit[1].split(":");

                boolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                boolQuery.must(QueryBuilders.termsQuery("attrs.attrValue",attrValues));

                NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", boolQuery, ScoreMode.None);
                boolQueryBuilder.filter(nestedQueryBuilder);
            });
        }
        // 封装所有查询条件
        searchSourceBuilder.query(boolQueryBuilder);


        /**
         * 排序，分页，高亮
         */
        //排序
        //形式为sort=hotScore_asc/desc
        if(!StringUtils.isEmpty(param.getSort())){
            String sort = param.getSort();
            String[] sortFields = sort.split("_");

            SortOrder sortOrder = "asc".equalsIgnoreCase(sortFields[1])? SortOrder.ASC:SortOrder.DESC;
            searchSourceBuilder.sort(sortFields[0], sortOrder);
        }

        //分页
        // from = (pageNum - 1) * pagesize
        searchSourceBuilder.from((param.getPageNum()-1)*ESConstant.PRODUCT_PAGESIZE);
        searchSourceBuilder.size(ESConstant.PRODUCT_PAGESIZE);

        // 高亮：只有在有keyword模糊查询时才需要高亮
        if(!StringUtils.isEmpty(param.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");

            searchSourceBuilder.highlighter(highlightBuilder);
        }

        /**
         * 聚合分析
         */
        //1. 按照品牌进行聚合
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);

        //1.1 品牌的子聚合-品牌名聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg")
                .field("brandName").size(1));
        //1.2 品牌的子聚合-品牌图片聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg")
                .field("brandImg").size(1));

        searchSourceBuilder.aggregation(brand_agg);

        //2. 按照分类信息进行聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg");
        catalog_agg.field("catalogId").size(20);

        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));

        searchSourceBuilder.aggregation(catalog_agg);

        //3. 按照属性信息进行聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        //3.1 按照属性ID进行聚合
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attr_agg.subAggregation(attr_id_agg);
        // 3.1.1 在每个属性ID下，按照属性名进行聚合
        TermsAggregationBuilder attr_name_agg = AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1);
        attr_id_agg.subAggregation(attr_name_agg);
        //3.1.2 在每个属性ID下，按照属性值进行聚合
        TermsAggregationBuilder attr_value_agg = AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50);
        attr_id_agg.subAggregation(attr_value_agg);

        searchSourceBuilder.aggregation(attr_agg);


        System.out.println("构建的DSL语句:"+searchSourceBuilder.toString());

        SearchRequest searchRequest = new SearchRequest(new String[]{ESConstant.PRODUCT_INDEX}, searchSourceBuilder);
        return searchRequest;
    }

        /**
         * 构建结果数据
         * 模糊匹配，过滤（按照属性、分类、品牌，价格区间，库存），完成排序、分页、高亮,聚合分析功能
         * @param response
         * @return
         */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {
        // SearchResult为自定义的类，用于封装返回给前端的属性
        SearchResult result = new SearchResult();

        // 1. 返回所有查询到到商品
        SearchHits hits = response.getHits();

        List<SkuESModel> esModels = new ArrayList<>();
        if(hits.getHits() != null && hits.getHits().length > 0){
            // 遍历查询到的所有商品
            for(SearchHit hit : hits.getHits()){
                String sourceAsString = hit.getSourceAsString();
                SkuESModel esModel = JSON.parseObject(sourceAsString, SkuESModel.class);

                // 判断是否按照关键字检索，是就显示高亮，否则不显示
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    //拿到高亮信息显示标题
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String skuTitleValue = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(skuTitleValue);
                }
                esModels.add(esModel);
            }
        }
        result.setProduct(esModels);

        //2、当前商品涉及到的所有属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        //获取属性信息的聚合
        ParsedNested attrsAgg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            // 1个bucket对应1个属性id
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            //1、得到属性的id
            long attrId = bucket.getKeyAsNumber().longValue();
            attrVo.setAttrId(attrId);

            //2、得到属性的名字
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attr_name_agg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);

            //3、得到属性的所有值：1个属性对应多个值
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attr_value_agg");
            List<String> attrValues = attrValueAgg.getBuckets().stream().map(item -> item.getKeyAsString()).collect(Collectors.toList());
            attrVo.setAttrValue(attrValues);

            attrVos.add(attrVo);
        }

        result.setAttrs(attrVos);

        //3、当前商品涉及到的所有品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();

            // 1. 得到品牌id
            long brandId = bucket.getKeyAsNumber().longValue();
            brandVo.setBrandId(brandId);

            // 1.1得到品牌id对应的品牌名称
            ParsedStringTerms brand_name_agg = bucket.getAggregations().get("brand_name_agg");
            String brandName = brand_name_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);

            // 1.2 得到品牌id对应的品牌图片
            ParsedStringTerms brand_img_agg = bucket.getAggregations().get("brand_img_agg");
            String brandImg = brand_img_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImg);

            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        //4、当前商品涉及到的所有分类信息
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");

        for (Terms.Bucket bucket : catalog_agg.getBuckets()) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            // 从bucket中拿到分类id
            String catalogId = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(catalogId));

            // 从bucket中拿到分类名字
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);

        //===============以上可以从聚合信息中获取====================//
        //5、分页信息-页码
        result.setPageNum(param.getPageNum());
        // 5、1分页信息、总记录数
        long total = hits.getTotalHits().value;
        result.setTotal(total);

        //5、2分页信息-总页码-计算: +1是因为PRODUCT_PAGESIZE=2，余数只能是0 或 1
        int totalPages = (int)total % ESConstant.PRODUCT_PAGESIZE == 0 ?
                (int) total / ESConstant.PRODUCT_PAGESIZE : ((int) total / ESConstant.PRODUCT_PAGESIZE + 1);
        result.setTotalPages(totalPages);

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        //6 构建面包屑导航功能 属性
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            List<SearchResult.NavVo> navVos = param.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                //1 分析每个attrs传过来的查询参数值
                //attrs=2_5寸:6寸
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                result.getAttrIds().add(Long.parseLong(s[0]));
                if (r.getCode() == 0) {
                    //正常返回
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {});
                    navVo.setNavName(data.getAttrName());
                } else {
                    //如果失败
                    navVo.setNavName(s[0]);
                }

                //2 取消了面包屑以后 我们要跳转到哪个地方 将请求地址的url里面的当前请求参数置空
                //拿到所有的查询条件 去掉当前
                String replace = replaceQueryString(param, attr, "attrs");
                navVo.setLink("http://search.mall.com/list.html?" + replace);
                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(navVos);
        }

        // 品牌/分类
        if(param.getBrandId() != null && param.getBrandId().size()>0) {
            List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");
            //TODO 远程查询
            R r = productFeignService.brandsInfo(param.getBrandId());
            if (r.getCode() == 0) {
                List<BrandVo> brands = r.getData("brand", new TypeReference<List<BrandVo>>() {});
                StringBuffer buffer = new StringBuffer();
                String replace = "";
                for (BrandVo brandVo : brands) {
                    buffer.append(brandVo.getBrandName() + ";");
                    replace = replaceQueryString(param, brandVo.getBrandId()+"", "brandId");
                }
                navVo.setNavValue(buffer.toString());
                navVo.setLink("http://search.mall.com/list.html?" + replace);
            }
            navs.add(navVo);
        }
        // 将封装结果返回给前端
        return result;
    }
    //编写面包屑的功能时，删除指定请求
    private String replaceQueryString(SearchParam param, String value,String key) {
        String encode = "";
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            //+ 对应浏览器的%20编码
            encode = encode.replace("+","%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return  param.get_queryString().replace("&" + key + "=" + encode, "");
    }
}
