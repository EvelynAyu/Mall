package com.aining.mall.ware.vo;

import lombok.Data;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/1 16:36
 */
@Data
public class PurchaseItemDoneVo {
    /**
     * 采购需求id
     */
    private Long itemId;
    /**
     * 采购需求状态
     */
    private Integer status;
    /**
     * 采购需求失败原因
     */
    private String reason;
}

