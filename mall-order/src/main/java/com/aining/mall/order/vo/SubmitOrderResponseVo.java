package com.aining.mall.order.vo;

import com.aining.mall.order.entity.OrderEntity;
import lombok.Data;

/**
 * 下单成功/失败的信息，返回给前端
 */
@Data
public class SubmitOrderResponseVo {

    private OrderEntity order;

    /** 错误状态码：0-成功 **/
    private Integer code;


}
