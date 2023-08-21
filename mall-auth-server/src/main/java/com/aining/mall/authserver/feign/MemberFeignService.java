package com.aining.mall.authserver.feign;

import com.aining.common.utils.R;
import com.aining.mall.authserver.vo.UserLoginVo;
import com.aining.mall.authserver.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/21 18:43
 */
@FeignClient("mall-member")
public interface MemberFeignService {

    @PostMapping(value = "/member/member/register")
    R register(@RequestBody UserRegisterVo vo);

    @PostMapping(value = "/member/member/login")
    R login(@RequestBody UserLoginVo vo);

}
