package com.aining.mall.ware.feign;

import com.aining.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/1 17:22
 */
/**
 * 请求经过网关：让这个远程调用给网关服务发请求，请求路径为：/api/product/skuinfo/info/{skuId}
 * 请求不经过网关直接给服务发送：让这个远程调用给对应服务发请求，请求路径为：/product/skuinfo/info/{skuId}
 */

@FeignClient("mall-product")
public interface ProductFeignService {
    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);
}
