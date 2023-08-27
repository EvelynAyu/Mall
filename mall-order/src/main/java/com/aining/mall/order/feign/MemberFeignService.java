package com.aining.mall.order.feign;

import com.aining.mall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/24 02:19
 */

@FeignClient("mall-member")
public interface MemberFeignService {
    @GetMapping(value = "/member/memberreceiveaddress/{memberId}/addresses")
    public List<MemberAddressVo> getAddresses(@PathVariable("memberId") Long memberId);
}
