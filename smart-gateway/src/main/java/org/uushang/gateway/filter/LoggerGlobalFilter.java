package org.uushang.gateway.filter;

import io.netty.buffer.UnpooledByteBufAllocator;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.DefaultServerRequest;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.uushang.gateway.constant.ServerRequestConstant;
import org.uushang.gateway.record.AccessRecord;
import org.uushang.gateway.record.Records;
import org.uushang.gateway.util.ServerUtil;
import org.uushang.gateway.util.StringUtil;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * 记录网关的请求日志信息
 *
 *
 * @author liyue
 * @version v1
 * @create 2019-04-01 17:34:45
 * @copyright www.liderong.cn
 */
@Slf4j
@Component
public class LoggerGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public int getOrder() {
        return -5;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //验证类型
        String scheme = exchange.getRequest().getURI().getScheme().toUpperCase();
        boolean rs = ServerRequestConstant.HTTP_HEAD.equals(scheme) || ServerRequestConstant.HTTPS_HEAD.equals(scheme);
        if(!rs) {
            return chain.filter(exchange);
        }
        ServerHttpRequest request = exchange.getRequest();
        String ipAddress = ServerUtil.ipAddress(request);
        //存放请求相关信息
        exchange.getAttributes().put(ServerRequestConstant.REQUEST_TIME_BEGIN, System.currentTimeMillis());
        exchange.getAttributes().put(ServerRequestConstant.ADDRESS_HEADER, ipAddress);
        //记录请求的基本信息
        AccessRecord record = new AccessRecord(request);
        record.setStartTime(LocalDateTime.now());
        record.setIpAddress(ipAddress);
        //记录请求体的类型 上传文件类型的请求单独处理
        //GET请求不存在请求体 只记录POST类型
        String contentType = request.getHeaders().getFirst("Content-Type");
        boolean res =
                Objects.nonNull(contentType)
                && ServerRequestConstant.REQUEST_METHOD_POST.equals(request.getMethodValue())
                && !contentType.startsWith(MediaType.MULTIPART_FORM_DATA_VALUE);
        if(res) {
            //POST方式 读取请求体
            //requestBody具有只能被读取一次的特性 所以需要在记录之后重新包装一个新的request对象
            //因为gateway是基于reactive开发的原因
            //导致遇到很多问题 目前的实现方式能够避免一下问题 暂不知后续是否还有问题
            //1.请求体只能读取一次
            //2.请求体超过1024B被截断
            //3.请求体为空时无法获取到响应
            ServerHttpRequest mutatedRequest = resolveRequest(exchange, record);
            ServerHttpResponse mutatedResponse = resolveResponse(exchange, record);
            return filter(exchange.mutate().request(mutatedRequest).response(mutatedResponse).build(), chain, record);
        }
        //GET方式直接使用原有对象
        ServerHttpResponse mutatedResponse = resolveResponse(exchange, record);
        return filter(exchange.mutate().response(mutatedResponse).build(), chain, record);
    }

    /**
     * 记录响应之后的耗时信息
     * 处理访问请求记录
     * @param exchange
     * @param chain
     * @return
     */
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain, AccessRecord record) {
        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    Long startTime = exchange.getAttribute(ServerRequestConstant.REQUEST_TIME_BEGIN);
                    //计算耗时
                    //如果exchange中没有获取到REQUEST_TIME_BEGIN 则使用记录中的开始时间
                    if(Objects.isNull(startTime)) {
                        //转换为毫秒数
                        startTime = record.getStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                        //为了防止输出的日志太多 此处只打印部分日志
                        //同时添加一个ID 后续可以通过此ID获取完整的记录
                        String recordId = UUID.randomUUID().toString().replaceAll("-", "");
                        record.setException(recordId);
                        log.error("===> {} {} {} request Attribute[requestTimeBegin] Not found",
                                record.getIpAddress(), record.getUrl(), recordId);
                    }
                    record.setEndTime(LocalDateTime.now());
                    record.setMillisecond(System.currentTimeMillis() - startTime);
                    //处理请求记录
                    Records.handler(record);
                })
        );
    }

    /**
     * 处理请求体
     * 处理之后封装为新的request
     * @param exchange
     * @param record
     * @return
     */
    private ServerHttpRequest resolveRequest(ServerWebExchange exchange, AccessRecord record) {
        DefaultServerRequest serverRequest = new DefaultServerRequest(exchange);
        ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
            //记录请求体内容
            private StringBuilder builder = new StringBuilder();
            //因为没有对请求体进行操作 所以这里重写只是为了加上TRANSFER_ENCODING的请求头
            //TRANSFER_ENCODING是用来表示分块传输 目前不知道具体意义
            //因为读取方式是参考的源码类ModifyRequestBodyGatewayFilterFactory
            //此类也进行了重写 为了防止未知问题
            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.putAll(super.getHeaders());
                httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
                return httpHeaders;
            }
            //解析请求体 能够解决请求体过长获取不完整的问题
            //使用此方式 在请求体为空时 依然能截获到响应
            @Override
            public Flux<DataBuffer> getBody() {
                return serverRequest.bodyToFlux(String.class).map(body -> {
                    builder.append(body);
                    NettyDataBufferFactory nettyDataBufferFactory =
                        new NettyDataBufferFactory(new UnpooledByteBufAllocator(false));
                    return nettyDataBufferFactory.wrap(body.getBytes());
                }).doOnComplete(()-> record.setBodyParams(StringUtil.format(builder.toString())));
            }
        };
        return mutatedRequest;
    }

    /**
     * 处理并读取response
     * 读取之后需要封装为新的response
     * @param exchange
     * @param record
     * @return
     */
    private ServerHttpResponse resolveResponse(ServerWebExchange exchange, AccessRecord record) {
        ServerHttpResponse response = exchange.getResponse();
        DataBufferFactory bufferFactory = response.bufferFactory();
        ServerHttpResponseDecorator mutatedResponse = new ServerHttpResponseDecorator(response) {
            //记录响应体内容
            //应该也会出现响应体过长导致的截断问题
            //测试结果 当响应体过长时 会多次读取 前端收到的结果是完整的 记录日志时 需要多次append才能得到完整的数据

            //记录响应体内容
            private StringBuilder builder = new StringBuilder();

            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                //记录请求执行状态
                //只能写在读取方法中 因为此时才收到了响应
                record.setHttpStatus(response.getStatusCode().value());
                /*************************************************/
                if (body instanceof Flux) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                    return super.writeWith(fluxBody.map(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);
                        String resp = new String(bytes, Charset.forName("UTF-8"));
                        builder.append(resp);
                        record.setResult(StringUtil.format(builder.toString()));
                        return bufferFactory.wrap(bytes);
                    }));
                }
                /*************************************************/
                return super.writeWith(body);
            }
        };
        return mutatedResponse;
    }
}