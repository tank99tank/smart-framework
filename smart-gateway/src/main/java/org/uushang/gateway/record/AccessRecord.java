package org.uushang.gateway.record;

import com.alibaba.fastjson.JSON;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.uushang.gateway.constant.ServerRequestConstant;

import java.time.LocalDateTime;

/**
 *
 * 用于记录网关的请求
 *
 *
 * @author liyue
 * @version v1
 * @create 2019-04-03 11:05:06
 * @copyright www.liderong.cn
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class AccessRecord {

    private String ipAddress;

    private String url;

    private String method;

    private String headers;

    private String bodyParams;

    private String queryParams;

    private Integer httpStatus;

    private String result;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long millisecond;

    private String exception;


    public AccessRecord(ServerRequest request) {
        this.ipAddress = (String) request.attribute(ServerRequestConstant.ADDRESS_HEADER).orElse("IP Address is Null");
        this.url = request.uri().toString();
        this.method = request.methodName();
        this.headers = JSON.toJSONString(request.headers().asHttpHeaders());
        this.queryParams = JSON.toJSONString(request.queryParams());
    }

    public AccessRecord(ServerHttpRequest request) {
        this.url = request.getURI().toString();
        this.method = request.getMethodValue();
        this.headers = JSON.toJSONString(request.getHeaders());
        this.queryParams = JSON.toJSONString(request.getQueryParams());
    }
}