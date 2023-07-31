package com.aining.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/7/31 17:59
 */
@Data
public class SkuReductionTo {
    /**
     * sku的优惠及满减信息：sms_sku_ladder
     */
    private Long skuId;
    private int fullCount;
    private BigDecimal discount;
    // 这里的countStatus就是SkuLadderEntity的addOther
    private int countStatus;

    /**
     * sku的满减信息：sms_sku_full_reduction
     */
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;

    /**
     * sms_member_price
     */
    private List<MemberPrice> memberPrice;
}
