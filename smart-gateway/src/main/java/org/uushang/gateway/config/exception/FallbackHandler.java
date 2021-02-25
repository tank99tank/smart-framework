package org.uushang.gateway.config.exception;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * 处理请求路由的熔断降级
 *
 *
 * @author liyue
 * @version v1
 * @create 2019-04-01 17:20:25
 * @copyright www.liderong.cn
 */
@RestController
public class FallbackHandler {

//    @RequestMapping("/fallback")
//    public Mono<Result> fallback() {
//        log.warn("===> Gateway timeout [{} {} {}]");
//        return Mono.just(Result.build(Status.FAILURE, "网关处理超时"));
//    }

    /**
     * 请求超时会执行降级 但是没有可用的服务实例时也会进入降级
     * 为了在日志中更精确的区分是超时还是服务不可用 在降级方法中抛出异常 交由ErrorHandler去处理
     */
    @RequestMapping("/fallback")
    public void fallback() {
        throw new FallbackException();
    }
}