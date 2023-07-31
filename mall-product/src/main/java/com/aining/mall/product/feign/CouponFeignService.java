package com.aining.mall.product.feign;


import com.aining.common.to.SkuReductionTo;
import com.aining.common.to.SpuBoundsTo;
import com.aining.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * mall-product服务远程调用mall-coupon服务的接口
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/7/31 17:02
 */
@FeignClient("mall-coupon")
public interface CouponFeignService {
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundsTo spuBoundsTo);

    @PostMapping("/coupon/skufullreduction/saveFullreduction")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
