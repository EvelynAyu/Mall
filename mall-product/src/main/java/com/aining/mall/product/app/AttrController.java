package com.aining.mall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.aining.mall.product.entity.ProductAttrValueEntity;
import com.aining.mall.product.service.ProductAttrValueService;
import com.aining.mall.product.vo.AttrRespVo;
import com.aining.mall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.aining.mall.product.service.AttrService;
import com.aining.common.utils.PageUtils;
import com.aining.common.utils.R;



/**
 * 商品属性
 *
 * @author aining
 * @email aininglai@outlook.com
 * @date 2022-10-16 22:11:17
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 前端：/product/attr/${type}/list/${this.catId}
     * 后端：/product/attr/base/list/id?t=1690499402882&page=1&limit=10&key=
     */
    @GetMapping("/{attrType}/list/{catelogId}")
    public R baseAttrList(@RequestParam Map<String, Object> params,
                          @PathVariable("catelogId") Long catelogId,
                          @PathVariable("attrType") String attrType){
        PageUtils page = attrService.queryAttrPage(params, catelogId, attrType);
        return R.ok().put("page",page);
    }

    /**
     * 查询商品的规格属性:pms_product_attr_value
     */
    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrListForSpu(@PathVariable("spuId") Long spuId){
        List<ProductAttrValueEntity> attrValueEntity = productAttrValueService.baseAttrListForSpu(spuId);
        return R.ok().put("data", attrValueEntity);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId){
        AttrRespVo attrRespVo = attrService.getAttrDetail(attrId);
        return R.ok().put("attr", attrRespVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrVo attr){
		attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrVo attr){
		attrService.updateAttr(attr);

        return R.ok();
    }
    /**
     * 修改
     */
    @PostMapping("/update/{spuId}")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId,
                           @RequestBody List<ProductAttrValueEntity> productAttrValueEntity){
        productAttrValueService.updateSpuAttr(spuId,productAttrValueEntity);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
