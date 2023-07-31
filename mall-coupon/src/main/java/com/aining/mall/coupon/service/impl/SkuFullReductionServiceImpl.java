package com.aining.mall.coupon.service.impl;

import com.aining.common.to.MemberPrice;
import com.aining.common.to.SkuReductionTo;
import com.aining.mall.coupon.entity.MemberPriceEntity;
import com.aining.mall.coupon.entity.SkuLadderEntity;
import com.aining.mall.coupon.service.MemberPriceService;
import com.aining.mall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aining.common.utils.PageUtils;
import com.aining.common.utils.Query;

import com.aining.mall.coupon.dao.SkuFullReductionDao;
import com.aining.mall.coupon.entity.SkuFullReductionEntity;
import com.aining.mall.coupon.service.SkuFullReductionService;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {
    @Autowired
    SkuLadderService skuLadderService;
    @Autowired
    MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        //1. sms_sku_ladder
        SkuLadderEntity ladderEntity = new SkuLadderEntity();
        ladderEntity.setSkuId(skuReductionTo.getSkuId());
        ladderEntity.setFullCount(skuReductionTo.getFullCount());
        ladderEntity.setDiscount(skuReductionTo.getDiscount());
        ladderEntity.setAddOther(skuReductionTo.getCountStatus());
        if(skuReductionTo.getFullCount() > 0){
            skuLadderService.save(ladderEntity);
        }


        //2. 保存满减打折和会员价格sms_sku_full_reduction
        SkuFullReductionEntity reductionEntity = new SkuFullReductionEntity();
        if(reductionEntity.getFullPrice() != null && reductionEntity.getFullPrice().compareTo(new BigDecimal("0")) == 1){
            BeanUtils.copyProperties(skuReductionTo,reductionEntity);
            this.save(reductionEntity);
        }

        //3. 保存sms_member_price
        List<MemberPrice> memberPriceList = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> memberPriceEntityList = memberPriceList.stream().filter((memberPriceEntity)->{
            return memberPriceEntity.getPrice().compareTo(new BigDecimal("0")) == 1;
        }).map((memberPrice) -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            memberPriceEntity.setSkuId(skuReductionTo.getSkuId());
            memberPriceEntity.setMemberLevelId(memberPrice.getId());
            memberPriceEntity.setMemberLevelName(memberPrice.getName());
            memberPriceEntity.setMemberPrice(memberPrice.getPrice());
            memberPriceEntity.setAddOther(1);
            return memberPriceEntity;
        }).collect(Collectors.toList());
        memberPriceService.saveBatch(memberPriceEntityList);
    }

}