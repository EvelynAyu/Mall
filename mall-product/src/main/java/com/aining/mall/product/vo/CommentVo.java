package com.aining.mall.product.vo;

import com.aining.mall.product.entity.SpuCommentEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/9/4 22:40
 */

@Data
public class CommentVo {
    /**
     * id
     */
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * spu_id
     */
    private Long spuId;
    /**
     * 商品名字
     */
    private String spuName;
    /**
     * 会员昵称
     */
    private String memberNickName;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 评论图片：url:资源路径
     */
    private String resources;
    /**
     * 内容
     */
    private String content;
    /**
     * 评论类型[0 - 对商品的直接评论，1 - 对评论的回复]
     */
    private Integer commentType;
    /**
     * 评论的回复
     */
    List<SpuCommentEntity> commentReplyList;
}
