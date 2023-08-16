package com.aining.mall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.aining.mall.ware.vo.SkuHasStockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.aining.mall.ware.entity.WareSkuEntity;
import com.aining.mall.ware.service.WareSkuService;
import com.aining.common.utils.PageUtils;
import com.aining.common.utils.R;



/**
 * 商品库存
 *
 * @author aining
 * @email aininglai@outlook.com
 * @date 2022-10-17 16:59:33
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareSkuEntity wareSku){
		wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareSkuEntity wareSku){
		wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 远程调用：查询sku是否有库存
     */
    @PostMapping("/hasStock")
    public R getSkusStock(@RequestBody List<Long> skuIds){
        // 返回当前skuId和库存量
        List<SkuHasStockVo> hasStockVos = wareSkuService.getSkusHasStock(skuIds);

        return R.ok().setData(hasStockVos);
    }

}
