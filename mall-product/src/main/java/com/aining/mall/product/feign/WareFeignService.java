package com.aining.mall.product.feign;

import com.aining.common.utils.R;
import com.aining.common.vo.SkuHasStockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/16 11:37
 */
@FeignClient("mall-ware")
public interface WareFeignService {
    @PostMapping("/ware/waresku/hasStock")
    R getSkusStock(@RequestBody List<Long> skuIds);
}
