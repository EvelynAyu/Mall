package com.aining.mall.order.service.impl;

import com.aining.common.utils.R;
import com.aining.common.vo.MemberResponseVo;
import com.aining.mall.order.entity.OrderEntity;
import com.aining.mall.order.feign.ProductFeignService;
import com.aining.mall.order.interceptor.LoginUserInterceptor;
import com.aining.mall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aining.common.utils.PageUtils;
import com.aining.common.utils.Query;

import com.aining.mall.order.dao.OrderItemDao;
import com.aining.mall.order.entity.OrderItemEntity;
import com.aining.mall.order.service.OrderItemService;

import javax.servlet.http.HttpSession;

import static com.aining.common.constant.AuthServerConstant.LOGIN_USER;


@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {
    @Autowired
    OrderService orderService;
    
    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    HttpSession session;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public Boolean checkPurchase(Long spuId) {
        //获取登陆人的信息
        MemberResponseVo loginUser = (MemberResponseVo) session.getAttribute(LOGIN_USER);
        if(loginUser != null){
            Long memberId = loginUser.getId();
            List<OrderEntity> orderEntityList = orderService.getBaseMapper().selectList(new QueryWrapper<OrderEntity>().eq("member_id", memberId));
            for (OrderEntity orderEntity : orderEntityList) {
                String orderSn = orderEntity.getOrderSn();
                Integer count = this.baseMapper.selectCount(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn).eq("spu_id", spuId));
                if(count != 0){
                    return true;
                }
            }
        }
        return false;
    }


}