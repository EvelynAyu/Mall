package com.aining.mall.product.feign;

import com.aining.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/9/5 15:54
 */
@FeignClient("mall-order")
public interface OrderFeignService {
    @GetMapping("/order/orderitem/buyOrNot")
    String checkPurchase(@RequestParam("spuId") Long spuId);
}
