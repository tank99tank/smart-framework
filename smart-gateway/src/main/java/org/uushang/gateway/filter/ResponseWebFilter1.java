package org.uushang.gateway.filter;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
//import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
 
@Slf4j
@Component
public class ResponseWebFilter1 implements WebFilter, Ordered {

    @Override
    public int getOrder() {
        return -10;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    	log.error("===> - 10 {}, {}", exchange, chain);
        return chain.filter(exchange);
    }
 
 
}
