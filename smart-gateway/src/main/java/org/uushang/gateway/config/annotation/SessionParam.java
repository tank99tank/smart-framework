package org.uushang.gateway.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * 单个参数
 *
 * @author liyue
 * @version v1
 * @create 2019-10-10 16:52:35
 * @copyright www.liderong.cn
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SessionParam {

    Param name();

    boolean required() default true;

}