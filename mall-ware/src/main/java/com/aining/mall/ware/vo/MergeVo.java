package com.aining.mall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/1 15:27
 */
@Data
public class MergeVo {
    /**
     * 整单id
     */
    private Long purchaseId;
    /**
     * 合并项集合
     */
    private List<Long> items;
}
