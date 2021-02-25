package org.uushang.gateway.config.context;

import org.springframework.core.NamedThreadLocal;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Objects;

/**
 *
 * Used to bind the current request and context and provide a way to get the current request
 *
 * @author liyue
 * @version v1
 * @create 2019-10-10 15:02:23
 * @copyright www.liderong.cn
 */
public class ReactiveRequestContextHolder {

//    private static final Class<ServerWebExchange> CONTEXT_KEY = ServerWebExchange.class;
//
//    /**
//     * Gets the {@code Mono<ServerWebExchange>} from Reactor {@link Context}
//     *
//     * @return the {@code Mono<ServerWebExchange>}
//     */
//    public static Mono<ServerWebExchange> getExchange() {
//        return Mono.subscriberContext()
//                .map(ctx -> ctx.get(CONTEXT_KEY));
//    }
//
//    /**
//     * Gets the {@code Mono<ServerHttpRequest>} from Reactor {@link Context}
//     *
//     * @return the {@code Mono<ServerHttpRequest>}
//     */
//    public static Mono<ServerHttpRequest> getRequest() {
//        return ReactiveRequestContextHolder.getExchange()
//                .map(ServerWebExchange::getRequest);
//    }
//
//    /**
//     * Put the {@code ServerWebExchange} to Reactor {@link Context}
//     *
//     * @param context  Context
//     * @param exchange ServerWebExchange
//     * @return the Reactor {@link Context}
//     */
//    public static Context put(Context context, ServerWebExchange exchange) {
//        return context.put(CONTEXT_KEY, exchange);
//    }

    private static final ThreadLocal<ServerHttpRequest> requests = new NamedThreadLocal<>("Thread ServerHttpRequest");

    /**
     * Store {@link ServerHttpRequest} to {@link ThreadLocal} in the current thread
     *
     * @param request {@link ServerHttpRequest}
     */
    public static void put(ServerHttpRequest request){

        //When the request time out, the reset will be invalid
        //because the timeout thread is the thread of hystrix, not the thread of HTTP
        if(Objects.nonNull(get())) {
            reset();
        }

        if(request != null){
            requests.set(request);
        }
    }

    /**
     * Get the current thread {@link ServerHttpRequest} from {@link ThreadLocal}
     *
     * @return {@link ServerHttpRequest}
     */
    public static ServerHttpRequest get(){
        ServerHttpRequest request = requests.get();
        return request;
    }

    /**
     * Clear the current thread {@link ServerHttpRequest} from {@link ThreadLocal}
     */
    public static void reset(){
        requests.remove();
    }
}