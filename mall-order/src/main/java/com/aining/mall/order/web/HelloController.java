package com.aining.mall.order.web;

import com.aining.mall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.UUID;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/24 00:55
 */

@Controller
public class HelloController {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @ResponseBody
    @GetMapping("/test/createOrder")
    public String createOrderTest(){
        // 订单下单成功
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(UUID.randomUUID().toString());
        orderEntity.setModifyTime(new Date());

        // 给MQ发消息
        rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",orderEntity);
        return "OK";
    }

    @GetMapping("/{page}.html")
    public String testPage(@PathVariable("page") String page){
        return page;
    }
}
