package com.aining.mall.order.web;

import com.aining.mall.order.config.AlipayTemplate;
import com.aining.mall.order.entity.OrderEntity;
import com.aining.mall.order.service.OrderService;
import com.aining.mall.order.vo.PayVo;
import com.alipay.api.AlipayApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/27 19:02
 */

@Slf4j
@Controller
public class PayWebController {

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private OrderService orderService;


    /**
     * 用户下单:支付宝支付
     * 1、让支付页让浏览器展示
     * 2、支付成功以后，跳转到用户的订单列表页
     * @param orderSn
     * @return
     * @throws AlipayApiException
     */
    @ResponseBody
    @GetMapping(value = "/aliPayOrder",produces = "text/html")
    public String aliPayOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        PayVo payVo = orderService.getOrderPay(orderSn);
        String pay = alipayTemplate.pay(payVo);
        System.out.println(pay);
        return pay;
    }


//    /**
//     * 微信支付
//     * @param orderSn
//     * @return
//     */
//    @GetMapping(value = "/weixinPayOrder")
//    public String weixinPayOrder(@RequestParam("orderSn") String orderSn, Model model) {
//
//        OrderEntity orderInfo = orderService.getOrderByOrderSn(orderSn);
//
//        if (orderInfo == null) {
//            throw new RuntimeException("订单不存在");
//        }
//
//        PayRequest request = new PayRequest();
//        request.setOrderName("4559066-最好的支付sdk");
//        request.setOrderId(orderInfo.getOrderSn());
//        request.setOrderAmount(0.01);
//        request.setPayTypeEnum(WXPAY_NATIVE);
//
//        PayResponse payResponse = bestPayService.pay(request);
//        payResponse.setOrderId(orderInfo.getOrderSn());
//        log.info("发起支付 response={}", payResponse);
//
//        //传入前台的二维码路径生成支付二维码
//        model.addAttribute("codeUrl",payResponse.getCodeUrl());
//        model.addAttribute("orderId",payResponse.getOrderId());
//        model.addAttribute("returnUrl",wxPayConfig.getReturnUrl());
//
//        return "createForWxNative";
//    }


    //根据订单号查询订单状态的API
    @GetMapping(value = "/queryByOrderId")
    @ResponseBody
    public OrderEntity queryByOrderId(@RequestParam("orderId") String orderId) {
        log.info("查询支付记录...");
        return orderService.getOrderByOrderSn(orderId);
    }



}

