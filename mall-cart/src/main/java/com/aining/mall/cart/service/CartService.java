package com.aining.mall.cart.service;

import com.aining.mall.cart.vo.CartItemVo;
import com.aining.mall.cart.vo.CartVo;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/21 23:20
 */
public interface CartService {

    CartItemVo addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItemVo getCartItem(Long skuId);

    /**
     * 获取购物车里面的信息
     * @return
     */
    CartVo getCart() throws ExecutionException, InterruptedException;
    void clearCartInfo(String cartKey);

    void changeItemCount(Long skuId, Integer num);

    void checkItem(Long skuId, Integer check);

    void deleteIdCartInfo(Integer skuId);

    List<CartItemVo> getUserCartItems();

    /**
     * 获取购物车里面的信息
     * @return
     */
//    CartVo getCart();
}
