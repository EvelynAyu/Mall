package com.aining.mall.authserver.feign;

import com.aining.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/21 15:41
 */

@FeignClient("mall-third-party")
public interface ThirdPartyFeignService {
    @GetMapping(value = "/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
