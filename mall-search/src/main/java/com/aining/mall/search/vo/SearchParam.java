package com.aining.mall.search.vo;

/**
 * @Description: 封装页面所有可能传递过来的查询条件
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/19 15:32
 */

import lombok.Data;

import java.util.List;

/**
 * 封装检索条件
 */
@Data
public class SearchParam {
    /**
     * 页面传递过来的全文匹配关键字
     */
    private String keyword;

    /**
     * 通过某个分类进行检索：品牌id,可以多选
     */
    private List<Long> brandId;

    /**
     * 三级分类id
     */
    private Long catalog3Id;

    /**
     * 排序条件：sort=price/salecount/hotscore_desc/asc
     */
    private String sort;

    /**
     * 是否显示有货: 0-无库存，1-有库存
     */
    private Integer hasStock;

    /**
     * 价格区间查询
     */
    private String skuPrice;

    /**
     * 按照属性进行筛选
     */
    private List<String> attrs;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 原生的所有查询条件
     */
    private String _queryString;
}
