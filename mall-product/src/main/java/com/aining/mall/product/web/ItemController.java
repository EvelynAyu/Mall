package com.aining.mall.product.web;

import com.aining.mall.product.service.SkuInfoService;
import com.aining.mall.product.vo.voForItem.SkuItemVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/8/20 18:03
 */
@Controller
public class ItemController {

    @Resource
    SkuInfoService skuInfoService;

    /**
     * 展示当前sku的详情
     * @param skuId
     * @return
     */
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {

        System.out.println("准备查询" + skuId + "详情");

        // 查询详细内容
        SkuItemVo skuItemVo = skuInfoService.item(skuId);
        model.addAttribute("item", skuItemVo);

        return "item";
    }
}
