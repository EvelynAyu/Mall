package com.aining.mall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/21 23:21
 */
public class CartVo {
    List<CartItemVo> items;
    /**
     * 商品数量
     */
    private Integer countNum;

    /**
     * 商品类型数量
     */
    private Integer countType;

    /**
     * 商品总价
     */
    private BigDecimal totalAmount;

    /**
     * 减免价格
     */
    private BigDecimal reduce = new BigDecimal("0.00");

    public List<CartItemVo> getItems() {
        return items;
    }

    public void setItems(List<CartItemVo> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int countNum = 0;
        if(items != null && items.size() > 0){
            for (CartItemVo item : items) {
                countNum += item.getCount();
            }
        }
        return countNum;
    }

    /**
     * 计算购物商品类型
     * @return
     */
    public Integer getCountType() {
        int countType = 0;
        if(items != null && items.size() > 0){
            for (CartItemVo item : items) {
                countType += 1;
            }
        }
        return countType;
    }

    /**
     * 计算购物项总价
     * @return
     */
    public BigDecimal getTotalAmount() {
        BigDecimal totalAmount = new BigDecimal("0");
        if(items != null && items.size() > 0){
            for (CartItemVo item : items) {
                if (item.getCheck()) {
                    totalAmount = totalAmount.add(item.getTotalPrice());
                }
            }
        }
        // 计算优惠后的价格
        return totalAmount.subtract(getReduce());
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
