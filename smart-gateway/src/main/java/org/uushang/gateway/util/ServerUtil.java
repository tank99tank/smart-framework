package org.uushang.gateway.util;

import com.alibaba.fastjson.JSONObject;
import com.owinfo.config.result.Result;
import com.owinfo.config.result.Status;
import io.netty.buffer.ByteBufAllocator;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 *
 * 获取ServerHttpRequest中的信息
 *
 *
 * @author liyue
 * @version v1
 * @create 2019-04-02 13:04:14
 * @copyright www.liderong.cn
 */
public class ServerUtil {

    private ServerUtil(){}

    /**
     * webflux的request用此方式获取
     * @param request
     * @return
     */
    public static String ipAddress(ServerHttpRequest request) {
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if(Objects.isNull(remoteAddress)) {
            return "remoteAddress is Null";
        }
        String hostAddress = remoteAddress.getAddress().getHostAddress();
        return hostAddress;
    }

    /**
     * 读取request中的请求体
     * @param request
     * @return
     */
    public static String resolve(ServerHttpRequest request) {
        StringBuilder sb = new StringBuilder();
        request.getBody().subscribe(buffer -> {
            byte[] bytes = new byte[buffer.readableByteCount()];
            buffer.read(bytes);
            DataBufferUtils.release(buffer);
            String body = new String(bytes, StandardCharsets.UTF_8);
            sb.append(body);
        });
        return sb.toString();
    }

    /**
     * 还原body为DataBuffer
     * @param body
     * @return
     */
    public static DataBuffer restore(String body) {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
        DataBuffer buffer = nettyDataBufferFactory.allocateBuffer(bytes.length);
        buffer.write(bytes);
        return buffer;
    }

    /**
     * 使用response写出信息
     * @param response
     * @param msg
     * @return
     */
    public static Mono<Void> write(ServerHttpResponse response, String msg) {
        return write(response, HttpStatus.OK, msg);
    }

    /**
     * 使用response写出信息
     * @param response
     * @param msg
     * @return
     */
    public static Mono<Void> write(ServerHttpResponse response, HttpStatus status, String msg) {
        int value = status.value();
        Result result;
        switch (value) {
            case 200 :
                result = Result.build(Status.SUCCESS, msg); break;
            case 400 :
                result = Result.build(Status.BAD_REQUEST, msg); break;
            case 401 :
                result = Result.build(Status.UNAUTHORIZED, msg); break;
            case 403 :
                result = Result.build(Status.FORBIDDEN, msg); break;
            case 500 :
                result = Result.build(Status.FAILURE, msg); break;
            default:
                result = Result.build(Status.SUCCESS, msg); break;
        }
        response.setStatusCode(status);
        String val;
        try {
            val = JSONObject.toJSONString(result);
        } catch (Exception e) {
            val = msg;
        }
        return response.writeWith(Flux.just(ServerUtil.restore(val)));
    }
}