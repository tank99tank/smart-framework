package org.uushang.gateway.config.error;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.netflix.hystrix.exception.HystrixTimeoutException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.cloud.gateway.support.TimeoutException;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.uushang.gateway.constant.ServerRequestConstant;
import org.uushang.gateway.record.AccessRecord;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * 处理请求路由过程中的异常
 *
 *
 * @author liyue
 * @version v1
 * @create 2019-04-02 11:03:43
 * @copyright www.liderong.cn
 */
@Slf4j
public class ErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {

    public ErrorWebExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties, ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resourceProperties, errorProperties, applicationContext);
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }


    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
        //获取错误信息
        Throwable throwable = super.getError(request);
        //计算请求开始时间
        Optional<Object> optional = request.attribute(ServerRequestConstant.REQUEST_TIME_BEGIN);
        LocalDateTime startTime = optional.isPresent() ? LocalDateTime
                .ofEpochSecond((Long) optional.get() / 1000, 0, ZoneOffset.ofHours(8))
                : LocalDateTime.now();
        //请求信息记录
        AccessRecord record = new AccessRecord(request);
        record.setStartTime(startTime);
        record.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        record.setResult(throwable.getMessage() == null ? throwable.toString() : throwable.getMessage());
        record.setEndTime(LocalDateTime.now());
        //此处的状态只表示gateway
        //比如 此处的404是gateway找不到路由mapping时的状态 而不是具体服务实例返回的状态
        if(throwable instanceof ResponseStatusException) {
            HttpStatus status = ((ResponseStatusException) throwable).getStatus();
            record.setHttpStatus(status.value());
            if(status == HttpStatus.NOT_FOUND) {
                return buildResult(status.value(), "未知的请求路径");
            } else if (status == HttpStatus.BAD_REQUEST) {
                return buildResult(status.value(), Objects.isNull(throwable.getMessage())
                        ? "请求无法进行处理" : throwable.getMessage());
            }
            return buildResult(status.value(), "请求处理失败");
        }
        //是否是超时类异常
        boolean rs = (throwable instanceof HystrixRuntimeException
                        && throwable.getCause() instanceof HystrixTimeoutException)
                        || throwable instanceof TimeoutException;
        if(rs) {
            record.setResult("Read timed out");
            return buildResult(HttpStatus.INTERNAL_SERVER_ERROR.value(), "请求处理超时");
        }
        //其它类型的异常统一处理
        return buildResult(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                throwable.getMessage() == null ? "网关处理发生错误" : throwable.getMessage());
    }

    @Override
    protected void logError(ServerRequest request, HttpStatus errorStatus) {
        Throwable throwable = super.getError(request);
        //如果是找不到路由 则打印日志 不打印栈信息
        if(errorStatus == HttpStatus.NOT_FOUND) {
            log.warn(buildMessage(request, throwable), throwable);
            return;
        }
        //熔断类和超时类异常也不打印栈信息
        if(throwable instanceof HystrixRuntimeException || throwable instanceof TimeoutException) {
            Throwable cause = throwable.getCause();
            log.error(buildMessage(request, cause == null ? new TimeoutException("Read timed out") : cause), throwable);
            return;
        }
        //其它类型异常统一打印
        log.error(buildMessage(request, throwable), throwable);
    }

    private Map<String, Object> buildResult(int status, String msg) {
        Map<String, Object> params = new HashMap<>(4);
        params.put("status", status);
        params.put("msg", msg);
        params.put("page", null);
        params.put("data", null);
        return params;
    }

    private String buildMessage(ServerRequest request, Throwable ex) {
        //此处可以增加打印其它的信息
        //因为webflux本身API的问题 暂时没有办法获取到IP地址
        StringBuilder message = new StringBuilder("Failed to handle request [");
        message.append(request.methodName());
        message.append(" ");
        message.append(request.uri());
        message.append("]");
        if(ex != null) {
            message.append(": ");
            message.append(ex.getMessage() == null ? ex.toString() : ex.getMessage());
        }
        return message.toString();
    }
}