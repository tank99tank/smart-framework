package org.uushang.gateway.security.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * 默认重定向策略
 *
 * @author pengjunjie
 * @date 2019-09-26
 */
public class DefaultServerRedirectStrategy {

    private HttpStatus httpStatus;

    private boolean contextRelative;

    public DefaultServerRedirectStrategy() {
        this.httpStatus = HttpStatus.FOUND;
        this.contextRelative = true;
    }

    /**
     * sendRedirect ajax前端重定向
     *
     * @param exchange
     * @param location
     * @return
     */
    public Mono<Void> sendRedirect(ServerWebExchange exchange, URI location) {
        Assert.notNull(exchange, "exchange cannot be null");
        Assert.notNull(location, "location cannot be null");
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setLocation(this.createLocation(exchange, location));
        return response.writeWith(Mono.empty());
    }

    private URI createLocation(ServerWebExchange exchange, URI location) {
        if (!this.contextRelative) {
            return location;
        } else {
            String url = location.toASCIIString();
            if (url.startsWith("/")) {
                String context = exchange.getRequest().getPath().contextPath().value();
                return URI.create(context + url);
            } else {
                return location;
            }
        }
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        Assert.notNull(httpStatus, "httpStatus cannot be null");
        this.httpStatus = httpStatus;
    }

    public void setContextRelative(boolean contextRelative) {
        this.contextRelative = contextRelative;
    }
}