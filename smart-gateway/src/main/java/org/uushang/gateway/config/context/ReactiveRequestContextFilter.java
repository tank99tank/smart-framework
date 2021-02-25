package org.uushang.gateway.config.context;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 *
 * reactor does not contain a {@link RequestContextHolder} object that has the same effect as mvc.
 * If you need to get the current request from the context, you need to manually configure it.
 *
 * Bind ServerWebExchange to {@link Context} via {@link ReactiveRequestContextHolder} after intercepting with filter.
 *
 * from {https://gitee.com/596392912/mica/tree/master/mica-boot/src/main/java/net/dreamlu/mica/reactive/context}
 * 使用 {@link Context} 的方式不能解决此问题 存入成功之后使用 {@link Mono#subscriberContext} 进行获取是无法成功的
 *
 * 将采用ThreadLocal的方式进行尝试
 *
 * @author liyue
 * @version v1
 * @create 2019-10-10 15:01:32
 * @copyright www.liderong.cn
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class ReactiveRequestContextFilter implements WebFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ReactiveRequestContextHolder.put(request);
        return chain.filter(exchange)
                //Clear request save in ThreadLocal to prevent memory overflow
                .doFinally(s -> ReactiveRequestContextHolder.reset());

        //Invalid way
//        return chain.filter(exchange)
//                .subscriberContext(ctx -> ctx.put(ReactiveRequestContextHolder.CONTEXT_KEY, request));
    }

    @Override
    public int getOrder() {
        return -20;
    }
}