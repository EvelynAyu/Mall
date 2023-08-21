package com.aining.mall.member.service;

import com.aining.mall.member.exception.PhoneException;
import com.aining.mall.member.exception.UsernameException;
import com.aining.mall.member.vo.MemberUserLoginVo;
import com.aining.mall.member.vo.MemberUserRegisterVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.aining.common.utils.PageUtils;
import com.aining.mall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author aining
 * @email aininglai@outlook.com
 * @date 2022-10-17 16:40:24
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberUserRegisterVo vo);

    /**
     * 判断邮箱是否重复
     * @param phone
     * @return
     */
    void checkPhoneUnique(String phone) throws PhoneException;

    /**
     * 判断用户名是否重复
     * @param userName
     * @return
     */
    void checkUserNameUnique(String userName) throws UsernameException;

    MemberEntity login(MemberUserLoginVo vo);
}

