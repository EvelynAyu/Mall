package com.aining.mall.search.service;

import com.aining.mall.search.vo.SearchParam;
import com.aining.mall.search.vo.SearchResult;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/19 15:35
 */
public interface MallSearchService {
     SearchResult search(SearchParam param);
     /**
      *
      * @param searchParam 检索的所有参数
      * @return 返回检索的结果，里面包含页面需要的所有信息
      */
     
}
