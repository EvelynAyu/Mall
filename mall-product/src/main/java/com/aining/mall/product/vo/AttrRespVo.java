package com.aining.mall.product.vo;

import lombok.Data;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/7/28 00:54
 */
@Data
public class AttrRespVo extends AttrVo{
    /**
     * 规格参数所属分类
     */
    private String catelogName;

    /**
     * 规格参数所属分组
     */
    private String groupName;

    /**
     * 修改时回显所属分组
     */
    private Long[] catelogPath;

}
