package com.aining.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/7/31 17:10
 */
@Data
public class SpuBoundsTo {
    /**
     * SpuId
     */
    private Long spuId;
    /**
     * 成长积分
     */
    private BigDecimal growBounds;
    /**
     * 购物积分
     */
    private BigDecimal buyBounds;
}
