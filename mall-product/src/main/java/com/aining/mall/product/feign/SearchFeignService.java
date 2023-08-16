package com.aining.mall.product.feign;

import com.aining.common.to.es.SkuESModel;
import com.aining.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/16 14:22
 */
@FeignClient("mall-search")
public interface SearchFeignService {
    @PostMapping("/search/save/product")
    public R productStatusUp(@RequestBody List<SkuESModel> skuESModels);
}
