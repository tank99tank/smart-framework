package org.uushang.gateway.config.exception;

/**
 *
 * 无法获取参数的异常
 *
 *
 * @author liyue
 * @version v1
 * @create 2019-10-10 16:52:35
 * @copyright www.liderong.cn
 */
public class MissingServerRequestParameterException extends RuntimeException {

    public MissingServerRequestParameterException(String msg) {
        super(msg);
    }
}