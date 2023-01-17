package com.aining.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.aining.common.utils.PageUtils;
import com.aining.mall.ware.entity.UndoLogEntity;

import java.util.Map;

/**
 * 
 *
 * @author aining
 * @email aininglai@outlook.com
 * @date 2022-10-17 16:59:32
 */
public interface UndoLogService extends IService<UndoLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

