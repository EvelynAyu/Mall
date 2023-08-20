package com.aining.mall.search.feign;

import com.aining.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/20 03:21
 */
@FeignClient("mall-product")
public interface ProductFeignService {

    @GetMapping("/product/attr/info/{attrId}")
    public R attrInfo(@PathVariable("attrId") Long attrId);

    @GetMapping("product/brand/infos")
    public R brandsInfo(@RequestParam("brandIds") List<Long> brandIds);

}
