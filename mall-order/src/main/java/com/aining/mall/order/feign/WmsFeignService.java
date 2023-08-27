package com.aining.mall.order.feign;

import com.aining.common.utils.R;
import com.aining.mall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/24 17:09
 */

@FeignClient("mall-ware")
public interface WmsFeignService {

    @PostMapping("/ware/waresku/hasStock")
    R getSkusStock(@RequestBody List<Long> skuIds);

    /**
     * 获取运费信息
     * @return
     */
    @GetMapping(value = "/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long addrId);

    @PostMapping(value = "/ware/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo vo);
}
