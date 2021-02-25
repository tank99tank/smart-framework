package org.uushang.gateway.config.annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.uushang.gateway.config.context.ReactiveRequestContextHolder;
import org.uushang.gateway.config.exception.MissingServerRequestParameterException;
import org.uushang.gateway.constant.Headers;
import org.uushang.gateway.security.token.JwtAccessTokenInfo;
import org.uushang.gateway.security.token.JwtClientUserInfo;

import java.lang.reflect.Parameter;
import java.util.Objects;

/**
 *
 * 对接口进行切面进行参数的注入，使其能在接口中正常获取到需要的常用参数
 * 目前仅对Map和常规参数类型添加了支持
 *
 * @author liyue
 * @version v1
 * @create 2019-10-10 16:52:35
 * @copyright www.liderong.cn
 */
@Aspect
@Component
public class SessionParamWrapper {

    @Autowired
    private RedisTemplate redisTemplate;

    @Pointcut("@annotation(com.owinfo.gateway.config.annotation.SessionParamAutowired)")
    public void annotationWrapper(){}

    @Around("annotationWrapper()")
    public Object annotationHandler(ProceedingJoinPoint joinPoint) throws Throwable {

        ServerHttpRequest request = ReactiveRequestContextHolder.get();

        Objects.requireNonNull(request, "Cannot retrieve the current request from the context");

        Parameter[] parameters = ((MethodSignature) joinPoint.getSignature()).getMethod().getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            SessionParam sessionParam = parameters[i].getAnnotation(SessionParam.class);
            if (Objects.nonNull(sessionParam)) {
                Param name = sessionParam.name();
                Object value = this.getValue(name, sessionParam.required(), request);
                args[i] = value;
            }
        }

        return joinPoint.proceed(args);
    }

    private Object getValue(Param name, boolean required, ServerHttpRequest request) {
        Object value = null;
        if (Param.PLATFORM == name) {
            value = request.getHeaders().getFirst(Headers.PLATFORM);
        } else if (Param.IDENTITYID == name) {
            value = request.getHeaders().getFirst(Headers.IDENTITY_ID);
        } else if (Param.ACCESSTOKEN == name) {
            value = request.getHeaders().getFirst(Headers.AUTHENTICATION);
        } else {
            //获取用户相关信息
            //这里要改一下 不应该每次获取值都去序列化一次 如果需要用户信息的地方比较多 就提前进行获取
            String jti = request.getHeaders().getFirst(Headers.IDENTITY_ID);
            Objects.requireNonNull(jti, "Unable to obtain identity id from request information");

            Object obj = redisTemplate.opsForValue().get(jti);
            Objects.requireNonNull(obj, "Unable to read information based on identity id, probably because id has expired");

            JwtAccessTokenInfo tokenInfo = (JwtAccessTokenInfo) obj;
            JwtClientUserInfo userInfo = tokenInfo.getUserInfo();
            switch (name) {
                case USERID : value = userInfo.getUserId(); break;
                case USERNO : value = userInfo.getUserNo(); break;
                case USERNAME : value = userInfo.getUsername(); break;
                default:
                    break;
            }
        }

        if (value == null && required) {
            throw new MissingServerRequestParameterException
                    ("Cannot get the required value from the request information, -> " + name.getName());
        }
        return value;
    }
}