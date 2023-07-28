package com.aining.mall.product.exception;

import com.aining.common.exception.BizCodeEnume;
import com.aining.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Aining aininglai@outlook.com
 * @version 1.0
 * @date 2023/7/25 02:28
 */

// 用于统一处理异常
@Slf4j
@RestControllerAdvice(basePackages = "com.aining.mall.product.controller")
public class mallExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e){
        log.error("数据校验出现问题{}, 异常类型:{}",e.getMessage(), e.getClass());
        BindingResult result = e.getBindingResult();
        if (result.hasErrors()) {
            Map<String, String> errorMap = new HashMap<>();
            // 获取校验的错误结果
            result.getFieldErrors().forEach((item) -> {
                // FieldError 获取的错误提示
                String message = item.getDefaultMessage();
                // 获取发生错误字段名
                String field = item.getField();
                errorMap.put(field, message);
            });
            return R.error(BizCodeEnume.VALID_EXCEPTION.getCode(), BizCodeEnume.VALID_EXCEPTION.getMsg()).put("data", errorMap);
        }
        return R.error();
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable){
        log.error("捕捉异常", throwable);
        return R.error(BizCodeEnume.UNKNOW_EXCEPTION.getCode(), BizCodeEnume.UNKNOW_EXCEPTION.getMsg());
    }
}
