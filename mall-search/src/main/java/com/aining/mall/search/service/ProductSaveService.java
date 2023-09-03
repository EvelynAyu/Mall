package com.aining.mall.search.service;

import com.aining.common.to.es.SkuESModel;

import java.io.IOException;
import java.util.List;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/16 14:00
 */
public interface ProductSaveService {
    boolean productStatusUp(List<SkuESModel> skuESModels) throws IOException;

}
