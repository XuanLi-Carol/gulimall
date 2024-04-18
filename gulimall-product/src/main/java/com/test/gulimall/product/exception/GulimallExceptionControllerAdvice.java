package com.test.gulimall.product.exception;

import com.test.common.exception.BizCodeEnum;
import com.test.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

//@ControllerAdvice
//@ResponseBody
@RestControllerAdvice(basePackages = {"com.test.gulimall.product.controller"})
@Slf4j
public class GulimallExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e){
        log.error("数据校验异常：{},{}",e.getMessage(),e.getClass());
        BindingResult bindingResult = e.getBindingResult();

        Map<String,String> resMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach((item)->{
            resMap.put(item.getField(),item.getDefaultMessage());
        });
        return R.error(BizCodeEnum.VALIDATE_EXCEPTION.getCode(), BizCodeEnum.VALIDATE_EXCEPTION.getMessage()).put("data",resMap);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleUnknownException(Throwable e){
        log.error("UNKNOWN ERROR: {}", ExceptionUtils.getStackTrace(e));
        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMessage());
    }

}
