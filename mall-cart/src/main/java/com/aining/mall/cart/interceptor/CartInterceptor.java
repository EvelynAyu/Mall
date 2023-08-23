package com.aining.mall.cart.interceptor;

import com.aining.common.constant.AuthServerConstant;
import com.aining.common.constant.CartConstant;
import com.aining.common.vo.MemberResponseVo;
import com.aining.mall.cart.to.UserInfoTo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.UUID;

import static com.aining.common.constant.CartConstant.TEMP_USER_COOKIE_NAME;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/21 23:46
 * @Description 拦截器，用于判断用户登陆状态
 */
public class CartInterceptor implements HandlerInterceptor {

    // 同一个线程共享数据
    public static ThreadLocal<UserInfoTo> toThreadLocal = new ThreadLocal<>();

    /**
     * 在目标方法执行之前执行
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo userInfoTo = new UserInfoTo();
        HttpSession session = request.getSession();
        MemberResponseVo memberResponseVo = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if(memberResponseVo != null){
            // 用户登陆
            userInfoTo.setUserId(memberResponseVo.getId());
        }

        // 如果是临时用户且拥有user-key
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            // 遍历cookie
            for (Cookie cookie : cookies) {
                //user-key
                String name = cookie.getName();
                if (name.equals(TEMP_USER_COOKIE_NAME)) {
                    // 设置user-key
                    userInfoTo.setUserKey(cookie.getValue());
                    //标记为已是临时用户
                    userInfoTo.setTempUser(true);
                }
            }
        }

        // 如果是临时用户但没有user-key
        if(StringUtils.isEmpty(userInfoTo.getUserKey())){
            // 没有user-key则设置新的user-key
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }

        // 目标方法执行之前
        toThreadLocal.set(userInfoTo);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        UserInfoTo userInfoTo = toThreadLocal.get();
        if(!userInfoTo.getTempUser()){
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME,userInfoTo.getUserKey());
            cookie.setDomain("mall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
