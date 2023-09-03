package com.aining.mall.search.controller;

import com.aining.common.exception.BizCodeEnume;
import com.aining.common.to.es.SkuESModel;
import com.aining.common.utils.R;
import com.aining.mall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/16 13:56
 */
@RestController
@RequestMapping("/search/save")
@Slf4j
public class ElassticSaveController {
    @Autowired
    ProductSaveService productSaveService;
    /**
     * 保存上架商品
     */
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuESModel> skuESModels){
        // 用flag标记上架是否成功
        boolean productUpFlag = false;
        try {
            productUpFlag = productSaveService.productStatusUp(skuESModels);
        } catch (Exception e) {
            log.error("ElasticsearchController商品上架错误：{}",e);
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }

        // 最后判断商品上架是否成功
        if(!productUpFlag){
            return R.ok();
        }
        else {
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }
    }
}
